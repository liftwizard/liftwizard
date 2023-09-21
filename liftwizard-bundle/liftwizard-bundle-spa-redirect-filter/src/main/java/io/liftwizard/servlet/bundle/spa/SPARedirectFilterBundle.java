/*
 * Copyright 2023 Craig Motlin
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

package io.liftwizard.servlet.bundle.spa;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Environment;
import io.liftwizard.servlet.config.spa.SPARedirectFilterFactory;
import io.liftwizard.servlet.filter.spa.SPARedirectFilter;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SPARedirectFilterBundle<T>
        implements ConfiguredBundle<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SPARedirectFilterBundle.class);

    public abstract SPARedirectFilterFactory getSPARedirectFilterFactory(T configuration);

    @Override
    public void run(T configuration, Environment environment)
    {
        SPARedirectFilterFactory factory = this.getSPARedirectFilterFactory(configuration);
        handleRegistration(this, environment, factory);
    }

    public static void handleRegistration(
            Object bundle,
            Environment environment,
            SPARedirectFilterFactory factory)
    {
        if (factory == null || !factory.isEnabled())
        {
            LOGGER.info("{} disabled.", bundle.getClass().getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", bundle.getClass().getSimpleName());

        String                redirectSPAPage       = factory.getRedirectSPAPage();
        String                cacheControlHeader    = factory.getCacheControlHeader();
        ImmutableList<String> wellKnownPathPrefixes = Lists.immutable.withAll(factory.getWellKnownPathPrefixes());

        var spaRedirectFilter = new SPARedirectFilter(
                redirectSPAPage,
                cacheControlHeader,
                wellKnownPathPrefixes);

        environment
                .servlets()
                .addFilter("spaRedirectFilter", spaRedirectFilter)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
