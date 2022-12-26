/*
 * Copyright 2020 Craig Motlin
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

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LiquibaseDataSourceMigrationFactory
{
    private @NotNull String                dataSourceName;
    private          String                catalogName;
    private          String                schemaName;
    private          String                migrationFileName     = "migrations.xml";
    private @NotNull MigrationFileLocation migrationFileLocation = MigrationFileLocation.CLASSPATH;
    private @NotNull List<String>          contexts              = List.of();
    private          boolean               rollbackOnShutdown;
    private          String                rollbackToTag;

    @JsonProperty
    public String getDataSourceName()
    {
        return this.dataSourceName;
    }

    @JsonProperty
    public void setDataSourceName(String dataSourceName)
    {
        this.dataSourceName = dataSourceName;
    }

    @JsonProperty
    public String getCatalogName()
    {
        return this.catalogName;
    }

    @JsonProperty
    public void setCatalogName(String catalogName)
    {
        this.catalogName = catalogName;
    }

    @JsonProperty
    public String getSchemaName()
    {
        return this.schemaName;
    }

    @JsonProperty
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    @JsonProperty
    public String getMigrationFileName()
    {
        return this.migrationFileName;
    }

    @JsonProperty
    public void setMigrationFileName(String migrationFileName)
    {
        this.migrationFileName = migrationFileName;
    }

    @JsonProperty
    public MigrationFileLocation getMigrationFileLocation()
    {
        return this.migrationFileLocation;
    }

    @JsonProperty
    public void setMigrationFileLocation(MigrationFileLocation migrationFileLocation)
    {
        this.migrationFileLocation = migrationFileLocation;
    }

    @JsonProperty
    public List<String> getContexts()
    {
        return this.contexts;
    }

    @JsonProperty
    public void setContexts(List<String> contexts)
    {
        this.contexts = contexts;
    }

    @JsonProperty
    public boolean isRollbackOnShutdown()
    {
        return this.rollbackOnShutdown;
    }

    @JsonProperty
    public void setRollbackOnShutdown(boolean rollbackOnShutdown)
    {
        this.rollbackOnShutdown = rollbackOnShutdown;
    }

    @JsonProperty
    public String getRollbackToTag()
    {
        return this.rollbackToTag;
    }

    @JsonProperty
    public void setRollbackToTag(String rollbackToTag)
    {
        this.rollbackToTag = rollbackToTag;
    }
}
