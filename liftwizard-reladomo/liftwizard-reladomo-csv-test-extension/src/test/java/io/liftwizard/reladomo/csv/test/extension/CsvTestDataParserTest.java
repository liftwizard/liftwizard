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

package io.liftwizard.reladomo.csv.test.extension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import com.example.helloworld.core.PersonData;
import com.example.helloworld.core.PersonFinder;
import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.attribute.Attribute;
import io.liftwizard.reladomo.test.extension.ExecuteSqlExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvTestDataParserTest {

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

	@Test
	void extractsClassNameFromFilename() {
		var parser = new CsvTestDataParser("test-data/com.example.helloworld.core.Person.csv");

		assertThat(parser.getClassName()).isEqualTo("com.example.helloworld.core.Person");
	}

	@Test
	void throwsExceptionForNonCsvFile() {
		assertThatThrownBy(() -> new CsvTestDataParser("test-data/com.example.SomeClass.txt"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Filename must end with .csv");
	}

	@Test
	void throwsExceptionForMissingFile() {
		assertThatThrownBy(() -> new CsvTestDataParser("test-data/com.example.NonExistent.csv"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Could not find file");
	}

	@Test
	void parsesAttributesFromHeaders() {
		var parser = new CsvTestDataParser("test-data/com.example.helloworld.core.Person.csv");

		List<Attribute<?, ?>> attributes = parser.getAttributes();

		assertThat(attributes).containsExactly(
			PersonFinder.id(),
			PersonFinder.fullName(),
			PersonFinder.jobTitle(),
			PersonFinder.systemFrom(),
			PersonFinder.systemTo()
		);
	}

	@Test
	void createsDataObjects() {
		var parser = new CsvTestDataParser("test-data/com.example.helloworld.core.Person.csv");

		List<MithraDataObject> dataObjects = parser.getDataObjects();

		assertThat(dataObjects)
			.hasSize(2)
			.allSatisfy((obj) -> assertThat(obj).isInstanceOf(PersonData.class));
	}

	@Test
	void populatesDataObjectsWithCorrectValues() {
		var parser = new CsvTestDataParser("test-data/com.example.helloworld.core.Person.csv");
		List<MithraDataObject> dataObjects = parser.getDataObjects();

		var expectedAlice = new PersonData();
		expectedAlice.setId(1L);
		expectedAlice.setFullName("Alice Smith");
		expectedAlice.setJobTitle("Engineer");
		expectedAlice.setSystemFrom(Timestamp.from(Instant.parse("2024-01-01T00:00:00.000Z")));
		expectedAlice.setSystemTo(Timestamp.from(Instant.parse("9999-12-01T23:59:00.000Z")));

		var expectedBob = new PersonData();
		expectedBob.setId(2L);
		expectedBob.setFullName("Bob Jones");
		expectedBob.setJobTitle("Manager");
		expectedBob.setSystemFrom(Timestamp.from(Instant.parse("2024-01-15T00:00:00.000Z")));
		expectedBob.setSystemTo(Timestamp.from(Instant.parse("9999-12-01T23:59:00.000Z")));

		assertThat(dataObjects)
			.usingRecursiveFieldByFieldElementComparator()
			.containsExactly(expectedAlice, expectedBob);
	}
}
