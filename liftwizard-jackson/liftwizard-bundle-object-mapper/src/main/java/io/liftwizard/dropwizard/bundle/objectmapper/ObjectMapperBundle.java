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

package io.liftwizard.dropwizard.bundle.objectmapper;

import javax.annotation.Nonnull;

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

@AutoService(PrioritizedBundle.class)
public class ObjectMapperBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperBundle.class);

    @Override
    public int getPriority()
    {
        return -9;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        ObjectMapperFactoryProvider objectMapperFactoryProvider =
                this.safeCastConfiguration(ObjectMapperFactoryProvider.class, configuration);
        ObjectMapperFactory objectMapperFactory = objectMapperFactoryProvider.getObjectMapperFactory();
        ObjectMapper        objectMapper        = environment.getObjectMapper();

        ObjectMapperBundle.configureObjectMapper(objectMapperFactory, objectMapper);
    }

    public static ObjectMapper configureObjectMapper()
    {
        return ObjectMapperBundle.configureObjectMapper(new ObjectMapperFactory(), Jackson.newObjectMapper());
    }

    public static ObjectMapper configureObjectMapper(ObjectMapperFactory objectMapperFactory, ObjectMapper objectMapper)
    {
        if (!objectMapperFactory.isEnabled())
        {
            LOGGER.info("{} disabled.", ObjectMapperBundle.class.getSimpleName());
            return objectMapper;
        }

        LOGGER.info("Running {}.", ObjectMapperBundle.class.getSimpleName());

        ObjectMapperConfig.configure(
                objectMapper,
                objectMapperFactory.isPrettyPrint(),
                objectMapperFactory.getSerializationInclusion());

        LOGGER.info("Completing {}.", ObjectMapperBundle.class.getSimpleName());

        return objectMapper;
    }
}
