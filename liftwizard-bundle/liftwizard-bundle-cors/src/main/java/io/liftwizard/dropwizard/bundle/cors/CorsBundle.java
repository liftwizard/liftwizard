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

package io.liftwizard.dropwizard.bundle.cors;

import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import com.google.auto.service.AutoService;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.cors.CorsFactory;
import io.liftwizard.dropwizard.configuration.cors.CorsFactoryProvider;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class CorsBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CorsBundle.class);

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        CorsFactoryProvider corsFactoryProvider = this.safeCastConfiguration(CorsFactoryProvider.class, configuration);
        CorsFactory         corsFactory         = corsFactoryProvider.getCorsFactory();
        if (!corsFactory.isEnabled())
        {
            LOGGER.info("{} disabled.", CorsBundle.class.getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", CorsBundle.class.getSimpleName());

        // https://stackoverflow.com/a/25801822
        Dynamic cors = environment.servlets().addFilter(corsFactory.getFilterName(), CrossOriginFilter.class);

        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, corsFactory.getAllowedOrigins());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, corsFactory.getAllowedHeaders());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, corsFactory.getAllowedMethods());
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, corsFactory.getAllowCredentials());
        cors.addMappingForUrlPatterns(
                EnumSet.allOf(DispatcherType.class),
                true,
                corsFactory.getUrlPatterns().toArray(new String[]{}));

        LOGGER.info("Completing {}.", CorsBundle.class.getSimpleName());
    }
}
