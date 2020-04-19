package com.liftwizard.dropwizard.db;

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
}
