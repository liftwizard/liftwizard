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

package io.liftwizard.dropwizard.configuration.liquibase.migration;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.liftwizard.dropwizard.configuration.enabled.EnabledFactory;

public class LiquibaseMigrationFactory
        extends EnabledFactory
{
    private List<LiquibaseDataSourceMigrationFactory> dataSourceMigrations = new ArrayList<>();

    private          boolean dropEntireSchemaOnStartupAndShutdown;

    private boolean dryRun;

    @JsonProperty
    public List<LiquibaseDataSourceMigrationFactory> getDataSourceMigrations()
    {
        return this.dataSourceMigrations;
    }

    @JsonProperty
    public void setDataSourceMigrations(List<LiquibaseDataSourceMigrationFactory> dataSourceMigrations)
    {
        this.dataSourceMigrations = dataSourceMigrations;
    }

    @JsonProperty
    public boolean isDropEntireSchemaOnStartupAndShutdown()
    {
        return this.dropEntireSchemaOnStartupAndShutdown;
    }

    @JsonProperty
    public void setDropEntireSchemaOnStartupAndShutdown(boolean dropEntireSchemaOnStartupAndShutdown)
    {
        this.dropEntireSchemaOnStartupAndShutdown = dropEntireSchemaOnStartupAndShutdown;
    }

    @JsonProperty
    public boolean isDryRun()
    {
        return this.dryRun;
    }

    @JsonProperty
    public void setDryRun(boolean dryRun)
    {
        this.dryRun = dryRun;
    }
}
