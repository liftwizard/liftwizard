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

package io.liftwizard.reladomo.csv.test.extension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;

import com.example.helloworld.core.Person;
import com.example.helloworld.core.PersonFinder;
import com.gs.fw.common.mithra.MithraList;
import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import io.liftwizard.reladomo.test.extension.ExecuteSqlExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import org.assertj.core.groups.Tuple;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class CsvTestDataExtensionTest
{
	private static final LocalDateTime INFINITY_UTC_WALL_CLOCK = LocalDateTime.of(9999, 12, 1, 23, 59, 0);

	@RegisterExtension
	@Order(1)
	final ExecuteSqlExtension executeSqlExtension = new ExecuteSqlExtension();

	@RegisterExtension
	@Order(2)
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/TestReladomoRuntimeConfiguration.xml"
	);

	@RegisterExtension
	@Order(3)
	final ReladomoPurgeAllExtension purgeAllExtension = new ReladomoPurgeAllExtension();

	@RegisterExtension
	@Order(4)
	final CsvTestDataExtension csvTestDataExtension = new CsvTestDataExtension(
		"test-data/com.example.helloworld.core.Person.csv"
	);

	@Test
	void loadsTestDataFromCsv()
	{
		MithraList<Person> allPersons = PersonFinder.findMany(PersonFinder.system().equalsEdgePoint());

		assertThat(allPersons)
			.extracting(Person::getId, Person::getFullName, Person::getJobTitle)
			.containsExactlyInAnyOrder(
				Tuple.tuple(1L, "Alice Smith", "Engineer"),
				Tuple.tuple(2L, "Bob Jones", "Manager")
			);
	}

	@Test
	void loadedDataHasCorrectValues()
	{
		Person alice = PersonFinder.findOne(PersonFinder.id().eq(1L).and(PersonFinder.system().equalsEdgePoint()));
		Person bob = PersonFinder.findOne(PersonFinder.id().eq(2L).and(PersonFinder.system().equalsEdgePoint()));

		Instant aliceSystemFrom = Instant.parse("2024-01-01T00:00:00.000Z");
		Instant bobSystemFrom = Instant.parse("2024-01-15T00:00:00.000Z");
		Instant infinityInstant = PersonFinder.systemTo().getAsOfAttributeInfinity().toInstant();

		assertThat(alice)
			.isNotNull()
			.extracting(
				Person::getId,
				Person::getFullName,
				Person::getJobTitle,
				(person) -> person.getSystemFrom().toInstant(),
				(person) -> person.getSystemTo().toInstant()
			)
			.containsExactly(1L, "Alice Smith", "Engineer", aliceSystemFrom, infinityInstant);

		assertThat(bob)
			.isNotNull()
			.extracting(
				Person::getId,
				Person::getFullName,
				Person::getJobTitle,
				(person) -> person.getSystemFrom().toInstant(),
				(person) -> person.getSystemTo().toInstant()
			)
			.containsExactly(2L, "Bob Jones", "Manager", bobSystemFrom, infinityInstant);
	}

	@Test
	void storesInfinityAsUtcWallClock()
		throws SQLException
	{
		assertThat(selectSystemTo()).containsExactly(INFINITY_UTC_WALL_CLOCK, INFINITY_UTC_WALL_CLOCK);
	}

	private static ImmutableList<LocalDateTime> selectSystemTo()
		throws SQLException
	{
		try (
			Connection connection = H2InMemoryConnectionManager.getInstance().getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT system_to FROM PERSON ORDER BY id")
		)
		{
			MutableList<LocalDateTime> result = Lists.mutable.empty();
			while (resultSet.next())
			{
				result.add(resultSet.getObject(1, LocalDateTime.class));
			}
			return result.toImmutable();
		}
	}
}
