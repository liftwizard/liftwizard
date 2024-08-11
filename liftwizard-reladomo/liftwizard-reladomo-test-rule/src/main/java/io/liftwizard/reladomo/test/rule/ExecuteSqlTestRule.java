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

package io.liftwizard.reladomo.test.rule;

import java.net.URL;
import java.sql.Connection;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.liftwizard.reladomo.connectionmanager.h2.memory.H2InMemoryConnectionManager;
import io.liftwizard.reladomo.ddl.executor.DatabaseDdlExecutor;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.reflections.Reflections;

public class ExecuteSqlTestRule
        implements TestRule
{
    /**
     * The reason for the dots instead of slashes is that {@link Reflections#scan(URL)} calls {@code file.getRelativePath().replace('/', '.')} before matching any patterns.
     */
    // language=RegExp
    private String ddlLocationPattern = "^(?!META-INF\\.).*\\.ddl$";
    // language=RegExp
    private String idxLocationPattern = "^(?!META-INF\\.).*\\.idx$";
    // language=RegExp
    private String fkLocationPattern = "^(?!META-INF\\.).*\\.fk$";

    @Nonnull
    private Supplier<? extends Connection> connectionSupplier = () -> H2InMemoryConnectionManager
            .getInstance()
            .getConnection();

    public ExecuteSqlTestRule setDdlLocationPattern(@Nonnull String ddlLocationPattern)
    {
        this.ddlLocationPattern = Objects.requireNonNull(ddlLocationPattern);
        return this;
    }

    public ExecuteSqlTestRule setIdxLocationPattern(@Nonnull String idxLocationPattern)
    {
        this.idxLocationPattern = Objects.requireNonNull(idxLocationPattern);
        return this;
    }

    public ExecuteSqlTestRule setFkLocationPattern(@Nonnull String fkLocationPattern)
    {
        this.fkLocationPattern = Objects.requireNonNull(fkLocationPattern);
        return this;
    }

    public ExecuteSqlTestRule setConnectionSupplier(@Nonnull Supplier<? extends Connection> connectionSupplier)
    {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier);
        return this;
    }

    @Nonnull
    @Override
    public Statement apply(@Nonnull Statement base, @Nonnull Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate()
                    throws Throwable
            {
                try (Connection connection = ExecuteSqlTestRule.this.connectionSupplier.get())
                {
                    DatabaseDdlExecutor.dropAllObjects(connection);
                    DatabaseDdlExecutor.executeSql(
                            connection,
                            ExecuteSqlTestRule.this.ddlLocationPattern,
                            ExecuteSqlTestRule.this.idxLocationPattern,
                            ExecuteSqlTestRule.this.fkLocationPattern);
                }
                base.evaluate();
                try (Connection connection = ExecuteSqlTestRule.this.connectionSupplier.get())
                {
                    DatabaseDdlExecutor.dropAllObjects(connection);
                }
            }
        };
    }
}
