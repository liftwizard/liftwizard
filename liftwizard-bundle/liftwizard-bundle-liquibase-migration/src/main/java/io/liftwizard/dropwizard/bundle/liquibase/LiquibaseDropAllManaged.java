/*
 * Copyright 2024 Craig Motlin
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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

public class LiquibaseDropAllManaged implements Managed {

    private final ManagedDataSource dataSource;

    @Nullable
    private final String catalogName;

    @Nullable
    private final String schemaName;

    private final String migrationFile;
    private final MigrationFileLocation migrationFileLocation;

    public LiquibaseDropAllManaged(
        ManagedDataSource dataSource,
        String catalogName,
        String schemaName,
        String migrationFile,
        MigrationFileLocation migrationFileLocation
    ) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.migrationFile = Objects.requireNonNull(migrationFile);
        this.migrationFileLocation = Objects.requireNonNull(migrationFileLocation);
    }

    @Override
    public void start() {}

    @Override
    public void stop() {
        try (CloseableLiquibase liquibase = this.openLiquibase()) {
            liquibase.dropAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CloseableLiquibase openLiquibase() throws SQLException, LiquibaseException {
        Database database = this.createDatabase();
        ResourceAccessor resourceAccessor = this.getResourceAccessor();
        return new CloseableLiquibase(this.migrationFile, resourceAccessor, database, this.dataSource);
    }

    private Database createDatabase() throws SQLException, LiquibaseException {
        DatabaseConnection connection = new JdbcConnection(this.dataSource.getConnection());
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

        if (database.supportsCatalogs() && this.catalogName != null) {
            database.setDefaultCatalogName(this.catalogName);
            database.setOutputDefaultCatalog(true);
        }
        if (database.supportsSchemas() && this.schemaName != null) {
            database.setDefaultSchemaName(this.schemaName);
            database.setOutputDefaultSchema(true);
        }

        return database;
    }

    @Nonnull
    private ResourceAccessor getResourceAccessor() {
        return switch (this.migrationFileLocation) {
            case CLASSPATH -> new ClassLoaderResourceAccessor();
            case FILESYSTEM -> new FileSystemResourceAccessor();
        };
    }
}
