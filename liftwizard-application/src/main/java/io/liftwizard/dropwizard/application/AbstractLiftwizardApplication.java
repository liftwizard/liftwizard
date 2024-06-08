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

package io.liftwizard.dropwizard.application;

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.qos.logback.classic.Level;
import com.gs.reladomo.serial.jackson.JacksonReladomoModule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.bundles.redirect.HttpsRedirect;
import io.dropwizard.bundles.redirect.RedirectBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.clock.ClockBundle;
import io.liftwizard.dropwizard.bundle.dynamic.bundles.DynamicBundlesBundle;
import io.liftwizard.dropwizard.bundle.environment.config.EnvironmentConfigBundle;
import io.liftwizard.dropwizard.bundle.uuid.UUIDBundle;
import io.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import io.liftwizard.dropwizard.configuration.factory.JsonConfigurationFactoryFactory;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import io.liftwizard.dropwizard.healthcheck.reladomo.ReladomoHealthCheck;
import io.liftwizard.dropwizard.task.reladomo.clear.cache.ReladomoClearCacheTask;
import org.marmelo.dropwizard.metrics.bundles.MetricsUIBundle;

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
        this.initializeEarlyBundles(bootstrap);
        this.initializeDynamicBundles(bootstrap);
        this.initializeBundles(bootstrap);
    }

    protected void initializeConfiguration(@Nonnull Bootstrap<T> bootstrap)
    {
        bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
        bootstrap.addBundle(new EnvironmentConfigBundle());
    }

    protected void initializeCommands(@Nonnull Bootstrap<T> bootstrap)
    {
    }

    protected void initializeEarlyBundles(@Nonnull Bootstrap<T> bootstrap)
    {
        bootstrap.addBundle(new ClockBundle());
        HttpsRedirect  httpsRedirect  = new HttpsRedirect();
        RedirectBundle redirectBundle = new RedirectBundle(httpsRedirect);
        bootstrap.addBundle(redirectBundle);
        bootstrap.addBundle(new UUIDBundle());
        bootstrap.addBundle(new MetricsUIBundle("/dashboard/*"));
    }

    protected void initializeBundles(@Nonnull Bootstrap<T> bootstrap)
    {
    }

    protected void initializeDynamicBundles(@Nonnull Bootstrap<T> bootstrap)
    {
        bootstrap.addBundle(new DynamicBundlesBundle());
    }

    @Override
    public void run(@Nonnull T configuration, @Nonnull Environment environment) throws Exception
    {
        this.registerJacksonModules(environment);
        this.registerHealthChecks(environment);
        this.registerTasks(environment);
    }

    protected void registerJacksonModules(@Nonnull Environment environment)
    {
        environment.getObjectMapper().registerModule(new JacksonReladomoModule());
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
