package com.liftwizard.dropwizard.bundle.named.data.source;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class NamedDataSourceBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER       = LoggerFactory.getLogger(NamedDataSourceBundle.class);
    private static final String ERROR_FORMAT = "Expected configuration to implement %s but found %s";

    @Override
    public int getPriority()
    {
        return -7;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(Object configuration, Environment environment)
    {
        if (!(configuration instanceof NamedDataSourceProvider))
        {
            String message = String.format(
                    ERROR_FORMAT,
                    NamedDataSourceProvider.class.getCanonicalName(),
                    configuration.getClass().getCanonicalName());
            throw new IllegalStateException(message);
        }

        LOGGER.info("Running {}.", NamedDataSourceBundle.class.getSimpleName());

        NamedDataSourceProvider namedDataSourceProvider = (NamedDataSourceProvider) configuration;
        namedDataSourceProvider.initializeDataSources(environment.metrics(), environment.lifecycle());

        LOGGER.info("Completing {}.", NamedDataSourceBundle.class.getSimpleName());
    }
}
