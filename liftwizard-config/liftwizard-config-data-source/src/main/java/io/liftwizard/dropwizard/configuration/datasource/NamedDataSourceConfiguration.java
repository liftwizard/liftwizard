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

package io.liftwizard.dropwizard.configuration.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.dropwizard.db.NamedDataSourceFactory;

public class NamedDataSourceConfiguration
        implements NamedDataSourceProvider
{
    private @Valid @NotNull List<NamedDataSourceFactory> namedDataSourceFactories = new ArrayList<>();

    private Map<String, ManagedDataSource> dataSourcesByName;

    private boolean initialized;

    @Override
    @JsonProperty("dataSources")
    public List<NamedDataSourceFactory> getNamedDataSourceFactories()
    {
        return this.namedDataSourceFactories;
    }

    @JsonProperty("dataSources")
    public void setNamedDataSourceFactories(List<NamedDataSourceFactory> namedDataSourceFactories)
    {
        this.namedDataSourceFactories = namedDataSourceFactories;
    }

    @ValidationMethod
    public boolean isValidDataSourceNames()
    {
        List<String> orderedDataSourceNames = this.namedDataSourceFactories
                .stream()
                .map(NamedDataSourceFactory::getName)
                .collect(Collectors.toList());
        Map<String, Long> frequencies = orderedDataSourceNames
                .stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        List<String> duplicateDataSourceNames = frequencies
                .entrySet()
                .stream()
                .filter(m -> m.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toList());

        if (duplicateDataSourceNames.isEmpty())
        {
            return true;
        }

        String errorMessage = "Duplicate names found in dataSources: " + duplicateDataSourceNames;
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public void initializeDataSources(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull LifecycleEnvironment lifecycle)
    {
        if (this.initialized)
        {
            throw new IllegalStateException("Already initialized.");
        }

        Objects.requireNonNull(metricRegistry);

        this.dataSourcesByName = Collections.unmodifiableMap(this.namedDataSourceFactories
                .stream()
                .collect(Collectors.toMap(
                        NamedDataSourceFactory::getName,
                        factory -> factory.build(metricRegistry),
                        (duplicate1, duplicate2) ->
                        {
                            throw new IllegalStateException("Duplicate named data source: " + duplicate1);
                        },
                        LinkedHashMap::new)));

        this.dataSourcesByName.values().forEach(lifecycle::manage);

        this.initialized = true;
    }

    @Override
    @JsonIgnore
    public DataSource getDataSourceByName(@Nonnull String name)
    {
        Objects.requireNonNull(name);
        if (!this.initialized)
        {
            throw new IllegalStateException();
        }
        return this.dataSourcesByName.get(name);
    }

    @Override
    @JsonIgnore
    public Map<String, ManagedDataSource> getDataSourcesByName()
    {
        if (!this.initialized)
        {
            throw new IllegalStateException("Not initialized. Did you remember to run NamedDataSourceBundle?");
        }
        return Objects.requireNonNull(this.dataSourcesByName);
    }
}
