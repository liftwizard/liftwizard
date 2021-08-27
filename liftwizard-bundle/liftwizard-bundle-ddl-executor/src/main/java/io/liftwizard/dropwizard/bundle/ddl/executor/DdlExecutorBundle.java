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

package io.liftwizard.dropwizard.bundle.ddl.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.google.auto.service.AutoService;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import io.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactory;
import io.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactoryProvider;
import io.liftwizard.reladomo.ddl.executor.DatabaseDdlExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class DdlExecutorBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlExecutorBundle.class);

    @Override
    public int getPriority()
    {
        return -6;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment) throws SQLException
    {
        DdlExecutorFactoryProvider ddlExecutorFactoryProvider = this.safeCastConfiguration(
                DdlExecutorFactoryProvider.class,
                configuration);
        NamedDataSourceProvider dataSourceProvider = this.safeCastConfiguration(
                NamedDataSourceProvider.class,
                configuration);

        List<DdlExecutorFactory> ddlExecutorFactories = ddlExecutorFactoryProvider.getDdlExecutorFactories();

        if (ddlExecutorFactories.isEmpty())
        {
            LOGGER.info("{} disabled.", this.getClass().getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        for (DdlExecutorFactory ddlExecutorFactory : ddlExecutorFactories)
        {
            String dataSourceName     = ddlExecutorFactory.getDataSourceName();
            String ddlLocationPattern = ddlExecutorFactory.getDdlLocationPattern();
            String idxLocationPattern = ddlExecutorFactory.getIdxLocationPattern();

            LOGGER.info("Running {} with data source '{}'.", this.getClass().getSimpleName(), dataSourceName);

            DataSource dataSource = dataSourceProvider.getDataSourceByName(dataSourceName);
            Objects.requireNonNull(dataSource, dataSourceName);
            try (Connection connection = dataSource.getConnection())
            {
                DatabaseDdlExecutor.executeSql(connection, ddlLocationPattern, idxLocationPattern);
            }
        }

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }
}
