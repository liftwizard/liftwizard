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

package io.liftwizard.dropwizard.bundle.clock;

import java.time.Clock;

import javax.annotation.Nonnull;

import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public class ClockBundle
        implements ConfiguredBundle<ClockFactoryProvider>
{
    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(ClockFactoryProvider configuration, @Nonnull Environment environment)
    {
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.runWithMdc(configuration, environment);
        }
    }

    private void runWithMdc(
            ClockFactoryProvider configuration,
            @Nonnull Environment environment)
    {
        ClockFactory clockFactory = configuration.getClockFactory();
        Clock        clock        = clockFactory.createClock();
        ClockBinder  clockBinder  = new ClockBinder(clock);
        environment.jersey().register(clockBinder);
    }
}
