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

package io.liftwizard.dropwizard.configuration.factory;

import javax.validation.Validator;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.JsonConfigurationFactory;

/**
 * Allows configuring Dropwizard using json.
 * Based on {@link DefaultConfigurationFactoryFactory} but replacing yml with json.
 *
 * @see DefaultConfigurationFactoryFactory
 * @see <a href="https://liftwizard.io/docs/configuration/json5-configuration#configuration-through-json5-instead-of-yaml">https://liftwizard.io/docs/configuration/json5-configuration#configuration-through-json5-instead-of-yaml</a>
 */
public class JsonConfigurationFactoryFactory<T>
        implements ConfigurationFactoryFactory<T>
{
    @Override
    public ConfigurationFactory<T> create(
            Class<T> aClass,
            Validator validator,
            ObjectMapper objectMapper,
            String propertyPrefix)
    {
        ObjectMapper strictObjectMapper = this.getStrictObjectMapper(objectMapper);
        return new JsonConfigurationFactory<>(
                aClass,
                validator,
                strictObjectMapper,
                propertyPrefix);
    }

    private ObjectMapper getStrictObjectMapper(ObjectMapper objectMapper)
    {
        ObjectMapper strictObjectMapper = objectMapper.copy();
        strictObjectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // strictObjectMapper.enable(Feature.STRICT_DUPLICATE_DETECTION);

        strictObjectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        strictObjectMapper.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature());
        strictObjectMapper.enable(JsonReadFeature.ALLOW_YAML_COMMENTS.mappedFeature());
        strictObjectMapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());
        strictObjectMapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());

        strictObjectMapper.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature());
        strictObjectMapper.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature());
        strictObjectMapper.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature());
        strictObjectMapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());

        strictObjectMapper.setDateFormat(new StdDateFormat());

        return strictObjectMapper;
    }
}
