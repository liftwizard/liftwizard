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

package io.liftwizard.serialization.jackson.pretty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPrettyPrinterTest
{
	private final ObjectMapper mapper = JsonPrettyPrinterTest.getObjectMapper();

	@Nonnull
	private static ObjectMapper getObjectMapper()
	{
		PrettyPrinter jsonPrettyPrinter = new JsonPrettyPrinter();

		var objectMapper = new ObjectMapper();
		objectMapper.setDefaultPrettyPrinter(jsonPrettyPrinter);
		objectMapper.setSerializerProvider(new JsonPrettyPrinterSerializerProvider());
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		return objectMapper;
	}

	@Test
	void smokeTest()
		throws JsonProcessingException
	{
		Map<String, List<String>> map = new LinkedHashMap<>();
		map.put("a", List.of("b", "c"));
		map.put("d", List.of("e", "f"));

		String actualJson = this.mapper.writeValueAsString(map);

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"a": [
					"b",
					"c"
				],
				"d": [
					"e",
					"f"
				]
			}
			""";

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void emptyArray()
		throws JsonProcessingException
	{
		List<String> emptyList = List.of();
		String actualJson = this.mapper.writeValueAsString(emptyList);
		var expectedJson = "[]\n";
		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void emptyObject()
		throws JsonProcessingException
	{
		Map<String, Object> emptyMap = Map.of();
		String actualJson = this.mapper.writeValueAsString(emptyMap);
		var expectedJson = "{}\n";
		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void nonEmptyObjectFieldValueOnNextLine()
		throws JsonProcessingException
	{
		// language=JSON
		var compactJson = """
		{"a": {}, "b": {"c": 1, "g": {"h": [1]}}, "d": [{"e": {}}, []], "f": []}
		""";

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"a": {},
				"b":
				{
					"c": 1,
					"g":
					{
						"h": [
							1
						]
					}
				},
				"d": [
					{
						"e": {}
					},
					[]
				],
				"f": []
			}
			""";

		this.assertPrettyPrints(compactJson, expectedJson);
	}

	@Test
	void nestedObjectsInArraysInObjects()
		throws JsonProcessingException
	{
		// language=JSON
		var compactJson = """
		{"a": [{"b": {"c": [{"d": {"e": true}}]}}]}
		""";

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"a": [
					{
						"b":
						{
							"c": [
								{
									"d":
									{
										"e": true
									}
								}
							]
						}
					}
				]
			}
			""";

		this.assertPrettyPrints(compactJson, expectedJson);
	}

	@Test
	void emptyObjectAsFieldValue()
		throws JsonProcessingException
	{
		// language=JSON
		var compactJson = """
		{"a": {}, "b": {"c": {}}}
		""";

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"a": {},
				"b":
				{
					"c": {}
				}
			}
			""";

		this.assertPrettyPrints(compactJson, expectedJson);
	}

	@Test
	void emptyObjectInArray()
		throws JsonProcessingException
	{
		// language=JSON
		var compactJson = """
		[{}, {"a": {}}]
		""";

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			[
				{},
				{
					"a": {}
				}
			]
			""";

		this.assertPrettyPrints(compactJson, expectedJson);
	}

	@Test
	void rootArray()
		throws JsonProcessingException
	{
		// language=JSON
		var compactJson = """
		[{"a": {"b": null}}, 1, "x", []]
		""";

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			[
				{
					"a":
					{
						"b": null
					}
				},
				1,
				"x",
				[]
			]
			""";

		this.assertPrettyPrints(compactJson, expectedJson);
	}

	@Test
	void stringValueContainingObjectSeparator()
		throws JsonProcessingException
	{
		// language=JSON
		var compactJson = """
		{"\\": {": "\\": {", "b": {"c": "\\": {"}}
		""";

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"\\": {": "\\": {",
				"b":
				{
					"c": "\\": {"
				}
			}
			""";

		this.assertPrettyPrints(compactJson, expectedJson);
	}

	@Test
	void pojosUseNextLineObjects()
		throws JsonProcessingException
	{
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("pet", new Pet(new Dog("Rex", new Owner("Ann"))));
		map.put("empty", Map.of());
		map.put("list", List.of(new Owner("Bob")));

		String actualJson = this.mapper.writeValueAsString(map);

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"pet":
				{
					"animal":
					{
						"@type": "Dog",
						"name": "Rex",
						"owner":
						{
							"name": "Ann"
						}
					}
				},
				"empty": {},
				"list": [
					{
						"name": "Bob"
					}
				]
			}
			""";

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void writerWithDefaultPrettyPrinter()
		throws JsonProcessingException
	{
		var objectMapper = new ObjectMapper();
		objectMapper.setDefaultPrettyPrinter(new JsonPrettyPrinter());
		objectMapper.setSerializerProvider(new JsonPrettyPrinterSerializerProvider());

		JsonNode jsonNode = objectMapper.readTree("{\"a\":{\"b\":1}}");
		String actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"a":
				{
					"b": 1
				}
			}
			""";

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void createInstanceReturnsFreshPrinter()
	{
		var jsonPrettyPrinter = new JsonPrettyPrinter();
		assertThat(jsonPrettyPrinter.createInstance())
			.isInstanceOf(JsonPrettyPrinter.class)
			.isNotSameAs(jsonPrettyPrinter)
			.isNotSameAs(jsonPrettyPrinter.createInstance());
	}

	private void assertPrettyPrints(String compactJson, String expectedJson)
		throws JsonProcessingException
	{
		JsonNode jsonNode = this.mapper.readTree(compactJson);
		String actualJson = this.mapper.writeValueAsString(jsonNode);
		assertThat(actualJson).isEqualTo(expectedJson);
	}

	private record Pet(Animal animal)
	{
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME)
	private interface Animal
	{
	}

	private record Dog(String name, Owner owner)
		implements Animal
	{
	}

	private record Owner(String name)
	{
	}
}
