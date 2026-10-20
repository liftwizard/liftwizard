package com.example.helloworld.reladomo;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import com.example.helloworld.core.Person;
import com.example.helloworld.core.PersonFinder;
import com.example.helloworld.reladomo.PersonTable.PersonRow;
import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.reladomo.test.extension.ExecuteSqlExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityTimestamp;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reladomo writes and queries as-of infinity as the UTC wall clock {@code 9999-12-01 23:59:00}, the same literal that
 * SQL migrations use, and converts other convert-to-utc timestamps correctly, regardless of the JVM default time zone.
 *
 * <p>Subclasses enable exactly one of the mechanisms that make this work, so that each mechanism is tested on its own.
 * The surefire configuration runs these tests in UTC and in America/New_York.</p>
 */
abstract class AbstractReladomoInfinityTest
{
	private static final LocalDateTime JANUARY_1_2000 = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
	private static final LocalDateTime JANUARY_1_2001 = LocalDateTime.of(2001, 1, 1, 0, 0, 0);

	@RegisterExtension
	@Order(1)
	final ExecuteSqlExtension executeSqlExtension = new ExecuteSqlExtension();

	@RegisterExtension
	@Order(4)
	final ReladomoPurgeAllExtension purgeAllExtension = new ReladomoPurgeAllExtension();

	@Test
	void reladomoInsertStoresInfinityAsUtcWallClock()
		throws SQLException
	{
		MithraManagerProvider.getMithraManager().executeTransactionalCommand((tx) ->
		{
			var person = new Person();
			person.setFullName("Inserted by Reladomo");
			person.setJobTitle("Job Title");
			person.insert();
			return null;
		});

		assertThat(PersonTable.selectAll())
			.extracting(PersonRow::fullName, PersonRow::systemTo)
			.containsExactly(tuple("Inserted by Reladomo", PersonTable.INFINITY_UTC_WALL_CLOCK));
	}

	@Test
	void infinityQueryFindsRowSeededBySql()
		throws SQLException
	{
		PersonTable.insert(new PersonRow(10L, "Seeded by SQL", JANUARY_1_2000, PersonTable.INFINITY_UTC_WALL_CLOCK));

		Person person = PersonFinder.findOne(PersonFinder.id().eq(10L));

		assertThat(person).isNotNull();
		assertThat(person.getFullName()).isEqualTo("Seeded by SQL");
		assertThat(person.getSystemFrom().toInstant()).isEqualTo(Instant.parse("2000-01-01T00:00:00Z"));
		assertThat(person.getSystemTo().toInstant()).isEqualTo(UtcInfinityTimestamp.getDefaultInfinityInstant());
	}

	@Test
	void nonInfinityTimestampsAreConvertedToUtc()
		throws SQLException
	{
		PersonTable.insert(new PersonRow(11L, "Historical", JANUARY_1_2000, JANUARY_1_2001));

		assertThat(findHistorical("1999-12-31T23:30:00Z")).isNull();
		assertThat(findHistorical("2001-01-01T00:30:00Z")).isNull();

		Person person = findHistorical("2000-01-01T00:30:00Z");
		assertThat(person).isNotNull();
		assertThat(person.getSystemFrom().toInstant()).isEqualTo(Instant.parse("2000-01-01T00:00:00Z"));
		assertThat(person.getSystemTo().toInstant()).isEqualTo(Instant.parse("2001-01-01T00:00:00Z"));
	}

	private static Person findHistorical(String processingDate)
	{
		Timestamp timestamp = Timestamp.from(Instant.parse(processingDate));
		Operation operation = PersonFinder.id().eq(11L).and(PersonFinder.system().eq(timestamp));
		return PersonFinder.findOneBypassCache(operation);
	}
}
