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

package io.liftwizard.dropwizard.db;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;

public class NamedDataSourceFactory extends DataSourceFactory
{
    private @Valid @NotNull String name;

    private ManagedDataSource managedDataSource;

    @JsonProperty
    public String getName()
    {
        return this.name;
    }

    @JsonProperty
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public final ManagedDataSource build(MetricRegistry metricRegistry, String equalNameParameter)
    {
        if (!Objects.equals(this.name, equalNameParameter))
        {
            throw new IllegalArgumentException(equalNameParameter);
        }

        return super.build(metricRegistry, this.name);
    }

    public ManagedDataSource build(MetricRegistry metricRegistry)
    {
        if (this.managedDataSource == null)
        {
            this.managedDataSource = this.build(metricRegistry, this.name);
        }

        return this.managedDataSource;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
