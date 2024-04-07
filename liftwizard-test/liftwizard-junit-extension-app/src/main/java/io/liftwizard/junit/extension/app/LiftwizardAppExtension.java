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

package io.liftwizard.junit.extension.app;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.junit5.DropwizardExtension;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * LiftwizardAppExtension is a fork of DropwizardAppExtension that is non-static and uses before/after instead of beforeAll/afterAll.
 */
@SuppressWarnings("unused")
public class LiftwizardAppExtension<C extends Configuration>
        implements DropwizardExtension, BeforeEachCallback, AfterEachCallback
{
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 1000;
    private static final int DEFAULT_READ_TIMEOUT_MS    = 5000;

    private final DropwizardTestSupport<C> testSupport;

    private final AtomicInteger recursiveCallCount = new AtomicInteger(0);

    @Nullable
    private Client client;

    public LiftwizardAppExtension(Class<? extends Application<C>> applicationClass)
    {
        this(applicationClass, (String) null);
    }

    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            @Nullable String configPath,
            ConfigOverride... configOverrides)
    {
        this(applicationClass, configPath, (String) null, configOverrides);
    }

    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            @Nullable String configPath,
            @Nullable String customPropertyPrefix,
            ConfigOverride... configOverrides)
    {
        this(applicationClass, configPath, customPropertyPrefix, ServerCommand::new, configOverrides);
    }

    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            @Nullable String configPath,
            @Nullable String customPropertyPrefix,
            Function<Application<C>, Command> commandInstantiator,
            ConfigOverride... configOverrides)
    {
        this(new DropwizardTestSupport<>(
                applicationClass,
                configPath,
                customPropertyPrefix,
                commandInstantiator,
                configOverrides));
    }

    public LiftwizardAppExtension(DropwizardTestSupport<C> testSupport)
    {
        this.testSupport = testSupport;
    }

    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            @Nullable String configPath,
            ConfigurationSourceProvider configSourceProvider,
            ConfigOverride... configOverrides)
    {
        this(applicationClass, configPath, configSourceProvider, null, configOverrides);
    }

    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            @Nullable String configPath,
            ConfigurationSourceProvider configSourceProvider,
            @Nullable String customPropertyPrefix,
            ConfigOverride... configOverrides)
    {
        this(
                applicationClass,
                configPath,
                configSourceProvider,
                customPropertyPrefix,
                ServerCommand::new,
                configOverrides);
    }

    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            @Nullable String configPath,
            ConfigurationSourceProvider configSourceProvider,
            @Nullable String customPropertyPrefix,
            Function<Application<C>, Command> commandInstantiator,
            ConfigOverride... configOverrides)
    {
        this(new DropwizardTestSupport<>(
                applicationClass,
                configPath,
                configSourceProvider,
                customPropertyPrefix,
                commandInstantiator,
                configOverrides));
    }

    /**
     * Alternate constructor that allows specifying exact Configuration object to
     * use, instead of reading a resource and binding it as Configuration object.
     *
     * @since 0.9
     */
    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            C configuration)
    {
        this(new DropwizardTestSupport<>(applicationClass, configuration));
    }

    /**
     * Alternate constructor that allows specifying the command the Dropwizard application is started with.
     *
     * @since 1.1.0
     */
    public LiftwizardAppExtension(
            Class<? extends Application<C>> applicationClass,
            C configuration, Function<Application<C>, Command> commandInstantiator)
    {
        this(new DropwizardTestSupport<>(applicationClass, configuration, commandInstantiator));
    }

    public LiftwizardAppExtension<C> manage(Managed managed)
    {
        return this.addListener(new AbstractServiceListener<>()
        {
            @Override
            public void onRun(C configuration, Environment environment, LiftwizardAppExtension<C> rule)
            {
                environment.lifecycle().manage(managed);
            }
        });
    }

    public LiftwizardAppExtension<C> addListener(AbstractServiceListener<C> listener)
    {
        this.testSupport.addListener(new DropwizardTestSupport.ServiceListener<>()
        {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardTestSupport<C> rule)
                    throws Exception
            {
                listener.onRun(configuration, environment, LiftwizardAppExtension.this);
            }

            @Override
            public void onStop(DropwizardTestSupport<C> rule)
            {
                listener.onStop(LiftwizardAppExtension.this);
            }
        });
        return this;
    }

    @Override
    public void beforeEach(ExtensionContext context)
            throws Exception
    {
        this.before();
    }

    @Override
    public void before()
            throws Exception
    {
        if (this.recursiveCallCount.getAndIncrement() == 0)
        {
            this.testSupport.before();
        }
    }

    @Override
    public void after()
    {
        if (this.recursiveCallCount.decrementAndGet() == 0)
        {
            this.testSupport.after();
            synchronized (this)
            {
                if (this.client != null)
                {
                    this.client.close();
                    this.client = null;
                }
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        this.after();
    }

    public C getConfiguration()
    {
        return this.testSupport.getConfiguration();
    }

    public int getLocalPort()
    {
        return this.testSupport.getLocalPort();
    }

    public int getPort(int connectorIndex)
    {
        return this.testSupport.getPort(connectorIndex);
    }

    public int getAdminPort()
    {
        return this.testSupport.getAdminPort();
    }

    public Application<C> newApplication()
    {
        return this.testSupport.newApplication();
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <A extends Application<C>> A getApplication()
    {
        return this.testSupport.getApplication();
    }

    public Environment getEnvironment()
    {
        return this.testSupport.getEnvironment();
    }

    public DropwizardTestSupport<C> getTestSupport()
    {
        return this.testSupport;
    }

    /**
     * Returns a new HTTP Jersey {@link Client} for performing HTTP requests against the tested
     * Dropwizard server. The client can be reused across different tests and automatically
     * closed along with the server. The client can be augmented by overriding the
     * {@link #clientBuilder()} method.
     *
     * @return a new {@link Client} managed by the extension.
     */
    public Client client()
    {
        synchronized (this)
        {
            if (this.client == null)
            {
                this.client = this.clientBuilder().build();
            }
            return this.client;
        }
    }

    protected JerseyClientBuilder clientBuilder()
    {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new GrizzlyConnectorProvider())
                .register(new JacksonFeature(this.getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_MS)
                .property(ClientProperties.READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS)
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
        return new JerseyClientBuilder().withConfig(clientConfig);
    }

    public ObjectMapper getObjectMapper()
    {
        return this.testSupport.getObjectMapper();
    }

    public abstract static class AbstractServiceListener<T extends Configuration>
    {
        public void onRun(T configuration, Environment environment, LiftwizardAppExtension<T> rule)
        {
            // Default NOP
        }

        public void onStop(LiftwizardAppExtension<T> rule)
        {
            // Default NOP
        }
    }
}
