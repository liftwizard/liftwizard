/*
 * Copyright 2023 Craig Motlin
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPrettyPrinterTest
{
    private final ObjectMapper mapper = JsonPrettyPrinterTest.getObjectMapper();

    @Nonnull
    private static ObjectMapper getObjectMapper()
    {
        JsonPrettyPrinter jsonPrettyPrinter = new JsonPrettyPrinter();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDefaultPrettyPrinter(jsonPrettyPrinter);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    @Test
    public void smokeTest()
            throws JsonProcessingException
    {
        Map<String, List<String>> map = new HashMap<>();
        map.put("a", List.of("b", "c"));
        map.put("d", List.of("e", "f"));

        String actualJson = this.mapper.writeValueAsString(map);

        //language=JSON
        String expectedJson = """
                {
                  "a": [
                    "b",
                    "c"
                  ],
                  "d": [
                    "e",
                    "f"
                  ]
                }""";

        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @Test
    public void emptyArray()
            throws JsonProcessingException
    {
        List<String> emptyList    = List.of();
        String       actualJson   = this.mapper.writeValueAsString(emptyList);
        String       expectedJson = "[]";
        assertThat(actualJson).isEqualTo(expectedJson);
    }
}
