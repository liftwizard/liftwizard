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

import java.util.Map;

import com.gs.fw.common.mithra.finder.Operation;
import io.liftwizard.reladomo.graphql.operation.model.ItemFinder;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphQLQueryToOperationConverterSubStringTest
{
	@RegisterExtension
	final ReladomoInitializeExtension initializeExtension = new ReladomoInitializeExtension(
		"reladomo-runtime-configuration/TestReladomoRuntimeConfiguration.xml"
	);

	@Test
	void subStringPassesStartAndEndThrough()
	{
		Map<String, Object> input = Map.of("name", Map.of("subString", Map.of("start", 1, "end", 3, "eq", "ab")));

		Operation actual = new GraphQLQueryToOperationConverter().convert(ItemFinder.getFinderInstance(), input);

		// Reladomo's Operation.equals() ignores the substring bounds, so compare the rendered operation too.
		Operation expected = ItemFinder.all().and(ItemFinder.all().and(ItemFinder.name().substring(1, 3).eq("ab")));
		assertThat(actual).isEqualTo(expected).hasToString("substring( Item.name , 1 , 3 ) = \"ab\"");
	}

	@Test
	void subStringChainsWithLower()
	{
		Map<String, Object> input = Map.of(
			"name",
			Map.of("lower", Map.of("subString", Map.of("start", 0, "end", 3, "lower", Map.of("eq", "abc"))))
		);

		Operation actual = new GraphQLQueryToOperationConverter().convert(ItemFinder.getFinderInstance(), input);

		assertThat(actual).hasToString("lower( substring( lower( Item.name ) , 0 , 3 ) ) = \"abc\"");
	}

	@Test
	void subStringWithoutStartAndEndThrows()
	{
		Map<String, Object> input = Map.of("name", Map.of("subString", Map.of("eq", "ab")));

		assertThatThrownBy(() -> new GraphQLQueryToOperationConverter().convert(ItemFinder.getFinderInstance(), input))
			.isInstanceOf(LiftwizardGraphQLContextException.class)
			.hasMessageStartingWith("subString requires start and end but found [eq]");
	}

	@Test
	void subStringWithoutOperatorsThrows()
	{
		Map<String, Object> input = Map.of("name", Map.of("subString", Map.of("start", 1, "end", 3)));

		assertThatThrownBy(() -> new GraphQLQueryToOperationConverter().convert(ItemFinder.getFinderInstance(), input))
			.isInstanceOf(LiftwizardGraphQLContextException.class)
			.hasMessageStartingWith("subString requires at least one operator besides start and end");
	}
}
