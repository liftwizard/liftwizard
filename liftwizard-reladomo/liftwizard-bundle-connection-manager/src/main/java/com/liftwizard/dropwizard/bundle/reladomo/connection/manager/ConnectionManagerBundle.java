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
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManagerBundle.class);

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
        NamedDataSourceProvider namedDataSourceProvider = this.safeCastConfiguration(
                NamedDataSourceProvider.class,
                configuration);
        ConnectionManagerFactoryProvider connectionManagerFactoryProvider = this.safeCastConfiguration(
                ConnectionManagerFactoryProvider.class,
                configuration);

        LOGGER.info("Running {}.", ConnectionManagerBundle.class.getSimpleName());

        MapIterable<String, ManagedDataSource> dataSourcesByName = namedDataSourceProvider.getDataSourcesByName();

        connectionManagerFactoryProvider.initializeConnectionManagers(dataSourcesByName);

        LOGGER.info("Completing {}.", ConnectionManagerBundle.class.getSimpleName());
    }
}
