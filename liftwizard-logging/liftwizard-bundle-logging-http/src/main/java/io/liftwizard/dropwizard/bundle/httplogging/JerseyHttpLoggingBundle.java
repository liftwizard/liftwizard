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

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactory;
import io.liftwizard.dropwizard.configuration.http.logging.JerseyHttpLoggingFactoryProvider;
import io.liftwizard.servlet.logging.feature.LoggingConfig;
import io.liftwizard.servlet.logging.feature.LoggingFeature;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
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
    private final BiConsumer<StructuredArguments, Optional<String>> structuredLogger;

    public JerseyHttpLoggingBundle(@Nonnull BiConsumer<StructuredArguments, Optional<String>> structuredLogger)
    {
        this.structuredLogger = Objects.requireNonNull(structuredLogger);
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

        int maxEntitySize = Math.toIntExact(factory.getMaxEntitySize().toBytes());

        LoggingConfig loggingBuilder = new LoggingConfig(
                factory.isLogRequests(),
                factory.isLogRequestBodies(),
                factory.isLogResponses(),
                factory.isLogResponseBodies(),
                factory.isLogExcludedHeaderNames(),
                factory.getIncludedHeaders(),
                maxEntitySize);

        JerseyEnvironment jersey = environment.jersey();
        if (loggingBuilder.isLogRequests() || loggingBuilder.isLogResponses())
        {
            LoggingFeature loggingFeature = new LoggingFeature(loggingBuilder, this.structuredLogger);
            jersey.register(loggingFeature);
        }

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }
}
