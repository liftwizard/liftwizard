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

package io.liftwizard.junit.extension.liquibase.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.Scope.Attr;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.ui.LoggerUIService;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LiquibaseTestExtension
        implements BeforeEachCallback
{
    private final String migrationsFile;
    private final boolean dropAll;

    public LiquibaseTestExtension(String migrationsFile)
    {
        this(migrationsFile, true);
    }

    public LiquibaseTestExtension(String migrationsFile, boolean dropAll)
    {
        this.migrationsFile = Objects.requireNonNull(migrationsFile);
        this.dropAll = dropAll;
    }

    @Override
    public void beforeEach(ExtensionContext context)
            throws Exception
    {
        Scope.child(Attr.ui, new LoggerUIService(), this::runWithLogger);
    }

    private void runWithLogger()
            throws SQLException, LiquibaseException
    {
        try (
                Connection connection = H2InMemoryConnectionManager.getInstance().getConnection();
                Liquibase liquibase = this.openLiquibase(connection))
        {
            if (this.dropAll)
            {
                liquibase.dropAll();
            }
            liquibase.update("");
        }
    }

    private Liquibase openLiquibase(Connection connection)
            throws LiquibaseException
    {
        Database database = this.createDatabase(connection);
        Liquibase liquibase = new Liquibase(this.migrationsFile, new ClassLoaderResourceAccessor(), database);
        liquibase.setShowSummaryOutput(UpdateSummaryOutputEnum.LOG);
        return liquibase;
    }

    private Database createDatabase(Connection connection)
            throws LiquibaseException
    {
        DatabaseConnection jdbcConnection = new JdbcConnection(connection);

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);

        database.supportsCatalogs();
        database.supportsSchemas();

        return database;
    }
}
