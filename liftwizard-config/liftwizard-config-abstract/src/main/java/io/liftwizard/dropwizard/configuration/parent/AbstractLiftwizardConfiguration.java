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

package io.liftwizard.dropwizard.configuration.parent;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.dropwizard.Configuration;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactoryProvider;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import io.liftwizard.dropwizard.configuration.clock.system.SystemClockFactory;
import io.liftwizard.dropwizard.configuration.config.logging.ConfigLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.cors.CorsFactory;
import io.liftwizard.dropwizard.configuration.cors.CorsFactoryProvider;
import io.liftwizard.dropwizard.configuration.enabled.EnabledFactory;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactory;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactory;
import io.liftwizard.dropwizard.configuration.object.mapper.ObjectMapperFactoryProvider;
import io.liftwizard.dropwizard.configuration.system.properties.SystemPropertiesFactory;
import io.liftwizard.dropwizard.configuration.system.properties.SystemPropertiesFactoryProvider;

@JsonPropertyOrder({
        "server",
        "logging",
        "metrics",
        "klass",
        "configLogging",
        "objectMapper",
        "cors",
        "authFilters",
        "jerseyHttpLogging",
})
public abstract class AbstractLiftwizardConfiguration
        extends Configuration
        implements ConfigLoggingFactoryProvider,
        CorsFactoryProvider,
        AuthFilterFactoryProvider,
        ObjectMapperFactoryProvider,
        JerseyHttpLoggingFactoryProvider,
        ClockFactoryProvider,
        SystemPropertiesFactoryProvider
{
    // region General
    @Valid
    @NotNull
    private ClockFactory clockFactory = new SystemClockFactory();
    @Valid
    @NotNull
    private SystemPropertiesFactory systemPropertiesFactory = new SystemPropertiesFactory();
    // endregion General

    // region Services
    @Valid
    @NotNull
    private EnabledFactory configLoggingFactory = new EnabledFactory();
    @Valid
    @NotNull
    private ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory();
    @Valid
    @NotNull
    private JerseyHttpLoggingFactory jerseyHttpLoggingFactory = new JerseyHttpLoggingFactory();
    @Valid
    @NotNull
    private CorsFactory corsFactory = new CorsFactory();
    @Valid
    @NotNull
    private List<AuthFilterFactory> authFilterFactories = List.of();
    // endregion Services

    // region General
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
    @JsonProperty("systemProperties")
    public SystemPropertiesFactory getSystemPropertiesFactory()
    {
        return this.systemPropertiesFactory;
    }

    @JsonProperty("systemProperties")
    public void setSystemPropertiesFactory(SystemPropertiesFactory systemPropertiesFactory)
    {
        this.systemPropertiesFactory = systemPropertiesFactory;
    }
    // endregion

    // region Services
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
    @JsonProperty("cors")
    public CorsFactory getCorsFactory()
    {
        return this.corsFactory;
    }

    @JsonProperty("cors")
    public void setCorsFactory(CorsFactory corsFactory)
    {
        this.corsFactory = corsFactory;
    }

    @Override
    @JsonProperty("authFilters")
    public List<AuthFilterFactory> getAuthFilterFactories()
    {
        return this.authFilterFactories;
    }

    @JsonProperty("authFilters")
    public void setAuthFilterFactories(List<AuthFilterFactory> authFilterFactories)
    {
        this.authFilterFactories = authFilterFactories;
    }
    // endregion Services
}
