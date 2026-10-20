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

package io.liftwizard.dropwizard.configuration.connectionmanager;

import java.util.Objects;

import com.gs.fw.common.mithra.databasetype.DatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityDerbyDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityGenericDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityH2DatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityMariaDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityMsSqlDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityOracleDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityPostgresDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinitySybaseDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinitySybaseIqDatabaseType;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityUdb82DatabaseType;

/**
 * The Reladomo database types that can be configured by name.
 *
 * <p>Each value uses a database type that writes as-of infinity as its UTC wall clock, so that rows written by
 * Reladomo match rows written by SQL, like {@code system_to = '9999-12-01 23:59:00'}, regardless of the JVM default
 * time zone.</p>
 */
public enum DatabaseTypeEnum
{
	DERBY(UtcInfinityDerbyDatabaseType.getInstance()),
	GENERIC(UtcInfinityGenericDatabaseType.getInstance()),
	H2(UtcInfinityH2DatabaseType.getInstance()),
	MARIA(UtcInfinityMariaDatabaseType.getInstance()),
	MSSQL(UtcInfinityMsSqlDatabaseType.getInstance()),
	ORACLE(UtcInfinityOracleDatabaseType.getInstance()),
	POSTGRES(UtcInfinityPostgresDatabaseType.getInstance()),
	SYBASE(UtcInfinitySybaseDatabaseType.getInstance()),
	SYBASE_IQ(UtcInfinitySybaseIqDatabaseType.getInstance()),
	SYBASE_IQ_NATIVE(UtcInfinitySybaseIqDatabaseType.getInstance()),
	UDB_82(UtcInfinityUdb82DatabaseType.getInstance());

	private final DatabaseType databaseType;

	DatabaseTypeEnum(DatabaseType databaseType)
	{
		this.databaseType = Objects.requireNonNull(databaseType);
	}

	public DatabaseType getDatabaseType()
	{
		return this.databaseType;
	}
}
