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

package io.liftwizard.reladomo.test.extension;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import io.liftwizard.reladomo.ddl.executor.DatabaseDdlExecutor;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.reflections.Reflections;

public class ExecuteSqlExtension
        implements BeforeEachCallback, AfterEachCallback
{
    /**
     *  The reason for the dots instead of slashes is that {@link Reflections#scan(URL)} calls {@code file.getRelativePath().replace('/', '.')} before matching any patterns.
     */
    // language=RegExp
    private String ddlLocationPattern = "^(?!META-INF\\.).*\\.ddl$";
    // language=RegExp
    private String idxLocationPattern = "^(?!META-INF\\.).*\\.idx$";
    // language=RegExp
    private String fkLocationPattern  = "^(?!META-INF\\.).*\\.fk$";

    @Nonnull
    private Supplier<? extends Connection> connectionSupplier = () -> H2InMemoryConnectionManager
            .getInstance()
            .getConnection();

    public ExecuteSqlExtension setDdlLocationPattern(@Nonnull String ddlLocationPattern)
    {
        this.ddlLocationPattern = Objects.requireNonNull(ddlLocationPattern);
        return this;
    }

    public ExecuteSqlExtension setIdxLocationPattern(@Nonnull String idxLocationPattern)
    {
        this.idxLocationPattern = Objects.requireNonNull(idxLocationPattern);
        return this;
    }

    public ExecuteSqlExtension setFkLocationPattern(@Nonnull String fkLocationPattern)
    {
        this.fkLocationPattern = Objects.requireNonNull(fkLocationPattern);
        return this;
    }

    public ExecuteSqlExtension setConnectionSupplier(@Nonnull Supplier<? extends Connection> connectionSupplier)
    {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier);
        return this;
    }

    @Override
    public void beforeEach(ExtensionContext context)
            throws SQLException
    {
        try (Connection connection = this.connectionSupplier.get())
        {
            DatabaseDdlExecutor.dropAllObjects(connection);
            DatabaseDdlExecutor.executeSql(
                    connection,
                    this.ddlLocationPattern,
                    this.idxLocationPattern,
                    this.fkLocationPattern);
        }
    }

    @Override
    public void afterEach(ExtensionContext context)
            throws SQLException
    {
        try (Connection connection = this.connectionSupplier.get())
        {
            DatabaseDdlExecutor.dropAllObjects(connection);
        }
    }
}
