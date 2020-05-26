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

package com.liftwizard.dropwizard.configuration.auth.filter.firebase;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import com.liftwizard.firebase.principal.FirebasePrincipal;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter.Builder;
import org.eclipse.collections.api.list.ImmutableList;

@JsonTypeName("firebase")
@AutoService(AuthFilterFactory.class)
public class FirebaseAuthFilterFactory implements AuthFilterFactory
{
    private @Valid @NotNull String databaseUrl;
    private @Valid @NotNull String firebaseConfig;

    @Nonnull
    @Override
    public AuthFilter<?, FirebasePrincipal> createAuthFilter()
    {
        FirebaseAuth firebaseAuthFactory = new FirebaseAuth(
                this.databaseUrl,
                this.firebaseConfig);
        com.google.firebase.auth.FirebaseAuth firebaseAuth = firebaseAuthFactory.getFirebaseAuth();

        Authenticator<String, FirebasePrincipal> authenticator = new FirebaseOAuthAuthenticator(firebaseAuth);

        return new Builder<FirebasePrincipal>()
                .setAuthenticator(authenticator)
                .setPrefix("Bearer")
                .buildAuthFilter();
    }

    @Override
    public ImmutableList<String> getMDCKeys()
    {
        return FirebaseOAuthAuthenticator.getMDCKeys();
    }

    @JsonProperty
    public String getDatabaseUrl()
    {
        return this.databaseUrl;
    }

    @JsonProperty
    public void setDatabaseUrl(String databaseUrl)
    {
        this.databaseUrl = databaseUrl;
    }

    @JsonProperty
    public String getFirebaseConfig()
    {
        return this.firebaseConfig;
    }

    @JsonProperty
    public void setFirebaseConfig(String firebaseConfig)
    {
        this.firebaseConfig = firebaseConfig;
    }
}
