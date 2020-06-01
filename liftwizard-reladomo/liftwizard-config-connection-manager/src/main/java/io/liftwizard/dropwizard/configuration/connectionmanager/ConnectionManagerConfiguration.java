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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.validation.ValidationMethod;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class ConnectionManagerConfiguration
        implements ConnectionManagerFactoryProvider
{
    private @Valid @NotNull List<ConnectionManagerFactory> connectionManagerFactories = new ArrayList<>();

    private ImmutableMap<String, SourcelessConnectionManager> connectionManagersByName;

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

        ImmutableList<String> orderedConnectionManagerNames = ListAdapter.adapt(this.connectionManagerFactories)
                .collect(ConnectionManagerFactory::getConnectionManagerName)
                .toImmutable();
        MutableBag<String> duplicateConnectionManagerNames = orderedConnectionManagerNames
                .toBag()
                .selectDuplicates();
        if (duplicateConnectionManagerNames.isEmpty())
        {
            return true;
        }

        ImmutableList<String> orderedDuplicateConnectionManagerNames =
                orderedConnectionManagerNames.select(duplicateConnectionManagerNames::contains);
        String errorMessage = "Duplicate names found in connectionManagers: "
                + orderedDuplicateConnectionManagerNames.makeString();
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public void initializeConnectionManagers(@Nonnull MapIterable<String, ManagedDataSource> dataSourcesByName)
    {
        if (this.initialized)
        {
            throw new IllegalStateException("Already initialized.");
        }

        Objects.requireNonNull(dataSourcesByName);

        this.connectionManagersByName = ListAdapter
                .adapt(this.connectionManagerFactories)
                .groupByUniqueKey(ConnectionManagerFactory::getConnectionManagerName)
                .collectValues((ignoredName, factory) -> this.initialize(factory, dataSourcesByName))
                .toImmutable();

        this.initialized = true;
    }

    private SourcelessConnectionManager initialize(
            @Nonnull ConnectionManagerFactory factory,
            @Nonnull MapIterable<String, ManagedDataSource> dataSourcesByName)
    {
        String            dataSourceName    = factory.getDataSourceName();
        ManagedDataSource managedDataSource = dataSourcesByName.get(dataSourceName);
        if (managedDataSource == null)
        {
            String errorMessage = String.format(
                    "Connection manager '%s' refers to unknown data source '%s'. Known data sources: %s",
                    factory.getConnectionManagerName(),
                    factory.getDataSourceName(),
                    dataSourcesByName.keysView());
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
    public ImmutableMap<String, SourcelessConnectionManager> getConnectionManagersByName()
    {
        if (!this.initialized)
        {
            throw new IllegalStateException("Not initialized. Did you remember to run ConnectionManagerBundle?");
        }
        return this.connectionManagersByName;
    }
}
