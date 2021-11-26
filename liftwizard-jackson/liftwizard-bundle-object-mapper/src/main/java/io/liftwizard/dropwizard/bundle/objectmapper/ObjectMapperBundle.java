/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.dropwizard.bundle.objectmapper;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactory;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactoryProvider;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the Jackson {@link ObjectMapper} used by Dropwizard for serializing and deserializing all responses, as well as for logging by bundles such as liftwizard-bundle-logging-config.
 *
 * <p>
 * Supports configuring pretty-printing on or off, and serialization inclusion to any value in Jackson's {@link Include}.
 *
 * <p>
 * Also turns on all json5 features, turns on {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES}, turns on {@link Feature#STRICT_DUPLICATE_DETECTION}, and turns on serialization of dates and Strings.
 *
 * @see <a href="https://liftwizard.io/docs/jackson/ObjectMapperBundle#objectmapperbundle">https://liftwizard.io/docs/jackson/ObjectMapperBundle#objectmapperbundle</a>
 */
@AutoService(PrioritizedBundle.class)
public class ObjectMapperBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperBundle.class);

    @Override
    public int getPriority()
    {
        return -10;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        ObjectMapperFactoryProvider objectMapperFactoryProvider =
                this.safeCastConfiguration(ObjectMapperFactoryProvider.class, configuration);
        ObjectMapperFactory objectMapperFactory = objectMapperFactoryProvider.getObjectMapperFactory();
        ObjectMapper        objectMapper        = environment.getObjectMapper();

        this.configureObjectMapper(objectMapperFactory, objectMapper);
    }

    public ObjectMapper configureObjectMapper()
    {
        return this.configureObjectMapper(new ObjectMapperFactory(), Jackson.newObjectMapper());
    }

    public ObjectMapper configureObjectMapper(ObjectMapperFactory objectMapperFactory, ObjectMapper objectMapper)
    {
        if (!objectMapperFactory.isEnabled())
        {
            LOGGER.info("{} disabled.", this.getClass().getSimpleName());
            return objectMapper;
        }

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        ObjectMapperConfig.configure(
                objectMapper,
                objectMapperFactory.isPrettyPrint(),
                objectMapperFactory.getFailOnUnknownProperties(),
                objectMapperFactory.getSerializationInclusion(),
                objectMapperFactory.getDefaultNullSetterInfo());

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());

        return objectMapper;
    }
}
