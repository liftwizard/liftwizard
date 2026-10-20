package com.example.helloworld.reladomo;

import java.sql.SQLException;
import java.time.LocalDateTime;

import com.example.helloworld.reladomo.PersonTable.PersonRow;
import io.liftwizard.reladomo.test.extension.ExecuteSqlExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import io.liftwizard.reladomo.test.extension.ReladomoTestFile;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rows loaded from Reladomo test data files store as-of infinity as the UTC wall clock, like rows seeded by SQL.
 */
class ReladomoTestDataInfinityTest
{
	@RegisterExtension
	@Order(1)
	final ExecuteSqlExtension executeSqlExtension = new ExecuteSqlExtension();

	@RegisterExtension
	@Order(2)
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/H2InMemoryReladomoRuntimeConfiguration.xml"
	);

	@RegisterExtension
	@Order(3)
	final ReladomoPurgeAllExtension purgeAllExtension = new ReladomoPurgeAllExtension();

	@RegisterExtension
	@Order(4)
	final ReladomoLoadDataExtension loadDataExtension = new ReladomoLoadDataExtension();

	@Test
	@ReladomoTestFile("test-data/person.txt")
	void storesInfinityAsUtcWallClock()
		throws SQLException
	{
		assertThat(PersonTable.selectAll()).containsExactly(
			new PersonRow(
				1L,
				"Full Name",
				LocalDateTime.of(1999, 12, 31, 23, 59, 59, 999_000_000),
				PersonTable.INFINITY_UTC_WALL_CLOCK
			)
		);
	}
}
