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

package com.liftwizard.dropwizard.configuration.auth.filter.impersonation;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter.Builder;
import org.eclipse.collections.api.list.ImmutableList;

@JsonTypeName("impersonation")
@AutoService(AuthFilterFactory.class)
public class ImpersonationAuthFilterFactory implements AuthFilterFactory
{
    @Nonnull
    @Override
    public AuthFilter<?, ImpersonatedPrincipal> createAuthFilter()
    {
        return new Builder<ImpersonatedPrincipal>()
                .setAuthenticator(new ImpersonationAuthenticator())
                .setPrefix("Impersonation")
                .buildAuthFilter();
    }

    @Override
    public ImmutableList<String> getMDCKeys()
    {
        return ImpersonationAuthenticator.getMDCKeys();
    }
}
