package com.liftwizard.reladomo.connectionmanager.h2.memory;

import java.sql.Connection;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.connectionmanager.XAConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.databasetype.H2DatabaseType;

public final class H2InMemoryConnectionManager
        implements SourcelessConnectionManager
{
    private static final H2InMemoryConnectionManager INSTANCE = new H2InMemoryConnectionManager();

    private static final TimeZone            TIME_ZONE             = TimeZone.getTimeZone("UTC");
    private static final String              SCHEMA_NAME           = "liftwizard-app-h2";
    private static final XAConnectionManager XA_CONNECTION_MANAGER =
            H2InMemoryConnectionManager.createXaConnectionManager();

    private H2InMemoryConnectionManager()
    {
        // singleton
    }

    @Nonnull
    private static XAConnectionManager createXaConnectionManager()
    {
        // TODO: Consider using org.apache.tomcat.jdbc.pool.DataSourceProxy and org.apache.tomcat.jdbc.pool.PoolProperties instead
        XAConnectionManager connectionManager = new XAConnectionManager();
        connectionManager.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
        connectionManager.setMaxWait(500);
        connectionManager.setJdbcConnectionString("jdbc:p6spy:h2:mem:");
        connectionManager.setJdbcUser("sa");
        connectionManager.setJdbcPassword("");
        connectionManager.setPoolName("Reladomo default connection pool");
        connectionManager.setInitialSize(1);
        connectionManager.setPoolSize(10);
        connectionManager.initialisePool();
        return connectionManager;
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
        return XA_CONNECTION_MANAGER.getConnection();
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
