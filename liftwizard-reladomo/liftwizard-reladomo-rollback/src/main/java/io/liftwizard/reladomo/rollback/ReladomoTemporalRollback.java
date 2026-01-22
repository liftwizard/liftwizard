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
import java.util.Optional;

import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.MithraObjectPortal;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityTimestamp;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rolls back all bitemporal Reladomo tables to a specified point in time.
 *
 * <p>For each temporal table, this performs two operations:
 * <ol>
 *   <li>Delete all rows where system_from &gt; targetDate (versions created after the rollback point)</li>
 *   <li>Update system_to = infinity for all rows where system_to &gt; targetDate AND system_to &lt; infinity
 *       (restore versions that were superseded after the rollback point)</li>
 * </ol>
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

		RelatedFinder<?> finder = metaData.getFinderInstance();
		MithraObjectPortal portal = finder.getMithraObjectPortal();
		String tableName = portal.getDatabaseObject().getDefaultTableName();

		Optional<AsOfAttribute> maybeSystemAttribute = ArrayIterate.detectOptional(
			metaData.getAsOfAttributes(),
			AsOfAttribute::isProcessingDate
		);
		if (maybeSystemAttribute.isEmpty()) {
			LOGGER.debug("Skipping non-system-temporal table: {}", metaData.getBusinessOrInterfaceClassName());
			return;
		}
		AsOfAttribute systemColumn = maybeSystemAttribute.orElseThrow(() ->
			new IllegalStateException("No system temporal attribute found")
		);
		String systemFromColumn = systemColumn.getFromAttribute().getColumnName();
		String systemToColumn = systemColumn.getToAttribute().getColumnName();

		LOGGER.info(
			"Rolling back table: {} (system_from={}, system_to={})",
			tableName,
			systemFromColumn,
			systemToColumn
		);

		this.deleteFutureVersions(portal, tableName, systemFromColumn);
		this.restoreSupersededVersions(portal, tableName, systemToColumn);
	}

	private void deleteFutureVersions(MithraObjectPortal portal, String tableName, String systemFromColumn) {
		String sql = String.format("DELETE FROM %s WHERE %s > ?", tableName, systemFromColumn);

		int deletedRows = this.executeUpdate(portal, sql, this.targetTimestamp);
		LOGGER.info("Deleted {} future versions from {}", deletedRows, tableName);
	}

	private void restoreSupersededVersions(MithraObjectPortal portal, String tableName, String systemToColumn) {
		String sql = String.format(
			"UPDATE %s SET %s = ? WHERE %s > ? AND %s < ?",
			tableName,
			systemToColumn,
			systemToColumn,
			systemToColumn
		);

		int updatedRows = this.executeUpdate(
			portal,
			sql,
			this.infinityTimestamp,
			this.targetTimestamp,
			this.infinityTimestamp
		);
		LOGGER.info("Restored {} superseded versions in {}", updatedRows, tableName);
	}

	private int executeUpdate(MithraObjectPortal portal, String sql, Timestamp... parameters) {
		SourcelessConnectionManager connectionManager = (SourcelessConnectionManager) portal
			.getDatabaseObject()
			.getConnectionManager();
		DatabaseType databaseType = connectionManager.getDatabaseType();

		try (
			Connection connection = connectionManager.getConnection();
			PreparedStatement statement = connection.prepareStatement(sql)
		) {
			for (int i = 0; i < parameters.length; i++) {
				databaseType.setTimestamp(statement, i + 1, parameters[i], false, null);
			}
			return statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Failed to execute SQL: " + sql, e);
		}
	}
}
