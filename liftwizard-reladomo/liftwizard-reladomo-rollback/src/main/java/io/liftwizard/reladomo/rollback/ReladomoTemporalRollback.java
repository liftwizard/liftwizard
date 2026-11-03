/*
 * Copyright 2026 Craig Motlin
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.gs.fw.common.mithra.MithraDatedTransactionalObject;
import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.fw.finder.TemporalTransactionalDomainList;
import com.gs.reladomo.metadata.ReladomoClassMetaData;
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
 *   <li>Restore versions that were superseded after the rollback point, meaning rows where
 *   system_from &lt;= targetDate AND targetDate &lt; system_to &lt; infinity, by setting system_to = infinity</li>
 * </ol>
 *
 * <p>All writes go through Reladomo inside one transaction, so they are buffered until commit and keep the cache
 * consistent. Atomicity at the database level depends on the connection manager taking part in the Reladomo
 * transaction, as Liftwizard's connection managers do through {@code TransactionalConnectionSource}.
 *
 * @see <a href="https://github.com/goldmansachs/reladomo/issues/261">Reladomo Issue #261</a>
 */
public class ReladomoTemporalRollback
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ReladomoTemporalRollback.class);

	private final Instant targetDate;
	private final Timestamp targetTimestamp;

	public ReladomoTemporalRollback(Instant targetDate)
	{
		this.targetDate = Objects.requireNonNull(targetDate);
		this.targetTimestamp = Timestamp.from(targetDate);
	}

	public void rollbackAllTables()
	{
		LOGGER.info("Rolling back all bitemporal tables to: {}", this.targetDate);

		MithraManagerProvider.getMithraManager().executeTransactionalCommand((tx) ->
		{
			MithraManagerProvider.getMithraManager().getRuntimeCacheControllerSet().forEach(this::rollbackTable);
			return null;
		});

		LOGGER.info("Rollback completed for all bitemporal tables");
	}

	private void rollbackTable(MithraRuntimeCacheController cacheController)
	{
		ReladomoClassMetaData metaData = cacheController.getMetaData();

		AsOfAttribute[] asOfAttributes = metaData.getAsOfAttributes();
		if (ArrayIterate.isEmpty(asOfAttributes))
		{
			LOGGER.debug("Skipping non-temporal table: {}", metaData.getBusinessOrInterfaceClassName());
			return;
		}

		Optional<AsOfAttribute> maybeSystemAttribute = ArrayIterate.detectOptional(
			asOfAttributes,
			AsOfAttribute::isProcessingDate
		);
		if (maybeSystemAttribute.isEmpty())
		{
			LOGGER.debug("Skipping non-system-temporal table: {}", metaData.getBusinessOrInterfaceClassName());
			return;
		}

		RelatedFinder<?> finder = metaData.getFinderInstance();
		String tableName = finder.getMithraObjectPortal().getDatabaseObject().getDefaultTableName();
		AsOfAttribute systemColumn = maybeSystemAttribute.orElseThrow(() ->
			new IllegalStateException("No system temporal attribute found")
		);

		LOGGER.info(
			"Rolling back table: {} (system_from={}, system_to={})",
			tableName,
			systemColumn.getFromAttribute().getColumnName(),
			systemColumn.getToAttribute().getColumnName()
		);

		TimestampAttribute systemFromAttribute = systemColumn.getFromAttribute();
		TimestampAttribute systemToAttribute = systemColumn.getToAttribute();
		Timestamp infinity = systemColumn.getInfinityDate();
		Operation edgePointOperation = ArrayAdapter.adapt(asOfAttributes)
			.collect(AsOfAttribute::equalsEdgePoint)
			.reduce(Operation::and)
			.orElseThrow();

		// Run both queries before any write. A query flushes buffered writes for its portal, so querying between
		// writes would push them to the database before the transaction commits.
		MithraList<?> futureVersions = finder.findMany(
			edgePointOperation.and(systemFromAttribute.greaterThan(this.targetTimestamp))
		);
		MithraList<?> supersededVersions = finder.findMany(
			edgePointOperation
				.and(systemFromAttribute.lessThanEquals(this.targetTimestamp))
				.and(systemToAttribute.greaterThan(this.targetTimestamp))
				.and(systemToAttribute.lessThan(infinity))
		);
		int futureVersionCount = futureVersions.size();
		supersededVersions.forceResolve();

		purgeAll(futureVersions);
		LOGGER.info("Purged {} future versions from {}", futureVersionCount, tableName);

		// Restore superseded versions by replacing each with a copy whose system_to is infinity. Going through
		// Reladomo keeps the restore in the transaction, uses Reladomo's timestamp conversion, and updates the cache.
		ListIterable<MithraDatedTransactionalObject> restoredVersions = Lists.mutable
			.withAll(supersededVersions)
			.collect(MithraDatedTransactionalObject.class::cast)
			.collect((supersededVersion) ->
			{
				MithraDatedTransactionalObject restoredVersion = supersededVersion.getNonPersistentCopy();
				systemToAttribute.setValue(restoredVersion, infinity);
				return restoredVersion;
			});

		purgeAll(supersededVersions);
		restoredVersions.forEach(MithraDatedTransactionalObject::insertForRecovery);
		LOGGER.info("Restored {} superseded versions in {}", restoredVersions.size(), tableName);
	}

	private static void purgeAll(MithraList<?> versions)
	{
		if (versions.isEmpty())
		{
			return;
		}

		if (!(versions instanceof TemporalTransactionalDomainList<?> temporalList))
		{
			throw new IllegalStateException(
				"Cannot purge versions - invalid list type: " + versions.getClass().getName()
			);
		}

		temporalList.purgeAll();
	}
}
