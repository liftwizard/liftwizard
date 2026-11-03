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

package io.liftwizard.graphql.schema.attribute;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReladomoAttributeSchemaTest
{
	@Test
	void subStringAttributeHasStartEndAndEveryStringAttributeOperator()
		throws Exception
	{
		TypeDefinitionRegistry registry = parseSchema();

		List<String> stringFields = fieldNames(registry, "_StringAttribute");
		List<String> subStringFields = fieldNames(registry, "_SubStringAttribute");

		assertThat(subStringFields).startsWith("start", "end");
		assertThat(subStringFields.subList(2, subStringFields.size())).containsExactlyElementsOf(stringFields);
	}

	private static TypeDefinitionRegistry parseSchema()
		throws Exception
	{
		try (
			InputStream inputStream = Objects.requireNonNull(
				ReladomoAttributeSchemaTest.class.getResourceAsStream("ReladomoAttribute.graphqls")
			);
			Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
		)
		{
			return new SchemaParser().parse(reader);
		}
	}

	private static List<String> fieldNames(TypeDefinitionRegistry registry, String typeName)
	{
		return registry
			.getType(typeName, InputObjectTypeDefinition.class)
			.orElseThrow()
			.getInputValueDefinitions()
			.stream()
			.map(InputValueDefinition::getName)
			.toList();
	}
}
