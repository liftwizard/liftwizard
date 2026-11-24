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

package io.liftwizard.graphql.instrumentation.limits;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.liftwizard.clock.incrementing.IncrementingClock;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQLLimitsInstrumentationTest
{
	private static final String SCHEMA = """
		type Query { node: Node }
		type Node { name: String, child: Node }
		""";

	private static final String QUERY = "{ node { name child { name child { name child { name } } } } }";

	@Test
	void timeoutAbortsOnceItHasPassed()
	{
		var clock = new IncrementingClock(Instant.EPOCH, ZoneOffset.UTC, Duration.ofSeconds(1));
		var instrumentation = new GraphQLExecutionTimeoutInstrumentation(clock, Duration.ofSeconds(4));

		ExecutionResult result = execute(instrumentation, QUERY);

		MutableMap<String, Object> expected = Maps.mutable.with(
			"errors",
			Lists.mutable.with(
				Maps.mutable.with(
					"message",
					"GraphQL execution exceeded the timeout of PT4S.",
					"extensions",
					Maps.mutable.with("classification", "ExecutionAborted")
				)
			)
		);
		assertThat(result.toSpecification()).containsExactlyInAnyOrderEntriesOf(expected);
	}

	@Test
	void timeoutAllowsQueryThatFinishesInTime()
	{
		var instrumentation = new GraphQLExecutionTimeoutInstrumentation(Clock.systemUTC(), Duration.ofSeconds(30));

		ExecutionResult result = execute(instrumentation, QUERY);

		MutableMap<String, Object> expected = Maps.mutable.with("data", Maps.mutable.with("node", createNodeTree()));
		assertThat(result.toSpecification()).containsExactlyInAnyOrderEntriesOf(expected);
	}

	@Test
	void depthLimitIgnoresIntrospection()
	{
		var instrumentation = new MaxDataQueryDepthInstrumentation(2);
		var query = """
			{
				__type(name: "String") { fields { type { ofType { ofType { name } } } } }
				node { name }
			}
			""";

		ExecutionResult result = execute(instrumentation, query);

		MutableMap<String, Object> expected = Maps.mutable.with(
			"data",
			Maps.mutable.with("__type", Maps.mutable.with("fields", null), "node", Maps.mutable.with("name", "root"))
		);
		assertThat(result.toSpecification()).containsExactlyInAnyOrderEntriesOf(expected);
	}

	private static ExecutionResult execute(Instrumentation instrumentation, String query)
	{
		RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
			.type("Query", (builder) -> builder.dataFetcher("node", (environment) -> createNodeTree()))
			.build();
		GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(SCHEMA), wiring);
		return GraphQL.newGraphQL(schema).instrumentation(instrumentation).build().execute(query);
	}

	private static Map<String, Object> createNodeTree()
	{
		MutableMap<String, Object> leaf = Maps.mutable.with("name", "leaf");
		MutableMap<String, Object> level3 = Maps.mutable.with("name", "level3", "child", leaf);
		MutableMap<String, Object> level2 = Maps.mutable.with("name", "level2", "child", level3);
		return Maps.mutable.with("name", "root", "child", level2);
	}
}
