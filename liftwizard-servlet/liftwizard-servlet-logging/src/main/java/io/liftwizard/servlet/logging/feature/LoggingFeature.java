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

package io.liftwizard.servlet.logging.feature;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import io.liftwizard.servlet.logging.filter.AbstractLoggingFilter;
import io.liftwizard.servlet.logging.filter.ServerLoggingRequestFilter;
import io.liftwizard.servlet.logging.filter.ServerLoggingResponseFilter;
import io.liftwizard.servlet.logging.interceptor.LoggingInterceptor;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;

public class LoggingFeature
        implements Feature
{
    @Nonnull
    private final LoggingConfig loggingConfig;

    @Nonnull
    private final BiConsumer<StructuredArguments, Optional<String>> logger;

    public LoggingFeature(
            @Nonnull LoggingConfig loggingConfig,
            @Nonnull BiConsumer<StructuredArguments, Optional<String>> logger)
    {
        this.loggingConfig = Objects.requireNonNull(loggingConfig);
        this.logger        = Objects.requireNonNull(logger);
    }

    @Override
    public boolean configure(FeatureContext context)
    {
        if (this.loggingConfig.isLogRequestBodies()
                || this.loggingConfig.isLogResponseBodies())
        {
            LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
            context.register(loggingInterceptor);
        }

        if (this.loggingConfig.isLogRequests())
        {
            AbstractLoggingFilter loggingRequestFilter = new ServerLoggingRequestFilter(
                    this.loggingConfig,
                    this.logger);
            context.register(loggingRequestFilter);
        }

        if (this.loggingConfig.isLogResponses())
        {
            AbstractLoggingFilter loggingResponseFilter = new ServerLoggingResponseFilter(
                    this.loggingConfig,
                    this.logger);
            context.register(loggingResponseFilter);
        }

        return true;
    }
}
