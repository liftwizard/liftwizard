package com.liftwizard.reladomo.connectionmanager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.TimeZone;

import javax.sql.DataSource;

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;

public class LiftwizardConnectionManager implements SourcelessConnectionManager
{
    private final String       connectionManagerName;
    private final String       dataSourceName;
    private final DataSource   dataSource;
    private final DatabaseType databaseType;
    private final TimeZone     databaseTimeZone;
    private final String       schemaName;

    public LiftwizardConnectionManager(
            String connectionManagerName,
            String dataSourceName,
            DataSource dataSource,
            DatabaseType databaseType,
            TimeZone databaseTimeZone,
            String schemaName)
    {
        this.connectionManagerName = Objects.requireNonNull(connectionManagerName);
        this.dataSourceName        = Objects.requireNonNull(dataSourceName);
        this.dataSource            = Objects.requireNonNull(dataSource);
        this.databaseType          = Objects.requireNonNull(databaseType);
        this.databaseTimeZone      = Objects.requireNonNull(databaseTimeZone);
        this.schemaName            = Objects.requireNonNull(schemaName);
    }

    public String getConnectionManagerName()
    {
        return this.connectionManagerName;
    }

    public String getDataSourceName()
    {
        return this.dataSourceName;
    }

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
            return this.dataSource.getConnection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DatabaseType getDatabaseType()
    {
        return this.databaseType;
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
