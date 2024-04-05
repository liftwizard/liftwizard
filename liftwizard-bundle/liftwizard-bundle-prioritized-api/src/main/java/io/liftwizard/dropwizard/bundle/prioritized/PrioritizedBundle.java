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

package io.liftwizard.dropwizard.bundle.prioritized;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnull;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public interface PrioritizedBundle
        extends ConfiguredBundle<Object>
{
    Logger LOGGER = LoggerFactory.getLogger(PrioritizedBundle.class);

    String MDC_BUNDLE   = "liftwizard.bundle";
    String MDC_PRIORITY = "liftwizard.priority";

    default int getPriority()
    {
        return 0;
    }

    default <C> C safeCastConfiguration(Class<C> aClass, Object configuration)
    {
        if (aClass.isInstance(configuration))
        {
            return aClass.cast(configuration);
        }

        String message = "Expected configuration to implement %s but found %s".formatted(
                aClass.getCanonicalName(),
                configuration.getClass().getCanonicalName());
        throw new IllegalStateException(message);
    }

    @Override
    default void initialize(@Nonnull Bootstrap<?> bootstrap)
    {
        Instant start = Instant.now();
        try (
                MDCCloseable mdc1 = MDC.putCloseable(MDC_BUNDLE, this.getClass().getSimpleName());
                MDCCloseable mdc2 = MDC.putCloseable(MDC_PRIORITY, String.valueOf(this.getPriority())))
        {
            this.initializeWithMdc(bootstrap);
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        String durationPrettyString = duration.toString().substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
        LOGGER.info("{} initialized in {}", this.getClass().getSimpleName(), durationPrettyString);
    }

    default void initializeWithMdc(@Nonnull Bootstrap<?> bootstrap)
    {
    }

    @Override
    default void run(@Nonnull Object configuration, @Nonnull Environment environment) throws Exception
    {
        Instant start = Instant.now();
        try (
                MDCCloseable mdc1 = MDC.putCloseable(MDC_BUNDLE, this.getClass().getSimpleName());
                MDCCloseable mdc2 = MDC.putCloseable(MDC_PRIORITY, String.valueOf(this.getPriority())))
        {
            this.runWithMdc(configuration, environment);
        }
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        String durationPrettyString = duration.toString().substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
        LOGGER.info("{} ran in {}", this.getClass().getSimpleName(), durationPrettyString);
    }

    void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment) throws Exception;
}
