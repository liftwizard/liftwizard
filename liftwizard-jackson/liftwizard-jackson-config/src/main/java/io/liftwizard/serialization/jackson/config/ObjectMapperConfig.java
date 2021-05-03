/*
 * Copyright 2020 Craig Motlin
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

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule;
import io.liftwizard.serialization.jackson.pretty.JsonPrettyPrinter;

public final class ObjectMapperConfig
{
    private static final PrettyPrinter DEFAULT_PRETTY_PRINTER = new JsonPrettyPrinter();

    private ObjectMapperConfig()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static void configure(
            @Nonnull ObjectMapper objectMapper,
            boolean prettyPrint,
            @Nonnull Include serializationInclusion)
    {
        if (prettyPrint)
        {
            objectMapper.setDefaultPrettyPrinter(DEFAULT_PRETTY_PRINTER);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        // Default behavior in Dropwizard 1.2.x
        // Necessary in Dropwizard 2.x
        objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(Feature.STRICT_DUPLICATE_DETECTION);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        objectMapper.enable(Feature.ALLOW_COMMENTS);
        objectMapper.enable(Feature.ALLOW_YAML_COMMENTS);
        objectMapper.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        objectMapper.enable(Feature.ALLOW_TRAILING_COMMA);

        objectMapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        objectMapper.enable(Feature.ALLOW_SINGLE_QUOTES);
        objectMapper.enable(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        objectMapper.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);

        objectMapper.configure(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL, true);

        objectMapper.registerModule(new EclipseCollectionsModule());

        objectMapper.setDateFormat(new StdDateFormat());
        objectMapper.setSerializationInclusion(serializationInclusion);
    }
}
