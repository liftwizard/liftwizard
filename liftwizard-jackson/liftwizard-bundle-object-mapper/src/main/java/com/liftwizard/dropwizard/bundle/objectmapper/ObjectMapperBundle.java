package com.liftwizard.dropwizard.bundle.objectmapper;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactory;
import com.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactoryProvider;
import com.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
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
