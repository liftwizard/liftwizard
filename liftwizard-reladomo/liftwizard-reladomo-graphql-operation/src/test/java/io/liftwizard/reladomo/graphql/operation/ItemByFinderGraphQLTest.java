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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.fw.common.mithra.finder.Operation;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.liftwizard.graphql.scalar.ScalarRuntimeWiringBuilder;
import io.liftwizard.junit.extension.match.FileSlurper;
import io.liftwizard.junit.extension.match.json.JsonMatchExtension;
import io.liftwizard.reladomo.graphql.operation.model.ItemFinder;
import io.liftwizard.reladomo.graphql.operation.model.ItemList;
import io.liftwizard.reladomo.test.extension.ExecuteSqlExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Executes real GraphQL queries against a schema built from {@code ReladomoAttribute.graphqls}, converts the
 * {@code operation} argument with {@link GraphQLQueryToOperationConverter}, and runs the resulting operation on H2.
 */
class ItemByFinderGraphQLTest
{
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
	final ReladomoLoadDataExtension loadDataExtension = new ReladomoLoadDataExtension("test-data/item-test-data.txt");

	@RegisterExtension
	final JsonMatchExtension jsonMatchExtension = new JsonMatchExtension(this.getClass());

	private final GraphQL graphQL = GraphQL.newGraphQL(buildSchema()).build();

	@Test
	void subString(TestInfo testInfo)
	{
		this.assertQueryResult(testInfo);
	}

	@Test
	void subStringWithoutOperator(TestInfo testInfo)
	{
		this.assertQueryResult(testInfo);
	}

	@Test
	void subStringWithoutStart(TestInfo testInfo)
	{
		this.assertQueryResult(testInfo);
	}

	private void assertQueryResult(TestInfo testInfo)
	{
		String testName = testInfo.getTestMethod().orElseThrow().getName();
		String baseName = this.getClass().getSimpleName() + "." + testName;
		String query = FileSlurper.slurp(baseName + ".graphql", this.getClass());

		ExecutionResult executionResult = this.graphQL.execute(query);
		String actualJson = toJson(executionResult.toSpecification());

		this.jsonMatchExtension.assertFileContents(baseName + ".json", actualJson);
	}

	private static GraphQLSchema buildSchema()
	{
		var schemaParser = new SchemaParser();
		TypeDefinitionRegistry registry = schemaParser.parse(
			FileSlurper.slurp(
				"/io/liftwizard/graphql/schema/attribute/ReladomoAttribute.graphqls",
				ItemByFinderGraphQLTest.class
			)
		);
		registry.merge(schemaParser.parse(FileSlurper.slurp("ItemQuery.graphqls", ItemByFinderGraphQLTest.class)));

		RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
		new ScalarRuntimeWiringBuilder().accept(runtimeWiringBuilder);
		runtimeWiringBuilder.type("Query", (builder) ->
			builder.dataFetcher("itemByFinder", (environment) -> findItems(environment.getArgument("operation")))
		);

		return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiringBuilder.build());
	}

	private static ItemList findItems(Map<String, Object> operationArgument)
	{
		Operation operation = new GraphQLQueryToOperationConverter().convert(
			ItemFinder.getFinderInstance(),
			operationArgument
		);
		ItemList items = ItemFinder.findMany(operation);
		items.setOrderBy(ItemFinder.id().ascendingOrderBy());
		return items;
	}

	private static String toJson(Map<String, Object> specification)
	{
		try
		{
			return OBJECT_MAPPER.writeValueAsString(specification);
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
