package com.liftwizard.dropwizard.bundle.reladomo.connection.manager.holder;

import com.google.auto.service.AutoService;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerFactoryProvider;
import com.liftwizard.reladomo.connection.manager.holder.ConnectionManagerHolder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.collections.api.map.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class ConnectionManagerHolderBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManagerHolderBundle.class);
    private static final String ERROR_FORMAT = "Expected configuration to implement %s but found %s";

    @Override
    public int getPriority()
    {
        return -4;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(Object configuration, Environment environment)
    {
        if (!(configuration instanceof ConnectionManagerFactoryProvider))
        {
            String message = String.format(
                    ERROR_FORMAT,
                    ConnectionManagerFactoryProvider.class.getCanonicalName(),
                    configuration.getClass().getCanonicalName());
            throw new IllegalStateException(message);
        }

        LOGGER.info("Running {}.", ConnectionManagerHolderBundle.class.getSimpleName());

        ConnectionManagerFactoryProvider connectionManagerFactoryProvider =
                (ConnectionManagerFactoryProvider) configuration;
        ImmutableMap<String, SourcelessConnectionManager> connectionManagersByName =
                connectionManagerFactoryProvider.getConnectionManagersByName();

        ConnectionManagerHolder.setConnectionManagersByName(connectionManagersByName);

        LOGGER.info("Completing {}.", ConnectionManagerHolderBundle.class.getSimpleName());
    }
}
