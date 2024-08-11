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

package io.liftwizard.dropwizard.configuration.connectionmanager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;

public class ConnectionManagersFactory
{
    private @Valid @NotNull List<ConnectionManagerFactory> connectionManagerFactories = List.of();

    private Map<String, ConnectionManagerFactory> connectionManagerFactoriesByName = new LinkedHashMap<>();

    private final Map<String, SourcelessConnectionManager> connectionManagersByName = new LinkedHashMap<>();

    @JsonProperty("connectionManagers")
    public List<ConnectionManagerFactory> getConnectionManagerFactories()
    {
        return this.connectionManagerFactories;
    }

    @JsonProperty("connectionManagers")
    public void setConnectionManagerFactories(List<ConnectionManagerFactory> connectionManagerFactories)
    {
        this.connectionManagerFactories = connectionManagerFactories;
        this.connectionManagerFactoriesByName = new LinkedHashMap<>();
        for (ConnectionManagerFactory connectionManagerFactory : connectionManagerFactories)
        {
            this.connectionManagerFactoriesByName.put(
                    connectionManagerFactory.getConnectionManagerName(),
                    connectionManagerFactory);
        }
    }

    @ValidationMethod
    @JsonIgnore
    public boolean isValidConnectionManagerNames()
    {
        /* TODO: We could validate more here. If multiple connectionManagers share a data source,
         * they should also share almost everything, except schemaNames should be different.
         */

        List<String> orderedConnectionManagerNames = this.connectionManagerFactories
                .stream()
                .map(ConnectionManagerFactory::getConnectionManagerName)
                .toList();
        List<String> duplicateConnectionManagerNames = orderedConnectionManagerNames
                .stream()
                .collect(Collectors.groupingBy(key -> key, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(p -> p.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (duplicateConnectionManagerNames.isEmpty())
        {
            return true;
        }

        String errorMessage = "Duplicate names found in connectionManagers: " + duplicateConnectionManagerNames;
        throw new IllegalStateException(errorMessage);
    }

    @JsonIgnore
    @Nonnull
    public Map<String, SourcelessConnectionManager> getConnectionManagersByName(
            NamedDataSourceProvider dataSourceProvider,
            @Nonnull Environment environment)
    {
        for (ConnectionManagerFactory connectionManagerFactory : this.connectionManagerFactories)
        {
            this.getConnectionManagerByName(
                    dataSourceProvider,
                    environment,
                    connectionManagerFactory);
        }
        return this.connectionManagersByName;
    }

    @JsonIgnore
    public SourcelessConnectionManager getConnectionManagerByName(
            NamedDataSourceProvider dataSourceProvider,
            @Nonnull Environment environment,
            ConnectionManagerFactory connectionManagerFactory)
    {
        String connectionManagerName = connectionManagerFactory.getConnectionManagerName();

        if (this.connectionManagersByName.containsKey(connectionManagerName))
        {
            return this.connectionManagersByName.get(connectionManagerName);
        }

        if (!this.connectionManagerFactoriesByName.containsKey(connectionManagerName))
        {
            String message = "No connection manager named: '%s'. Known connection managers: %s".formatted(
                    connectionManagerName,
                    this.connectionManagerFactoriesByName.keySet());
            throw new IllegalStateException(message);
        }

        ManagedDataSource managedDataSource = dataSourceProvider
                .getNamedDataSourcesFactory()
                .getDataSourceByName(
                        connectionManagerFactory.getDataSourceName(),
                        environment.metrics(),
                        environment.lifecycle());

        SourcelessConnectionManager sourcelessConnectionManager = this.connectionManagerFactoriesByName
                .get(connectionManagerName)
                .createSourcelessConnectionManager(managedDataSource);
        this.connectionManagersByName.put(connectionManagerName, sourcelessConnectionManager);
        return sourcelessConnectionManager;
    }
}
