package com.example.helloworld;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.example.helloworld.core.Template;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.liftwizard.dropwizard.configuration.config.logging.ConfigLoggingFactoryProvider;
import com.liftwizard.dropwizard.configuration.enabled.EnabledFactory;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

public class HelloWorldConfiguration
        extends Configuration
        implements ConfigLoggingFactoryProvider
{
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @NotNull
    private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

    private @NotNull @Valid EnabledFactory      configLogging = new EnabledFactory(true);

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

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
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
    public EnabledFactory getConfigLoggingFactory()
    {
        return this.configLogging;
    }

    public void setConfigLogging(EnabledFactory configLogging)
    {
        this.configLogging = configLogging;
    }
}
