package com.liftwizard.dropwizard.configuration.datasource;

import java.util.List;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.codahale.metrics.MetricRegistry;
import com.liftwizard.dropwizard.db.NamedDataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.eclipse.collections.api.map.MapIterable;

public interface NamedDataSourceProvider
{
    List<NamedDataSourceFactory> getNamedDataSourceFactories();

    void initializeDataSources(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull LifecycleEnvironment lifecycle);

    DataSource getDataSourceByName(@Nonnull String name);

    @Nonnull
    MapIterable<String, ManagedDataSource> getDataSourcesByName();
}
