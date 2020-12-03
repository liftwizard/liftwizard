package com.example.helloworld;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.codahale.metrics.MetricRegistry;
import com.example.helloworld.core.Template;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.smoketurner.dropwizard.graphql.GraphQLFactory;
import io.dropwizard.Configuration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import io.liftwizard.dropwizard.configuration.clock.system.SystemClockFactory;
import io.liftwizard.dropwizard.configuration.config.logging.ConfigLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerConfiguration;
import io.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerFactory;
import io.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerFactoryProvider;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourceConfiguration;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import io.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactory;
import io.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactoryProvider;
import io.liftwizard.dropwizard.configuration.enabled.EnabledFactory;
import io.liftwizard.dropwizard.configuration.graphql.GraphQLFactoryProvider;
import io.liftwizard.dropwizard.configuration.h2.H2Factory;
import io.liftwizard.dropwizard.configuration.h2.H2FactoryProvider;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactory;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactory;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactoryProvider;
import io.liftwizard.dropwizard.configuration.reladomo.ReladomoFactory;
import io.liftwizard.dropwizard.configuration.reladomo.ReladomoFactoryProvider;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import io.liftwizard.dropwizard.configuration.uuid.system.SystemUUIDSupplierFactory;
import io.liftwizard.dropwizard.db.NamedDataSourceFactory;
import org.eclipse.collections.api.map.MapIterable;
import org.hibernate.validator.constraints.NotEmpty;

