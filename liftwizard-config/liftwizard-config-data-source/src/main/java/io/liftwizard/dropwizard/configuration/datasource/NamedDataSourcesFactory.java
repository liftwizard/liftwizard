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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.dropwizard.db.NamedDataSourceFactory;

public class NamedDataSourcesFactory
{
    private @Valid @NotNull List<NamedDataSourceFactory> namedDataSourceFactories = new ArrayList<>();

    private Map<String, NamedDataSourceFactory> namedDataSourceFactoriesByName = new LinkedHashMap<>();

    private final Map<String, ManagedDataSource> dataSourcesByName = new LinkedHashMap<>();

    @JsonProperty("dataSources")
    public List<NamedDataSourceFactory> getNamedDataSourceFactories()
    {
        return this.namedDataSourceFactories;
    }

    @JsonProperty("dataSources")
    public void setNamedDataSourceFactories(List<NamedDataSourceFactory> namedDataSourceFactories)
    {
        this.namedDataSourceFactories       = namedDataSourceFactories;
        this.namedDataSourceFactoriesByName = new LinkedHashMap<>();
        for (NamedDataSourceFactory namedDataSourceFactory : namedDataSourceFactories)
        {
            this.namedDataSourceFactoriesByName.put(
                    namedDataSourceFactory.getName(),
                    namedDataSourceFactory);
        }
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

    @Nonnull
    @JsonIgnore
    public NamedDataSourceFactory getNamedDataSourceFactoryByName(String name)
    {
        return this.namedDataSourceFactories
                .stream()
                .filter(namedDataSourceFactory -> namedDataSourceFactory.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown data source name: " + name));
    }

    @JsonIgnore
    public ManagedDataSource getDataSourceByName(
            @Nonnull String name,
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull LifecycleEnvironment lifecycle)
    {
        if (this.dataSourcesByName.containsKey(name))
        {
            return this.dataSourcesByName.get(name);
        }

        if (!this.namedDataSourceFactoriesByName.containsKey(name))
        {
            String message = String.format(
                    "No data source named: '%s'. Known data sources: %s",
                    name,
                    this.namedDataSourceFactoriesByName.keySet());
            throw new IllegalStateException(message);
        }

        NamedDataSourceFactory namedDataSourceFactory = this.namedDataSourceFactoriesByName.get(name);
        ManagedDataSource      managedDataSource      = namedDataSourceFactory.build(metricRegistry);
        lifecycle.manage(managedDataSource);
        this.dataSourcesByName.put(name, managedDataSource);
        return managedDataSource;
    }
}
