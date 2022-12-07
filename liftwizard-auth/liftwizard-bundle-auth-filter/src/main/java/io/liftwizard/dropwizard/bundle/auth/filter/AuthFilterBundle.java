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

package io.liftwizard.dropwizard.bundle.auth.filter;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider.Binder;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactoryProvider;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class AuthFilterBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilterBundle.class);

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        AuthFilterFactoryProvider authFilterFactoryProvider =
                this.safeCastConfiguration(AuthFilterFactoryProvider.class, configuration);

        List<AuthFilterFactory> authFilterFactories = authFilterFactoryProvider.getAuthFilterFactories();

        List<AuthFilter<?, ? extends Principal>> authFilters = this.getAuthFilters(authFilterFactories);

        if (authFilters.isEmpty())
        {
            LOGGER.warn("{} disabled.", this.getClass().getSimpleName());
            return;
        }

        List<String> authFilterNames = authFilters
                .stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .collect(Collectors.toList());

        LOGGER.info("Running {} with auth filters {}.", this.getClass().getSimpleName(), authFilterNames);

        environment.jersey().register(this.getAuthDynamicFeature(authFilters));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new Binder<>(Principal.class));

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }

    @Nonnull
    private List<AuthFilter<?, ? extends Principal>> getAuthFilters(List<AuthFilterFactory> authFilterFactories)
    {
        return authFilterFactories
                .stream()
                .map(AuthFilterFactory::createAuthFilter)
                .collect(Collectors.toList());
    }

    @Nonnull
    private AuthDynamicFeature getAuthDynamicFeature(List<AuthFilter<?, ? extends Principal>> authFilters)
    {
        ChainedAuthFilter chainedAuthFilter = new ChainedAuthFilter(authFilters);
        return new AuthDynamicFeature(chainedAuthFilter);
    }
}
