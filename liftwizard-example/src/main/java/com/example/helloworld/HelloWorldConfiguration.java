package com.example.helloworld;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.example.helloworld.core.Template;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableMap;
import com.smoketurner.dropwizard.graphql.GraphQLFactory;
import io.dropwizard.Configuration;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import io.liftwizard.dropwizard.configuration.clock.system.SystemClockFactory;
import io.liftwizard.dropwizard.configuration.config.logging.ConfigLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerProvider;
import io.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagersFactory;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourceProvider;
import io.liftwizard.dropwizard.configuration.datasource.NamedDataSourcesFactory;
import io.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactory;
import io.liftwizard.dropwizard.configuration.ddl.executor.DdlExecutorFactoryProvider;
import io.liftwizard.dropwizard.configuration.enabled.EnabledFactory;
import io.liftwizard.dropwizard.configuration.graphql.GraphQLFactoryProvider;
import io.liftwizard.dropwizard.configuration.h2.H2Factory;
import io.liftwizard.dropwizard.configuration.h2.H2FactoryProvider;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactory;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.liquibase.migration.LiquibaseMigrationFactory;
import io.liftwizard.dropwizard.configuration.liquibase.migration.LiquibaseMigrationFactoryProvider;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactory;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactoryProvider;
import io.liftwizard.dropwizard.configuration.reladomo.ReladomoFactory;
import io.liftwizard.dropwizard.configuration.reladomo.ReladomoFactoryProvider;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import io.liftwizard.dropwizard.configuration.uuid.system.SystemUUIDSupplierFactory;

@JsonPropertyOrder({"template", "defaultName", "viewRendererConfiguration"})
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
        ConnectionManagerProvider,
        GraphQLFactoryProvider,
        LiquibaseMigrationFactoryProvider
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

    // include-namedDataSourcesFactory
    @JsonUnwrapped
    private @Valid @NotNull NamedDataSourcesFactory   namedDataSourcesFactory   =
            new NamedDataSourcesFactory();
    // include-namedDataSourcesFactory

    // include-connectionManagersFactory
    @JsonUnwrapped
    private @Valid @NotNull ConnectionManagersFactory connectionManagersFactory =
            new ConnectionManagersFactory();
    // include-connectionManagersFactory

    // include-liquibaseMigrationFactory
    private @Valid @NotNull LiquibaseMigrationFactory liquibaseMigrationFactory =
            new LiquibaseMigrationFactory();
    // include-liquibaseMigrationFactory

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

    @JsonProperty("dataSources")
    @JsonUnwrapped
    public NamedDataSourcesFactory getNamedDataSourcesFactory()
    {
        return this.namedDataSourcesFactory;
    }

    @JsonProperty("dataSources")
    @JsonUnwrapped
    public void setNamedDataSourcesFactory(NamedDataSourcesFactory namedDataSourcesFactory)
    {
        this.namedDataSourcesFactory = namedDataSourcesFactory;
    }

    @JsonProperty("connectionManagers")
    @JsonUnwrapped
    @Override
    public ConnectionManagersFactory getConnectionManagersFactory()
    {
        return this.connectionManagersFactory;
    }

    @JsonProperty("connectionManagers")
    @JsonUnwrapped
    public void setConnectionManagersFactory(ConnectionManagersFactory connectionManagersFactory)
    {
        this.connectionManagersFactory = connectionManagersFactory;
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

    @JsonProperty("liquibase")
    @Override
    public LiquibaseMigrationFactory getLiquibaseMigrationFactory()
    {
        return this.liquibaseMigrationFactory;
    }

    @JsonProperty("liquibase")
    public void setLiquibaseMigrationFactory(LiquibaseMigrationFactory liquibaseMigrationFactory)
    {
        this.liquibaseMigrationFactory = liquibaseMigrationFactory;
    }
}