public class HelloWorldConfiguration
        extends Configuration
        implements ConfigLoggingFactoryProvider,
        ClockFactoryProvider,
        UUIDSupplierFactoryProvider,
        ObjectMapperFactoryProvider,
        JerseyHttpLoggingFactoryProvider,
        H2FactoryProvider,
        DdlExecutorFactoryProvider,
        ReladomoFactoryProvider,
        NamedDataSourceProvider,
        ConnectionManagerFactoryProvider,
        GraphQLFactoryProvider
{
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @NotNull
    private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

    private @NotNull @Valid EnabledFactory           configLoggingFactory     = new EnabledFactory(true);
    private @NotNull @Valid ObjectMapperFactory      objectMapperFactory      = new ObjectMapperFactory();
    private @NotNull @Valid ClockFactory             clockFactory             = new SystemClockFactory();
    private @NotNull @Valid UUIDSupplierFactory      uuidFactory              = new SystemUUIDSupplierFactory();
    private @Valid @NotNull JerseyHttpLoggingFactory jerseyHttpLoggingFactory = new JerseyHttpLoggingFactory();
    private @Valid @NotNull H2Factory                h2Factory                = new H2Factory();
    private @Valid @NotNull List<DdlExecutorFactory> ddlExecutorFactories     = Arrays.asList();
    private @Valid @NotNull ReladomoFactory          reladomoFactory          = new ReladomoFactory();
    private @Valid @NotNull GraphQLFactory           graphQLFactory           = new GraphQLFactory();

    private @Valid @NotNull NamedDataSourceConfiguration   namedDataSourceConfiguration   =
            new NamedDataSourceConfiguration();
    private @Valid @NotNull ConnectionManagerConfiguration connectionManagerConfiguration =
            new ConnectionManagerConfiguration();

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Template buildTemplate() {
        return new Template(template, defaultName);
    }

    @JsonProperty("viewRendererConfiguration")
    public Map<String, Map<String, String>> getViewRendererConfiguration() {
        return viewRendererConfiguration;
    }

    @JsonProperty("viewRendererConfiguration")
    public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
        final ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();
        for (Map.Entry<String, Map<String, String>> entry : viewRendererConfiguration.entrySet()) {
            builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
        }
        this.viewRendererConfiguration = builder.build();
    }

    @Override
    @JsonProperty("configLogging")
    public EnabledFactory getConfigLoggingFactory()
    {
        return this.configLoggingFactory;
    }

    @JsonProperty("configLogging")
    public void setConfigLoggingFactory(EnabledFactory configLoggingFactory)
    {
        this.configLoggingFactory = configLoggingFactory;
    }

    @Override
    @JsonProperty("objectMapper")
    public ObjectMapperFactory getObjectMapperFactory()
    {
        return this.objectMapperFactory;
    }

    @JsonProperty("objectMapper")
    public void setObjectMapperFactory(ObjectMapperFactory objectMapperFactory)
    {
        this.objectMapperFactory = objectMapperFactory;
    }

    @Override
    @JsonProperty("clock")
    public ClockFactory getClockFactory()
    {
        return this.clockFactory;
    }

    @JsonProperty("clock")
    public void setClockFactory(ClockFactory clockFactory)
    {
        this.clockFactory = clockFactory;
    }

    @Override
    @JsonProperty("uuid")
    public UUIDSupplierFactory getUuidSupplierFactory()
    {
        return this.uuidFactory;
    }

    @JsonProperty("uuid")
    public void setUuidFactory(UUIDSupplierFactory uuidFactory)
    {
        this.uuidFactory = uuidFactory;
    }

    @Override
    @JsonProperty("jerseyHttpLogging")
    public JerseyHttpLoggingFactory getJerseyHttpLoggingFactory()
    {
        return this.jerseyHttpLoggingFactory;
    }

    @JsonProperty("jerseyHttpLogging")
    public void setJerseyHttpLoggingFactory(JerseyHttpLoggingFactory jerseyHttpLoggingFactory)
    {
        this.jerseyHttpLoggingFactory = jerseyHttpLoggingFactory;
    }

    @Override
    @JsonProperty("h2")
    public H2Factory getH2Factory()
    {
        return this.h2Factory;
    }

    @JsonProperty("h2")
    public void setH2(H2Factory h2Factory)
    {
        this.h2Factory = h2Factory;
    }

    @Override
    @JsonProperty("reladomo")
    public ReladomoFactory getReladomoFactory()
    {
        return this.reladomoFactory;
    }

    @JsonProperty("reladomo")
    public void setReladomoFactory(ReladomoFactory reladomoFactory)
    {
        this.reladomoFactory = reladomoFactory;
    }

    @Override
    public void initializeDataSources(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull LifecycleEnvironment lifecycle)
    {
        this.namedDataSourceConfiguration.initializeDataSources(metricRegistry, lifecycle);
    }

    @Override
    @JsonProperty("dataSources")
    public List<NamedDataSourceFactory> getNamedDataSourceFactories()
    {
        return this.namedDataSourceConfiguration.getNamedDataSourceFactories();
    }

    @JsonProperty("dataSources")
    public void setNamedDataSourceFactories(List<NamedDataSourceFactory> namedDataSourceFactories)
    {
        this.namedDataSourceConfiguration.setNamedDataSourceFactories(namedDataSourceFactories);
    }

    @Override
    @JsonIgnore
    public DataSource getDataSourceByName(@Nonnull String name)
    {
        return this.namedDataSourceConfiguration.getDataSourceByName(name);
    }

    @Override
    @JsonIgnore
    public MapIterable<String, ManagedDataSource> getDataSourcesByName()
    {
        return this.namedDataSourceConfiguration.getDataSourcesByName();
    }

    @Override
    @JsonProperty("ddlExecutors")
    public List<DdlExecutorFactory> getDdlExecutorFactories()
    {
        return this.ddlExecutorFactories;
    }

    @JsonProperty("ddlExecutors")
    public void setDdlExecutorFactories(List<DdlExecutorFactory> ddlExecutorFactories)
    {
        this.ddlExecutorFactories = ddlExecutorFactories;
    }

    @Override
    public void initializeConnectionManagers(@Nonnull MapIterable<String, ManagedDataSource> dataSourcesByName)
    {
        this.connectionManagerConfiguration.initializeConnectionManagers(dataSourcesByName);
    }

    @Override
    @JsonProperty("connectionManagers")
    public List<ConnectionManagerFactory> getConnectionManagerFactories()
    {
        return this.connectionManagerConfiguration.getConnectionManagerFactories();
    }

    @JsonProperty("connectionManagers")
    public void setConnectionManagerFactories(List<ConnectionManagerFactory> connectionManagerFactories)
    {
        this.connectionManagerConfiguration.setConnectionManagerFactories(connectionManagerFactories);
    }

    @Override
    @Nonnull
    @JsonProperty("graphQL")
    public GraphQLFactory getGraphQLFactory()
    {
        return this.graphQLFactory;
    }

    @JsonProperty("graphQL")
    public void setGraphQLFactory(@Nonnull GraphQLFactory graphQLFactory)
    {
        this.graphQLFactory = graphQLFactory;
    }

    @JsonIgnore
    @Override
    public SourcelessConnectionManager getConnectionManagerByName(@Nonnull String name)
    {
        SourcelessConnectionManager sourcelessConnectionManager =
                this.connectionManagerConfiguration.getConnectionManagerByName(name);
        return Objects.requireNonNull(
                sourcelessConnectionManager,
                () -> String.format("Could not find connection manager with name %s. Valid choices are %s",
                        name,
                        this.connectionManagerConfiguration.getConnectionManagersByName().keysView()));
    }

    @JsonIgnore
    @Override
    public org.eclipse.collections.api.map.ImmutableMap<String, SourcelessConnectionManager> getConnectionManagersByName()
    {
        return this.connectionManagerConfiguration.getConnectionManagersByName();
    }
}
