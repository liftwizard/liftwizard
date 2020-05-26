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

package com.liftwizard.dropwizard.configuration.connectionmanager;

import java.util.Arrays;
import java.util.Objects;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.liftwizard.reladomo.connectionmanager.LiftwizardConnectionManager;
import io.dropwizard.validation.ValidationMethod;

public class ConnectionManagerFactory
{
    private @Valid @NotNull String           connectionManagerName;
    private @Valid @NotNull String           dataSourceName;
    private @Valid @NotNull DatabaseTypeEnum databaseType = DatabaseTypeEnum.GENERIC;
    private @Valid @NotNull String           timeZoneName = "UTC";
    private @Valid @NotNull String           schemaName;

    public SourcelessConnectionManager createSourcelessConnectionManager(@Nonnull DataSource dataSource)
    {
        Objects.requireNonNull(dataSource);

        DatabaseType reladomoDatabaseType = this.databaseType.getDatabaseType();
        TimeZone     timeZone             = TimeZone.getTimeZone(this.timeZoneName);
        return new LiftwizardConnectionManager(
                this.connectionManagerName,
                this.dataSourceName,
                dataSource,
                reladomoDatabaseType,
                timeZone,
                this.schemaName);
    }

    @JsonProperty
    public String getConnectionManagerName()
    {
        return this.connectionManagerName;
    }

    @JsonProperty
    public void setConnectionManagerName(String connectionManagerName)
    {
        this.connectionManagerName = connectionManagerName;
    }

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
    public DatabaseTypeEnum getDatabaseType()
    {
        return this.databaseType;
    }

    @JsonProperty
    public void setDatabaseType(DatabaseTypeEnum databaseType)
    {
        this.databaseType = databaseType;
    }

    @JsonProperty("timeZone")
    public String getTimeZoneName()
    {
        return this.timeZoneName;
    }

    @JsonProperty("timeZone")
    public void setTimeZoneName(String timeZoneName)
    {
        this.timeZoneName = timeZoneName;
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

    @ValidationMethod(message = "Invalid timeZoneName")
    @JsonIgnore
    public boolean isValidTimezone()
    {
        TimeZone zoneInfo = TimeZone.getTimeZone(this.timeZoneName);
        if (zoneInfo == null)
        {
            String message = String.format(
                    "Got timeZoneName '%s' but expected one of: %s",
                    this.timeZoneName,
                    Arrays.toString(TimeZone.getAvailableIDs()));
            throw new IllegalStateException(message);
        }
        return true;
    }
}
