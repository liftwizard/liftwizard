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
    private static final Logger LOGGER = LoggerFactory.getLogger(NamedDataSourceBundle.class);

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
        NamedDataSourceProvider namedDataSourceProvider = this.safeCastConfiguration(
                NamedDataSourceProvider.class,
                configuration);

        LOGGER.info("Running {}.", NamedDataSourceBundle.class.getSimpleName());

        namedDataSourceProvider.initializeDataSources(environment.metrics(), environment.lifecycle());

        LOGGER.info("Completing {}.", NamedDataSourceBundle.class.getSimpleName());
    }
}
