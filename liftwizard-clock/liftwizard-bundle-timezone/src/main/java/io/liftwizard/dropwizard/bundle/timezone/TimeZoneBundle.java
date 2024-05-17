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

package io.liftwizard.dropwizard.bundle.timezone;

import java.util.TimeZone;

import javax.annotation.Nonnull;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.configuration.timezone.TimeZoneFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public class TimeZoneBundle
        implements ConfiguredBundle<TimeZoneFactoryProvider>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneBundle.class);

    @Override
    public void run(TimeZoneFactoryProvider configuration, @Nonnull Environment environment)
    {
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.runWithMdc(configuration, environment);
        }
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    private void runWithMdc(
            TimeZoneFactoryProvider configuration,
            @Nonnull Environment environment)
    {
        TimeZone timeZone = configuration.getTimeZone();
        LOGGER.info("Setting default TimeZone to: {}", timeZone);
        TimeZone.setDefault(timeZone);
        TimezoneBinder timezoneBinder = new TimezoneBinder(timeZone);
        environment.jersey().register(timezoneBinder);
    }
}
