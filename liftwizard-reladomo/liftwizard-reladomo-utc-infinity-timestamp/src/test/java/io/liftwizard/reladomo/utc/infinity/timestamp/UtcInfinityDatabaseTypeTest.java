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
package io.liftwizard.reladomo.utc.infinity.timestamp;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import com.gs.fw.common.mithra.databasetype.AbstractDatabaseType;
import com.gs.fw.common.mithra.databasetype.DerbyDatabaseType;
import com.gs.fw.common.mithra.databasetype.GenericDatabaseType;
import com.gs.fw.common.mithra.databasetype.H2DatabaseType;
import com.gs.fw.common.mithra.databasetype.MariaDatabaseType;
import com.gs.fw.common.mithra.databasetype.MsSqlDatabaseType;
import com.gs.fw.common.mithra.databasetype.OracleDatabaseType;
import com.gs.fw.common.mithra.databasetype.PostgresDatabaseType;
import com.gs.fw.common.mithra.databasetype.SybaseDatabaseType;
import com.gs.fw.common.mithra.databasetype.SybaseIqDatabaseType;
import com.gs.fw.common.mithra.databasetype.Udb82DatabaseType;
import com.gs.fw.common.mithra.util.MithraTimestamp;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtcInfinityDatabaseTypeTest
{
	private static final LocalDateTime INFINITY_UTC_WALL_CLOCK = LocalDateTime.of(9999, 12, 1, 23, 59, 0);
	private static final Timestamp NON_INFINITY = Timestamp.from(Instant.parse("2000-01-01T00:30:00Z"));

	private static final ImmutableMap<AbstractDatabaseType, AbstractDatabaseType> UTC_INFINITY_TO_REGULAR =
		Maps.immutable
			.<AbstractDatabaseType, AbstractDatabaseType>empty()
			.newWithKeyValue(UtcInfinityDerbyDatabaseType.getInstance(), DerbyDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityGenericDatabaseType.getInstance(), GenericDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityH2DatabaseType.getInstance(), H2DatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityMariaDatabaseType.getInstance(), MariaDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityMsSqlDatabaseType.getInstance(), MsSqlDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityOracleDatabaseType.getInstance(), OracleDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityPostgresDatabaseType.getInstance(), PostgresDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinitySybaseDatabaseType.getInstance(), SybaseDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinitySybaseIqDatabaseType.getInstance(), SybaseIqDatabaseType.getInstance())
			.newWithKeyValue(UtcInfinityUdb82DatabaseType.getInstance(), Udb82DatabaseType.getInstance());

	/**
	 * Reladomo binds as-of infinity with {@link MithraTimestamp#DefaultTimeZone}, which writes the JVM-local wall clock.
	 * The UTC infinity database types write the UTC wall clock instead.
	 */
	@Test
	void bindsInfinityAsUtcWallClock()
	{
		ImmutableMap<String, LocalDateTime> actual = UTC_INFINITY_TO_REGULAR.keysView()
			.toMap(
				(databaseType) -> databaseType.getClass().getSimpleName(),
				(databaseType) ->
					bindWallClock(
						databaseType,
						UtcInfinityTimestamp.getDefaultInfinity(),
						MithraTimestamp.DefaultTimeZone
					)
			)
			.toImmutable();

		ImmutableMap<String, LocalDateTime> expected = UTC_INFINITY_TO_REGULAR.keysView()
			.toMap((databaseType) -> databaseType.getClass().getSimpleName(), (databaseType) -> INFINITY_UTC_WALL_CLOCK)
			.toImmutable();

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void bindsOtherTimeZonesLikeTheRegularDatabaseType()
	{
		TimeZone newYork = TimeZone.getTimeZone("America/New_York");
		TimeZone jvmDefault = TimeZone.getDefault();

		ImmutableMap<String, String> actual = UTC_INFINITY_TO_REGULAR.keyValuesView()
			.toMap(
				(pair) -> pair.getOne().getClass().getSimpleName(),
				(pair) ->
					bindWallClock(pair.getOne(), NON_INFINITY, MithraTimestamp.UtcTimeZone)
					+ " "
					+ bindWallClock(pair.getOne(), NON_INFINITY, newYork)
					+ " "
					+ bindWallClock(pair.getOne(), NON_INFINITY, jvmDefault)
			)
			.toImmutable();

		ImmutableMap<String, String> expected = UTC_INFINITY_TO_REGULAR.keyValuesView()
			.toMap(
				(pair) -> pair.getOne().getClass().getSimpleName(),
				(pair) ->
					bindWallClock(pair.getTwo(), NON_INFINITY, MithraTimestamp.UtcTimeZone)
					+ " "
					+ bindWallClock(pair.getTwo(), NON_INFINITY, newYork)
					+ " "
					+ bindWallClock(pair.getTwo(), NON_INFINITY, jvmDefault)
			)
			.toImmutable();

		assertThat(actual).isEqualTo(expected);
		assertThat(bindWallClock(UtcInfinityH2DatabaseType.getInstance(), NON_INFINITY, newYork)).isEqualTo(
			LocalDateTime.of(1999, 12, 31, 19, 30, 0)
		);
	}

	@Test
	void instancesAreSubclassesOfTheRegularDatabaseTypes()
	{
		UTC_INFINITY_TO_REGULAR.forEachKeyValue((utcInfinity, regular) ->
			assertThat(utcInfinity).isInstanceOf(regular.getClass())
		);
	}

	/**
	 * Binds the timestamp the way Reladomo does, and returns the wall clock that the database would store.
	 */
	private static LocalDateTime bindWallClock(
		AbstractDatabaseType databaseType,
		Timestamp timestamp,
		TimeZone timeZone
	)
	{
		var result = new AtomicReference<LocalDateTime>();
		var preparedStatement = (PreparedStatement) Proxy.newProxyInstance(
			UtcInfinityDatabaseTypeTest.class.getClassLoader(),
			new Class<?>[]
			{
				PreparedStatement.class,
			},
			(proxy, method, args) ->
			{
				if (!"setTimestamp".equals(method.getName()) || args.length != 3)
				{
					throw new UnsupportedOperationException(method.toString());
				}
				var boundTimestamp = (Timestamp) args[1];
				var calendar = (Calendar) args[2];
				Instant instant = boundTimestamp.toInstant();
				result.set(LocalDateTime.ofInstant(instant, calendar.getTimeZone().toZoneId()));
				return null;
			}
		);
		try
		{
			databaseType.setTimestamp(preparedStatement, 1, timestamp, false, timeZone);
		}
		catch (SQLException e)
		{
			throw new AssertionError(e);
		}
		return result.get();
	}
}
