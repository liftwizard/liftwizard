/*
 * Copyright 2022 Craig Motlin
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

package io.liftwizard.dropwizard.bundle.liquibase;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.Managed;
import io.liftwizard.dropwizard.configuration.liquibase.migration.MigrationFileLocation;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDropAllManaged
        implements Managed
{
    private final ManagedDataSource     dataSource;
    private final String                catalogName;
    private final String                schemaName;
    private final String                migrationFile;
    private final MigrationFileLocation migrationFileLocation;

    public LiquibaseDropAllManaged(
            ManagedDataSource dataSource,
            String catalogName,
            String schemaName,
            String migrationFile,
            MigrationFileLocation migrationFileLocation)
    {
        this.dataSource            = dataSource;
        this.catalogName           = catalogName;
        this.schemaName            = schemaName;
        this.migrationFile         = migrationFile;
        this.migrationFileLocation = migrationFileLocation;
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
            throws LiquibaseException
    {
        try (
                CloseableLiquibase liquibase = this.openLiquibase(
                        this.dataSource,
                        this.catalogName,
                        this.schemaName,
                        this.migrationFile,
                        this.migrationFileLocation))
        {
            liquibase.dropAll();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private CloseableLiquibase openLiquibase(
            ManagedDataSource dataSource,
            String catalogName,
            String schemaName,
            String migrationsFile,
            MigrationFileLocation migrationFileLocation)
            throws SQLException, LiquibaseException
    {
        Database         database         = this.createDatabase(dataSource, catalogName, schemaName);
        ResourceAccessor resourceAccessor = LiquibaseDropAllManaged.getResourceAccessor(migrationFileLocation);
        return new CloseableLiquibase(migrationsFile, resourceAccessor, database, dataSource);
    }

    @Nonnull
    private static ResourceAccessor getResourceAccessor(MigrationFileLocation migrationFileLocation)
    {
        return switch (migrationFileLocation)
        {
            case CLASSPATH -> new ClassLoaderResourceAccessor();
            case FILESYSTEM -> new FileSystemResourceAccessor();
            default -> throw new IllegalStateException("Unexpected value: " + migrationFileLocation);
        };
    }

    private Database createDatabase(ManagedDataSource dataSource, String catalogName, String schemaName)
            throws SQLException, LiquibaseException
    {
        DatabaseConnection conn     = new JdbcConnection(dataSource.getConnection());
        Database           database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn);

        if (database.supportsCatalogs() && catalogName != null)
        {
            database.setDefaultCatalogName(catalogName);
            database.setOutputDefaultCatalog(true);
        }
        if (database.supportsSchemas() && schemaName != null)
        {
            database.setDefaultSchemaName(schemaName);
            database.setOutputDefaultSchema(true);
        }

        return database;
    }
}
