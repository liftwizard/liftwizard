package com.liftwizard.dropwizard.configuration.datasource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liftwizard.dropwizard.db.NamedDataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.validation.ValidationMethod;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.collections.impl.map.ordered.mutable.OrderedMapAdapter;

public class NamedDataSourceConfiguration
        implements NamedDataSourceProvider
{
    private @Valid @NotNull List<NamedDataSourceFactory> namedDataSourceFactories = new ArrayList<>();

    private MapIterable<String, ManagedDataSource> dataSourcesByName;

    private boolean initialized;

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
        ImmutableList<String> orderedDataSourceNames = ListAdapter.adapt(this.namedDataSourceFactories)
                .collect(NamedDataSourceFactory::getName)
                .toImmutable();
        MutableBag<String> duplicateDataSourceNames = orderedDataSourceNames
                .toBag()
                .selectDuplicates();
        if (duplicateDataSourceNames.isEmpty())
        {
            return true;
        }

        ImmutableList<String> orderedDuplicateDataSourceNames =
                orderedDataSourceNames.select(duplicateDataSourceNames::contains);
        String errorMessage = "Duplicate names found in dataSources: " + orderedDuplicateDataSourceNames.makeString();
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

        this.dataSourcesByName = ListAdapter.adapt(this.namedDataSourceFactories)
                .groupByUniqueKey(NamedDataSourceFactory::getName, OrderedMapAdapter.adapt(new LinkedHashMap<>()))
                .collectValues((name, factory) -> factory.build(metricRegistry))
                .asUnmodifiable();

        this.dataSourcesByName.valuesView().each(lifecycle::manage);

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
    public MapIterable<String, ManagedDataSource> getDataSourcesByName()
    {
        if (!this.initialized)
        {
            throw new IllegalStateException("Not initialized. Did you remember to run NamedDataSourceBundle?");
        }
        return Objects.requireNonNull(this.dataSourcesByName);
    }
}
