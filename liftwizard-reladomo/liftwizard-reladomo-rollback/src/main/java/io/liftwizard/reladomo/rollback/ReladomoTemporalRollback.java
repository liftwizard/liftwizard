/*
 * Copyright 2025 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.reladomo.rollback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.TimeZone;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.MithraObjectPortal;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.fw.finder.TemporalTransactionalDomainList;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityTimestamp;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rolls back all temporal tables to a specified point in time.
 *
 * <p>For each temporal table, this performs two operations:
 * <ol>
 *   <li>Purge all rows where system_from &gt; targetDate</li>
 *   <li>Restore versions that were superseded after the rollback point</li>
 * </ol>
 *
 * @see <a href="https://github.com/goldmansachs/reladomo/issues/261">Reladomo Issue #261</a>
 */
public class ReladomoTemporalRollback {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoTemporalRollback.class);

	private final Instant targetDate;
	private final Timestamp targetTimestamp;
	private final Timestamp infinityTimestamp;

	public ReladomoTemporalRollback(Instant targetDate) {
		this(targetDate, UtcInfinityTimestamp.getDefaultInfinityInstant());
	}

	public ReladomoTemporalRollback(Instant targetDate, Instant infinityDate) {
		this.targetDate = Objects.requireNonNull(targetDate);
		this.targetTimestamp = Timestamp.from(targetDate);
		this.infinityTimestamp = Timestamp.from(Objects.requireNonNull(infinityDate));
	}

	public void rollbackAllTables() {
		LOGGER.info("Rolling back all bitemporal tables to: {}", this.targetDate);

		MithraManagerProvider.getMithraManager().executeTransactionalCommand((tx) -> {
				MithraManagerProvider.getMithraManager().getRuntimeCacheControllerSet().forEach(this::rollbackTable);
				return null;
			});

		LOGGER.info("Rollback completed for all bitemporal tables");
	}

	private void rollbackTable(MithraRuntimeCacheController cacheController) {
		ReladomoClassMetaData metaData = cacheController.getMetaData();

		AsOfAttribute[] asOfAttributes = metaData.getAsOfAttributes();
		if (asOfAttributes == null) {
			return;
		}

		AsOfAttribute systemAttribute = ArrayIterate.detect(asOfAttributes, AsOfAttribute::isProcessingDate);

		if (systemAttribute == null) {
			return;
		}

		RelatedFinder<?> finder = metaData.getFinderInstance();
		MithraObjectPortal portal = finder.getMithraObjectPortal();
		String tableName = portal.getDatabaseObject().getDefaultTableName();

		LOGGER.info(
			"Rolling back table: {} (system_from={}, system_to={})",
			tableName,
			systemAttribute.getFromAttribute().getColumnName(),
			systemAttribute.getToAttribute().getColumnName()
		);

		this.purgeFutureVersions(metaData, finder, systemAttribute, tableName);
		this.restoreSupersededVersions(portal, tableName, systemAttribute);
	}

	/**
	 * Purges all rows where system_from &gt; targetDate.
	 */
	private void purgeFutureVersions(
		ReladomoClassMetaData metaData,
		RelatedFinder<?> finder,
		AsOfAttribute systemAttribute,
		String tableName
	) {
		TimestampAttribute systemFromAttribute = systemAttribute.getFromAttribute();

		ListIterable<AsOfAttribute> asOfAttributes = metaData.getAsOfAttributes() == null
			? Lists.immutable.empty()
			: ArrayAdapter.adapt(metaData.getAsOfAttributes());

		Operation edgePointOperation = asOfAttributes
			.collect(AsOfAttribute::equalsEdgePoint)
			.reduce(Operation::and)
			.orElseGet(finder::all);

		Operation futureVersionsOperation = edgePointOperation.and(
			systemFromAttribute.greaterThan(this.targetTimestamp)
		);

		MithraList<?> futureVersions = finder.findMany(futureVersionsOperation);
		int count = futureVersions.size();

		if (count == 0) {
			LOGGER.info("No future versions to purge from {}", tableName);
			return;
		}

		if (!(futureVersions instanceof TemporalTransactionalDomainList<?> temporalList)) {
			throw new IllegalStateException("Cannot purge future versions - invalid list type");
		}

		temporalList.purgeAll();
		LOGGER.info("Purged {} future versions from {}", count, tableName);
	}

	/**
	 * Restores superseded versions by setting system_to = infinity for rows
	 * where system_to &gt; targetDate AND system_to &lt; infinity.
	 */
	private void restoreSupersededVersions(MithraObjectPortal portal, String tableName, AsOfAttribute systemAttribute) {
		String systemToColumn = systemAttribute.getToAttribute().getColumnName();

		String sql = String.format(
			"UPDATE %s SET %s = ? WHERE %s > ? AND %s < ?",
			tableName,
			systemToColumn,
			systemToColumn,
			systemToColumn
		);

		SourcelessConnectionManager connectionManager = (SourcelessConnectionManager) portal
			.getDatabaseObject()
			.getConnectionManager();
		DatabaseType databaseType = connectionManager.getDatabaseType();
		TimeZone timeZone = connectionManager.getDatabaseTimeZone();

		try (
			Connection connection = connectionManager.getConnection();
			PreparedStatement statement = connection.prepareStatement(sql)
		) {
			databaseType.setTimestamp(statement, 1, this.infinityTimestamp, false, timeZone);
			databaseType.setTimestamp(statement, 2, this.targetTimestamp, false, timeZone);
			databaseType.setTimestamp(statement, 3, this.infinityTimestamp, false, timeZone);

			int updatedRows = statement.executeUpdate();

			if (updatedRows > 0) {
				LOGGER.info("Restored {} superseded versions in {}", updatedRows, tableName);
			} else {
				LOGGER.info("No superseded versions to restore in {}", tableName);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to execute SQL: " + sql, e);
		}
	}
}
