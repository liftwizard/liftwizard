package com.liftwizard.dropwizard.bundle.reladomo.connection.manager;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerFactoryProvider;
import com.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.collections.api.map.MapIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class ConnectionManagerBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER       = LoggerFactory.getLogger(ConnectionManagerBundle.class);
    private static final String ERROR_FORMAT = "Expected configuration to implement %s but found %s";

    @Override
    public int getPriority()
    {
        return -5;
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

        if (!(configuration instanceof ConnectionManagerFactoryProvider))
        {
            String message = String.format(
                    ERROR_FORMAT,
                    ConnectionManagerFactoryProvider.class.getCanonicalName(),
                    configuration.getClass().getCanonicalName());
            throw new IllegalStateException(message);
        }

        LOGGER.info("Running {}.", ConnectionManagerBundle.class.getSimpleName());

        NamedDataSourceProvider namedDataSourceProvider = (NamedDataSourceProvider) configuration;
        MapIterable<String, ManagedDataSource> dataSourcesByName = namedDataSourceProvider.getDataSourcesByName();

        ConnectionManagerFactoryProvider connectionManagerFactoryProvider =
                (ConnectionManagerFactoryProvider) configuration;
        connectionManagerFactoryProvider.initializeConnectionManagers(dataSourcesByName);

        LOGGER.info("Completing {}.", ConnectionManagerBundle.class.getSimpleName());
    }
}
