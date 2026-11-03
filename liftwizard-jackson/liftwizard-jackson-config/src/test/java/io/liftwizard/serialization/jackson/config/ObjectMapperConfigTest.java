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

package io.liftwizard.serialization.jackson.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperConfigTest
{
	@Test
	void prettyPrintsObjectFieldValuesOnNextLine()
		throws JsonProcessingException
	{
		ObjectMapper objectMapper = ObjectMapperConfig.configure(new ObjectMapper());

		JsonNode jsonNode = objectMapper.readTree("{\"a\":{},\"b\":{\"c\":[{\"d\":1}]}}");
		String actualJson = objectMapper.writeValueAsString(jsonNode);

		// language=JSON
		// prettier-ignore
		var expectedJson = """
			{
				"a": {},
				"b":
				{
					"c": [
						{
							"d": 1
						}
					]
				}
			}
			""";

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	@Test
	void compactWithoutPrettyPrint()
		throws JsonProcessingException
	{
		ObjectMapper objectMapper = ObjectMapperConfig.configure(new ObjectMapper(), false);

		JsonNode jsonNode = objectMapper.readTree("{\"a\":{},\"b\":{\"c\":[{\"d\":1}]}}");
		String actualJson = objectMapper.writeValueAsString(jsonNode);

		assertThat(actualJson).isEqualTo("{\"a\":{},\"b\":{\"c\":[{\"d\":1}]}}");
	}
}
