package com.liftwizard.reladomo.connectionmanager.heroku;

import java.sql.Connection;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.connectionmanager.XAConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.databasetype.PostgresDatabaseType;

public final class HerokuConnectionManager implements SourcelessConnectionManager
{
    private static final HerokuConnectionManager INSTANCE = new HerokuConnectionManager();

    @Nonnull
    private final XAConnectionManager xaConnectionManager;
    private final TimeZone            databaseTimeZone;
    private final String              schemaName;

    private HerokuConnectionManager()
    {
        this.schemaName          = "liftwizard-app";
        this.databaseTimeZone    = TimeZone.getTimeZone("UTC");
        this.xaConnectionManager = this.createXaConnectionManager();
    }

    @Nonnull
    private XAConnectionManager createXaConnectionManager()
    {
        XAConnectionManager connectionManager = new XAConnectionManager();
        connectionManager.setDriverClassName("org.postgresql.Driver");
        connectionManager.setMaxWait(500);
        connectionManager.setJdbcConnectionString(System.getenv("JDBC_DATABASE_URL"));
        connectionManager.setJdbcUser(System.getenv("JDBC_DATABASE_USERNAME"));
        connectionManager.setJdbcPassword(System.getenv("JDBC_DATABASE_PASSWORD"));
        connectionManager.setPoolName("Reladomo default connection pool");
        connectionManager.setInitialSize(1);
        connectionManager.setPoolSize(10);
        connectionManager.initialisePool();
        return connectionManager;
    }

    @Nonnull
    @SuppressWarnings("unused")
    public static HerokuConnectionManager getInstance()
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
        return this.xaConnectionManager.getConnection();
    }

    @Override
    public DatabaseType getDatabaseType()
    {
        return PostgresDatabaseType.getInstance();
    }

    @Override
    public TimeZone getDatabaseTimeZone()
    {
        return this.databaseTimeZone;
    }

    @Override
    public String getDatabaseIdentifier()
    {
        return this.schemaName;
    }
}
