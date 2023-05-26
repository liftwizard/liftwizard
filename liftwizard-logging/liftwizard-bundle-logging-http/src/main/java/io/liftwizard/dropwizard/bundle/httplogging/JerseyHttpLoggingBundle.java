/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.dropwizard.bundle.httplogging;

import java.security.Principal;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactory;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactoryProvider;
import io.liftwizard.servlet.logging.feature.LoggingConfig;
import io.liftwizard.servlet.logging.filter.ServerLoggingFilter;
import io.liftwizard.servlet.logging.filter.ServerLoggingRequestFilter;
import io.liftwizard.servlet.logging.filter.ServerLoggingResponseFilter;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs all requests and responses to slf4j. The maxEntitySize is configurable.
 *
 * @see <a href="https://liftwizard.io/docs/logging/JerseyHttpLoggingBundle#jerseyhttploggingbundle">https://liftwizard.io/docs/logging/JerseyHttpLoggingBundle#jerseyhttploggingbundle</a>
 */
public class JerseyHttpLoggingBundle
        implements ConfiguredBundle<JerseyHttpLoggingFactoryProvider>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyHttpLoggingBundle.class);

    @Nonnull
    private final Consumer<StructuredArguments>      structuredLogger;
    @Nonnull
    private final Function<Principal, Map<String, Object>> principalBuilder;

    public JerseyHttpLoggingBundle(@Nonnull Consumer<StructuredArguments> structuredLogger)
    {
        this(structuredLogger, principal -> Map.of("name", principal.getName()));
    }

    public JerseyHttpLoggingBundle(
            @Nonnull Consumer<StructuredArguments> structuredLogger,
            @Nonnull Function<Principal, Map<String, Object>> principalBuilder)
    {
        this.structuredLogger = Objects.requireNonNull(structuredLogger);
        this.principalBuilder = Objects.requireNonNull(principalBuilder);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(JerseyHttpLoggingFactoryProvider configuration, Environment environment)
            throws Exception
    {
        JerseyHttpLoggingFactory factory = configuration.getJerseyHttpLoggingFactory();
        if (!factory.isEnabled())
        {
            LOGGER.info("{} disabled.", this.getClass().getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        Clock clock = getClock(configuration);

        int maxEntitySize = Math.toIntExact(factory.getMaxEntitySize().toBytes());

        LoggingConfig loggingConfig = new LoggingConfig(
                factory.isLogRequests(),
                factory.isLogRequestBodies(),
                factory.isLogResponses(),
                factory.isLogResponseBodies(),
                factory.isLogRequestHeaderNames(),
                factory.isLogExcludedRequestHeaderNames(),
                factory.isLogResponseHeaderNames(),
                factory.isLogExcludedResponseHeaderNames(),
                Lists.immutable.withAll(factory.getIncludedRequestHeaders()),
                Lists.immutable.withAll(factory.getIncludedResponseHeaders()),
                maxEntitySize);

        if (loggingConfig.isLogRequests())
        {
            var loggingRequestFilter = new ServerLoggingRequestFilter(this.principalBuilder);
            environment.jersey().register(loggingRequestFilter);
        }

        if (loggingConfig.isLogResponses())
        {
            var loggingResponseFilter = new ServerLoggingResponseFilter();
            environment.jersey().register(loggingResponseFilter);
        }

        ServerLoggingFilter loggingFilter = new ServerLoggingFilter(loggingConfig, this.structuredLogger, clock);
        environment
                .servlets()
                .addFilter("ServerLoggingFilter", loggingFilter)
                .addMappingForUrlPatterns(null, true, "/*");

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }

    private static Clock getClock(JerseyHttpLoggingFactoryProvider configuration)
    {
        if (!(configuration instanceof ClockFactoryProvider))
        {
            LOGGER.warn(
                    "Configuration {} does not implement {}. Using system clock.",
                    configuration.getClass().getSimpleName(),
                    ClockFactoryProvider.class.getSimpleName());
            return Clock.systemUTC();
        }

        ClockFactoryProvider clockFactoryProvider = (ClockFactoryProvider) configuration;
        ClockFactory         clockFactory         = clockFactoryProvider.getClockFactory();
        return clockFactory.createClock();
    }
}
