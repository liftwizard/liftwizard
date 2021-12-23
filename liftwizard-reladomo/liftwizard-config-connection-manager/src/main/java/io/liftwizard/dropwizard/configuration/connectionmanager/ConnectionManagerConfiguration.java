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

package io.liftwizard.dropwizard.configuration.connectionmanager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.validation.ValidationMethod;

public class ConnectionManagerConfiguration
        implements ConnectionManagerFactoryProvider
{
    private @Valid @NotNull List<ConnectionManagerFactory> connectionManagerFactories = List.of();

    private Map<String, SourcelessConnectionManager> connectionManagersByName;

    private boolean initialized;

    @Override
    @JsonProperty("connectionManagers")
    public List<ConnectionManagerFactory> getConnectionManagerFactories()
    {
        return this.connectionManagerFactories;
    }

    @JsonProperty("connectionManagers")
    public void setConnectionManagerFactories(List<ConnectionManagerFactory> connectionManagerFactories)
    {
        this.connectionManagerFactories = connectionManagerFactories;
    }

    @ValidationMethod
    public boolean isValidConnectionManagerNames()
    {
        /* TODO: We could validate more here. If multiple connectionManagers share a data source,
         * they should also share almost everything, except schemaNames should be different.
         */

        List<String> orderedConnectionManagerNames = this.connectionManagerFactories
                .stream()
                .map(ConnectionManagerFactory::getConnectionManagerName)
                .collect(Collectors.toUnmodifiableList());
        List<String> duplicateConnectionManagerNames = orderedConnectionManagerNames
                .stream()
                .collect(Collectors.groupingBy(key -> key, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(p -> p.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (duplicateConnectionManagerNames.isEmpty())
        {
            return true;
        }

        String errorMessage = "Duplicate names found in connectionManagers: "
                + duplicateConnectionManagerNames;
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public void initializeConnectionManagers(@Nonnull Map<String, ManagedDataSource> dataSourcesByName)
    {
        if (this.initialized)
        {
            throw new IllegalStateException("Already initialized.");
        }

        Objects.requireNonNull(dataSourcesByName);

        this.connectionManagersByName = Collections.unmodifiableMap(this.connectionManagerFactories
                .stream()
                .collect(Collectors.toMap(
                        ConnectionManagerFactory::getConnectionManagerName,
                        factory -> this.initialize(factory, dataSourcesByName),
                        (duplicate1, duplicate2) ->
                        {
                            throw new IllegalStateException("Duplicate connection manager factory: " + duplicate1);
                        },
                        LinkedHashMap::new)));

        this.initialized = true;
    }

    private SourcelessConnectionManager initialize(
            @Nonnull ConnectionManagerFactory factory,
            @Nonnull Map<String, ManagedDataSource> dataSourcesByName)
    {
        String            dataSourceName    = factory.getDataSourceName();
        ManagedDataSource managedDataSource = dataSourcesByName.get(dataSourceName);
        if (managedDataSource == null)
        {
            String errorMessage = String.format(
                    "Connection manager '%s' refers to unknown data source '%s'. Known data sources: %s",
                    factory.getConnectionManagerName(),
                    factory.getDataSourceName(),
                    dataSourcesByName.keySet());
            throw new IllegalStateException(errorMessage);
        }
        return factory.createSourcelessConnectionManager(managedDataSource);
    }

    @Override
    @JsonIgnore
    public SourcelessConnectionManager getConnectionManagerByName(@Nonnull String name)
    {
        Objects.requireNonNull(name);
        if (!this.initialized)
        {
            throw new IllegalStateException();
        }
        return this.connectionManagersByName.get(name);
    }

    @Override
    @JsonIgnore
    public Map<String, SourcelessConnectionManager> getConnectionManagersByName()
    {
        if (!this.initialized)
        {
            throw new IllegalStateException("Not initialized. Did you remember to run ConnectionManagerBundle?");
        }
        return this.connectionManagersByName;
    }
}
