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

package com.liftwizard.dropwizard.application;

import java.util.EnumSet;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule;
import com.gs.reladomo.serial.jackson.JacksonReladomoModule;
import com.liftwizard.dropwizard.bundle.clock.ClockBundle;
import com.liftwizard.dropwizard.bundle.dynamic.bundles.DynamicBundlesBundle;
import com.liftwizard.dropwizard.bundle.environment.config.EnvironmentConfigBundle;
import com.liftwizard.dropwizard.bundle.uuid.UUIDBundle;
import com.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import com.liftwizard.dropwizard.configuration.factory.JsonConfigurationFactoryFactory;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import com.liftwizard.dropwizard.healthcheck.reladomo.ReladomoHealthCheck;
import com.liftwizard.dropwizard.task.reladomo.clear.cache.ReladomoClearCacheTask;
import com.liftwizard.servlet.logging.correlation.id.CorrelationIdFilter;
import com.liftwizard.servlet.logging.resource.info.ResourceInfoLoggingFilter;
import com.liftwizard.servlet.logging.structured.argument.StructuredArgumentLoggingFilter;
import com.liftwizard.servlet.logging.structured.duration.DurationStructuredLoggingFilter;
import com.liftwizard.servlet.logging.structured.reladomo.ReladomoStructuredLoggingFilter;
import com.liftwizard.servlet.logging.structured.status.info.StatusInfoStructuredLoggingFilter;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.bundles.redirect.HttpsRedirect;
import io.dropwizard.bundles.redirect.RedirectBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class AbstractLiftwizardApplication<T extends Configuration & UUIDSupplierFactoryProvider & ClockFactoryProvider>
        extends Application<T>
{
    protected final String name;

    protected AbstractLiftwizardApplication(String name)
    {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    protected Level bootstrapLogLevel()
    {
        return Level.INFO;
    }

    @Nonnull
    @Override
    public final String getName()
    {
        return this.name;
    }

    @Override
    public void initialize(@Nonnull Bootstrap<T> bootstrap)
    {
        super.initialize(bootstrap);

        this.initializeConfiguration(bootstrap);
        this.initializeCommands(bootstrap);
        this.initializeBundles(bootstrap);
        this.initializeDynamicBundles(bootstrap);
    }

    protected void initializeConfiguration(@Nonnull Bootstrap<T> bootstrap)
    {
        bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
        bootstrap.addBundle(new EnvironmentConfigBundle());
    }

    protected void initializeCommands(@Nonnull Bootstrap<T> bootstrap)
    {
    }

    protected void initializeBundles(@Nonnull Bootstrap<T> bootstrap)
    {
        HttpsRedirect  httpsRedirect  = new HttpsRedirect();
        RedirectBundle redirectBundle = new RedirectBundle(httpsRedirect);
        bootstrap.addBundle(redirectBundle);
        bootstrap.addBundle(new UUIDBundle());
        bootstrap.addBundle(new ClockBundle());
    }

    protected void initializeDynamicBundles(@Nonnull Bootstrap<T> bootstrap)
    {
        bootstrap.addBundle(new DynamicBundlesBundle());
    }

    @Override
    public void run(@Nonnull T configuration, @Nonnull Environment environment) throws Exception
    {
        this.registerJacksonModules(environment);
        this.registerLoggingFilters(environment);
        this.registerHealthChecks(environment);
        this.registerTasks(environment);
    }

    protected void registerJacksonModules(@Nonnull Environment environment)
    {
        environment.getObjectMapper().registerModule(new EclipseCollectionsModule());
        environment.getObjectMapper().registerModule(new JacksonReladomoModule());
    }

    protected void registerLoggingFilters(@Nonnull Environment environment)
    {
        environment.getApplicationContext().addFilter(
                StructuredArgumentLoggingFilter.class,
                "/*",
                EnumSet.of(DispatcherType.REQUEST));

        environment.jersey().register(CorrelationIdFilter.class);
        environment.jersey().register(ResourceInfoLoggingFilter.class);
        environment.jersey().register(StatusInfoStructuredLoggingFilter.class);
        environment.jersey().register(DurationStructuredLoggingFilter.class);
        environment.jersey().register(ReladomoStructuredLoggingFilter.class);
    }

    protected void registerHealthChecks(@Nonnull Environment environment)
    {
        environment.healthChecks().register("reladomo", new ReladomoHealthCheck());
    }

    protected void registerTasks(@Nonnull Environment environment)
    {
        environment.admin().addTask(new ReladomoClearCacheTask());
    }
}
