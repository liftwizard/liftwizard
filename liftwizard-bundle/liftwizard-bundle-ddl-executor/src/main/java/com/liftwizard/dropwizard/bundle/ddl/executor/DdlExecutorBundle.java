package com.liftwizard.dropwizard.bundle.ddl.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import com.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactory;
import com.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactoryProvider;
import com.liftwizard.reladomo.ddl.executor.DatabaseDdlExecutor;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class DdlExecutorBundle implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlExecutorBundle.class);

    private static final String ERROR_FORMAT = "Expected configuration to implement %s but found %s";

    @Override
    public int getPriority()
    {
        return -6;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(Object configuration, Environment environment) throws SQLException
    {
        if (!(configuration instanceof DdlExecutorFactoryProvider))
        {
            String message = String.format(
                    ERROR_FORMAT,
                    DdlExecutorFactoryProvider.class.getCanonicalName(),
                    configuration.getClass().getCanonicalName());
            throw new IllegalStateException(message);
        }

        if (!(configuration instanceof NamedDataSourceProvider))
        {
            String message = String.format(
                    ERROR_FORMAT,
                    NamedDataSourceProvider.class.getCanonicalName(),
                    configuration.getClass().getCanonicalName());
            throw new IllegalStateException(message);
        }

        DdlExecutorFactoryProvider ddlExecutorFactoryProvider = (DdlExecutorFactoryProvider) configuration;
        List<DdlExecutorFactory>   ddlExecutorFactories       = ddlExecutorFactoryProvider.getDdlExecutorFactories();

        if (ddlExecutorFactories.isEmpty())
        {
            LOGGER.info("{} disabled.", DdlExecutorBundle.class.getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", DdlExecutorBundle.class.getSimpleName());

        NamedDataSourceProvider dataSourceProvider = (NamedDataSourceProvider) configuration;
        for (DdlExecutorFactory ddlExecutorFactory : ddlExecutorFactories)
        {
            String dataSourceName     = ddlExecutorFactory.getDataSourceName();
            String ddlLocationPattern = ddlExecutorFactory.getDdlLocationPattern();
            String idxLocationPattern = ddlExecutorFactory.getIdxLocationPattern();

            LOGGER.info("Running {} with data source {}.", DdlExecutorBundle.class.getSimpleName(), dataSourceName);

            DataSource dataSource = dataSourceProvider.getDataSourceByName(dataSourceName);
            Objects.requireNonNull(dataSource, dataSourceName);
            try (Connection connection = dataSource.getConnection())
            {
                DatabaseDdlExecutor.executeSql(connection, ddlLocationPattern, idxLocationPattern);
            }
        }

        LOGGER.info("Completing {}.", DdlExecutorBundle.class.getSimpleName());
    }
}
