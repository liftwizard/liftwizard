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

package io.liftwizard.reladomo.graphql.operation;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.reladomo.graphql.operation.model.ItemFinder;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQLQueryToOperationConverterTemporalInTest
{
	private static final Timestamp DATE_1 = Timestamp.valueOf(LocalDate.parse("2024-01-01").atStartOfDay());
	private static final Timestamp DATE_2 = Timestamp.valueOf(LocalDate.parse("2024-02-01").atStartOfDay());
	private static final Timestamp TIMESTAMP_1 = Timestamp.from(Instant.parse("2024-01-01T12:00:00Z"));
	private static final Timestamp TIMESTAMP_2 = Timestamp.from(Instant.parse("2024-02-01T12:00:00Z"));

	@RegisterExtension
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/TestReladomoRuntimeConfiguration.xml"
	);

	@Test
	void dateInParsesEachValue()
	{
		Operation actual = convert("createdOn", "in", List.of("2024-01-01", "2024-02-01"));

		Operation expected = ItemFinder.all().and(
			ItemFinder.all().and(ItemFinder.createdOn().in(Set.of(DATE_1, DATE_2)))
		);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void dateNotInParsesEachValue()
	{
		Operation actual = convert("createdOn", "notIn", List.of("2024-01-01", "2024-02-01"));

		Operation expected = ItemFinder.all().and(
			ItemFinder.all().and(ItemFinder.createdOn().notIn(Set.of(DATE_1, DATE_2)))
		);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void timestampInParsesEachValue()
	{
		Operation actual = convert("updatedAt", "in", List.of("2024-01-01T12:00:00Z", "2024-02-01T12:00:00Z"));

		Operation expected = ItemFinder.all().and(
			ItemFinder.all().and(ItemFinder.updatedAt().in(Set.of(TIMESTAMP_1, TIMESTAMP_2)))
		);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void timestampNotInParsesEachValue()
	{
		Operation actual = convert("updatedAt", "notIn", List.of("2024-01-01T12:00:00Z", "2024-02-01T12:00:00Z"));

		Operation expected = ItemFinder.all().and(
			ItemFinder.all().and(ItemFinder.updatedAt().notIn(Set.of(TIMESTAMP_1, TIMESTAMP_2)))
		);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void dateInKeepsNullValues()
	{
		Operation actual = convert("createdOn", "in", Arrays.asList("2024-01-01", null));

		Operation expected = ItemFinder.all().and(
			ItemFinder.all().and(ItemFinder.createdOn().in(new LinkedHashSet<>(Arrays.asList(DATE_1, null))))
		);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void timestampInKeepsNullValues()
	{
		Operation actual = convert("updatedAt", "in", Arrays.asList("2024-01-01T12:00:00Z", null));

		Operation expected = ItemFinder.all().and(
			ItemFinder.all().and(ItemFinder.updatedAt().in(new LinkedHashSet<>(Arrays.asList(TIMESTAMP_1, null))))
		);
		assertThat(actual).isEqualTo(expected);
	}

	private static Operation convert(String attributeName, String operationName, List<String> values)
	{
		Map<String, Object> input = Map.of(attributeName, Map.of(operationName, values));
		return new GraphQLQueryToOperationConverter().convert(ItemFinder.getFinderInstance(), input);
	}
}
