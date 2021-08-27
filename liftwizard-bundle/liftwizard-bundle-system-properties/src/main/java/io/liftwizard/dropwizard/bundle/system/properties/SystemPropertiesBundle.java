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

package io.liftwizard.dropwizard.bundle.system.properties;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.system.properties.SystemPropertiesFactory;
import io.liftwizard.dropwizard.configuration.system.properties.SystemPropertiesFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class SystemPropertiesBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemPropertiesBundle.class);

    @Override
    public int getPriority()
    {
        return -8;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        SystemPropertiesFactoryProvider systemPropertiesFactoryProvider = this.safeCastConfiguration(
                SystemPropertiesFactoryProvider.class,
                configuration);
        SystemPropertiesFactory systemPropertiesFactory = systemPropertiesFactoryProvider.getSystemPropertiesFactory();
        if (systemPropertiesFactory.getSystemProperties().isEmpty())
        {
            LOGGER.info("{} disabled.", this.getClass().getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        boolean strict = systemPropertiesFactory.isStrict();
        systemPropertiesFactory.getSystemProperties().forEach((key, value) ->
        {
            String oldValue = System.setProperty(key, value);
            if (strict && oldValue != null)
            {
                String error = String.format(
                        "Overwrote system property {%s:%s} with %s.",
                        key,
                        oldValue,
                        value);
                throw new IllegalStateException(error);
            }
        });

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }
}
