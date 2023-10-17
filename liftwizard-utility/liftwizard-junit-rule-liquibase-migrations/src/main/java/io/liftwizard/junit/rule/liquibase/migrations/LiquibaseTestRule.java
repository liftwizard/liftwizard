/*
 * Copyright 2023 Craig Motlin
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

package io.liftwizard.junit.rule.liquibase.migrations;

import java.util.Objects;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class LiquibaseTestRule
        implements TestRule
{
    private final String  migrationsFile;
    private final boolean dropAll;

    public LiquibaseTestRule(String migrationsFile)
    {
        this(migrationsFile, true);
    }

    public LiquibaseTestRule(String migrationsFile, boolean dropAll)
    {
        this.migrationsFile = Objects.requireNonNull(migrationsFile);
        this.dropAll        = dropAll;
    }

    @Override
    public Statement apply(Statement base, Description description)
    {
        return new LiquibaseStatement(base, this.migrationsFile, this.dropAll);
    }
}
