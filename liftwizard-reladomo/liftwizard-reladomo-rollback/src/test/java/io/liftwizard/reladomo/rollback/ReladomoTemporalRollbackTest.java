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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.example.helloworld.core.Person;
import com.example.helloworld.core.PersonFinder;
import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraManagerProvider;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.reladomo.test.extension.ExecuteSqlExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import io.liftwizard.reladomo.test.extension.ReladomoTestFile;
import io.liftwizard.reladomo.utc.infinity.timestamp.UtcInfinityTimestamp;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class ReladomoTemporalRollbackTest {

	private static final Instant INFINITY = UtcInfinityTimestamp.getDefaultInfinityInstant();

	private static final Instant ROLLBACK_DATE = LocalDateTime.of(2024, 6, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);

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
	final ReladomoLoadDataExtension loadDataExtension = new ReladomoLoadDataExtension();

	@RegisterExtension
	@Order(5)
	final LogMarkerTestExtension logMarkerExtension = new LogMarkerTestExtension();

	@Test
	@ReladomoTestFile("test-data/rollback-test-data.txt")
	void rollbackPurgesFutureVersionsAndRestoresSupersededVersions() {
		// Verify initial state using edge point to see all versions
		MithraList<Person> allPersonsBefore = PersonFinder.findMany(PersonFinder.system().equalsEdgePoint());
		assertThat(allPersonsBefore).hasSize(3);

		// Perform rollback to June 1, 2024
		ReladomoTemporalRollback rollback = new ReladomoTemporalRollback(ROLLBACK_DATE, INFINITY);
		rollback.rollbackAllTables();

		// Clear cache to ensure we read from database
		MithraManagerProvider.getMithraManager().clearAllQueryCaches();

		// Verify state after rollback
		MithraList<Person> allPersonsAfter = PersonFinder.findMany(PersonFinder.system().equalsEdgePoint());

		// Should have 2 persons: Alice and Bob (Charlie was created after rollback date)
		assertThat(allPersonsAfter).hasSize(2);

		// Alice should still be active (unchanged)
		Person alice = PersonFinder.findOne(PersonFinder.id().eq(1L).and(PersonFinder.system().equalsEdgePoint()));
		assertThat(alice).isNotNull();
		assertThat(alice.getFullName()).isEqualTo("Alice");
		assertThat(alice.getSystemTo().toInstant().atZone(ZoneOffset.UTC).getYear()).isEqualTo(9999);

		// Bob should now be active (system_to restored to infinity)
		Person bob = PersonFinder.findOne(PersonFinder.id().eq(2L).and(PersonFinder.system().equalsEdgePoint()));
		assertThat(bob).isNotNull();
		assertThat(bob.getFullName()).isEqualTo("Bob");
		// Bob's system_to should now be infinity (restored from 2024-06-15)
		assertThat(bob.getSystemTo().toInstant().atZone(ZoneOffset.UTC).getYear()).isEqualTo(9999);

		// Charlie should not exist (purged because created after rollback date)
		Person charlie = PersonFinder.findOne(PersonFinder.id().eq(3L).and(PersonFinder.system().equalsEdgePoint()));
		assertThat(charlie).isNull();
	}

	@Test
	@ReladomoTestFile("test-data/rollback-test-data.txt")
	void rollbackWithNoChangesNeeded() {
		// Rollback to a date in the future - nothing should change
		Instant futureDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);

		MithraList<Person> allPersonsBefore = PersonFinder.findMany(PersonFinder.system().equalsEdgePoint());
		int countBefore = allPersonsBefore.size();

		ReladomoTemporalRollback rollback = new ReladomoTemporalRollback(futureDate, INFINITY);
		rollback.rollbackAllTables();

		MithraManagerProvider.getMithraManager().clearAllQueryCaches();

		MithraList<Person> allPersonsAfter = PersonFinder.findMany(PersonFinder.system().equalsEdgePoint());
		assertThat(allPersonsAfter).hasSize(countBefore);
	}
}
