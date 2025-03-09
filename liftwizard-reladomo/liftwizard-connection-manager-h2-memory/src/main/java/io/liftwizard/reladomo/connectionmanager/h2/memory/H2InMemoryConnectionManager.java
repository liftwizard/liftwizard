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

package io.liftwizard.reladomo.connectionmanager.h2.memory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.databasetype.H2DatabaseType;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public final class H2InMemoryConnectionManager
        implements SourcelessConnectionManager
{
    private static final H2InMemoryConnectionManager INSTANCE = new H2InMemoryConnectionManager();

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    private static final String SCHEMA_NAME = "liftwizard-app-h2";
    private static final DataSource DATA_SOURCE = H2InMemoryConnectionManager.createDataSource();

    private H2InMemoryConnectionManager()
    {
        // singleton
    }

    @Nonnull
    private static DataSource createDataSource()
    {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        poolProperties.setUrl("jdbc:p6spy:h2:mem:");
        poolProperties.setUsername("sa");
        poolProperties.setPassword("");
        poolProperties.setInitialSize(1);
        poolProperties.setMaxActive(10);
        poolProperties.setMaxWait(500);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setTestOnReturn(false);
        poolProperties.setTestWhileIdle(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setValidationInterval(30000);
        poolProperties.setTimeBetweenEvictionRunsMillis(30000);
        poolProperties.setMinEvictableIdleTimeMillis(60000);
        poolProperties.setName("Reladomo default connection pool");

        DataSource dataSource = new DataSource();
        dataSource.setPoolProperties(poolProperties);

        return dataSource;
    }

    @Nonnull
    @SuppressWarnings("unused")
    public static H2InMemoryConnectionManager getInstance()
    {
        return INSTANCE;
    }

    @Nonnull
    @Override
    public BulkLoader createBulkLoader()
    {
        throw new RuntimeException("BulkLoader is not supported");
    }

    @Override
    public Connection getConnection()
    {
        try
        {
            return DATA_SOURCE.getConnection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Could not obtain database connection", e);
        }
    }

    @Override
    public DatabaseType getDatabaseType()
    {
        return H2DatabaseType.getInstance();
    }

    @Override
    public TimeZone getDatabaseTimeZone()
    {
        return TIME_ZONE;
    }

    @Override
    public String getDatabaseIdentifier()
    {
        return SCHEMA_NAME;
    }
}
