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

public class ConnectionManagersFactory {

    private @Valid @NotNull List<ConnectionManagerFactory> connectionManagerFactories = List.of();

    private Map<String, ConnectionManagerFactory> connectionManagerFactoriesByName = new LinkedHashMap<>();

    private final Map<String, SourcelessConnectionManager> connectionManagersByName = new LinkedHashMap<>();

    @JsonProperty("connectionManagers")
    public List<ConnectionManagerFactory> getConnectionManagerFactories() {
        return this.connectionManagerFactories;
    }

    @JsonProperty("connectionManagers")
    public void setConnectionManagerFactories(List<ConnectionManagerFactory> connectionManagerFactories) {
        this.connectionManagerFactories = connectionManagerFactories;
        this.connectionManagerFactoriesByName = new LinkedHashMap<>();
        for (ConnectionManagerFactory connectionManagerFactory : connectionManagerFactories) {
            this.connectionManagerFactoriesByName.put(
                    connectionManagerFactory.getConnectionManagerName(),
                    connectionManagerFactory
                );
        }
    }

    @ValidationMethod
    @JsonIgnore
    public boolean isValidConnectionManagerNames() {
        List<String> orderedConnectionManagerNames =
            this.connectionManagerFactories.stream().map(ConnectionManagerFactory::getConnectionManagerName).toList();
        List<String> duplicateConnectionManagerNames = orderedConnectionManagerNames
            .stream()
            .collect(Collectors.groupingBy(key -> key, LinkedHashMap::new, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(p -> p.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();
        if (!duplicateConnectionManagerNames.isEmpty()) {
            String errorMessage = "Duplicate names found in connectionManagers: " + duplicateConnectionManagerNames;
            throw new IllegalStateException(errorMessage);
        }

        // Validate that connection managers sharing the same data source have compatible configurations
        Map<String, List<ConnectionManagerFactory>> managersByDataSource =
            this.connectionManagerFactories.stream()
                .collect(Collectors.groupingBy(ConnectionManagerFactory::getDataSourceName));

        for (List<ConnectionManagerFactory> managersWithSameDataSource : managersByDataSource.values()) {
            if (managersWithSameDataSource.size() <= 1) {
                continue;
            }

            // For managers sharing a data source, verify that they have the same configuration except schema
            ConnectionManagerFactory firstManager = managersWithSameDataSource.get(0);
            for (int i = 1; i < managersWithSameDataSource.size(); i++) {
                ConnectionManagerFactory currentManager = managersWithSameDataSource.get(i);

                // Verify that database type matches
                if (!firstManager.getDatabaseType().equals(currentManager.getDatabaseType())) {
                    String errorMessage =
                        "Connection managers '%s' and '%s' share data source '%s' but have different database types: %s vs %s".formatted(
                                firstManager.getConnectionManagerName(),
                                currentManager.getConnectionManagerName(),
                                firstManager.getDataSourceName(),
                                firstManager.getDatabaseType(),
                                currentManager.getDatabaseType()
                            );
                    throw new IllegalStateException(errorMessage);
                }

                // Verify that time zone matches
                if (!firstManager.getTimeZoneName().equals(currentManager.getTimeZoneName())) {
                    String errorMessage =
                        "Connection managers '%s' and '%s' share data source '%s' but have different time zones: %s vs %s".formatted(
                                firstManager.getConnectionManagerName(),
                                currentManager.getConnectionManagerName(),
                                firstManager.getDataSourceName(),
                                firstManager.getTimeZoneName(),
                                currentManager.getTimeZoneName()
                            );
                    throw new IllegalStateException(errorMessage);
                }

                // Verify that schemas are different
                if (firstManager.getSchemaName().equals(currentManager.getSchemaName())) {
                    String errorMessage =
                        "Connection managers '%s' and '%s' share data source '%s' but also have the same schema: %s".formatted(
                                firstManager.getConnectionManagerName(),
                                currentManager.getConnectionManagerName(),
                                firstManager.getDataSourceName(),
                                firstManager.getSchemaName()
                            );
                    throw new IllegalStateException(errorMessage);
                }
            }
        }

        return true;
    }

    @JsonIgnore
    @Nonnull
    public Map<String, SourcelessConnectionManager> getConnectionManagersByName(
        NamedDataSourceProvider dataSourceProvider,
        @Nonnull Environment environment
    ) {
        for (ConnectionManagerFactory connectionManagerFactory : this.connectionManagerFactories) {
            this.getConnectionManagerByName(dataSourceProvider, environment, connectionManagerFactory);
        }
        return this.connectionManagersByName;
    }

    @JsonIgnore
    public SourcelessConnectionManager getConnectionManagerByName(
        NamedDataSourceProvider dataSourceProvider,
        @Nonnull Environment environment,
        ConnectionManagerFactory connectionManagerFactory
    ) {
        String connectionManagerName = connectionManagerFactory.getConnectionManagerName();

        if (this.connectionManagersByName.containsKey(connectionManagerName)) {
            return this.connectionManagersByName.get(connectionManagerName);
        }

        if (!this.connectionManagerFactoriesByName.containsKey(connectionManagerName)) {
            String message =
                "No connection manager named: '%s'. Known connection managers: %s".formatted(
                        connectionManagerName,
                        this.connectionManagerFactoriesByName.keySet()
                    );
            throw new IllegalStateException(message);
        }

        ManagedDataSource managedDataSource = dataSourceProvider
            .getNamedDataSourcesFactory()
            .getDataSourceByName(
                connectionManagerFactory.getDataSourceName(),
                environment.metrics(),
                environment.lifecycle()
            );

        SourcelessConnectionManager sourcelessConnectionManager =
            this.connectionManagerFactoriesByName.get(connectionManagerName).createSourcelessConnectionManager(
                    managedDataSource
                );
        this.connectionManagersByName.put(connectionManagerName, sourcelessConnectionManager);
        return sourcelessConnectionManager;
    }
}
