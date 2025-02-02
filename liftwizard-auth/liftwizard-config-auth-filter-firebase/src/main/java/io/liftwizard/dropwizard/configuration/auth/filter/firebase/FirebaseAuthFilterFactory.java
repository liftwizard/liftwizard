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

package io.liftwizard.dropwizard.configuration.auth.filter.firebase;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auto.service.AutoService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter.Builder;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import io.liftwizard.firebase.principal.FirebasePrincipal;

@JsonTypeName("firebase")
@AutoService(AuthFilterFactory.class)
public class FirebaseAuthFilterFactory
        implements AuthFilterFactory
{
    private @Valid @NotNull String databaseUrl;
    private @Valid @NotNull String firebaseConfig;

    private FirebaseAuth firebaseAuthFactory;

    @Nonnull
    @Override
    public AuthFilter<?, FirebasePrincipal> createAuthFilter()
    {
        com.google.firebase.auth.FirebaseAuth firebaseAuth = this.createFirebaseAuth();

        Authenticator<String, FirebasePrincipal> authenticator = new FirebaseOAuthAuthenticator(firebaseAuth);

        return new Builder<FirebasePrincipal>()
                .setAuthenticator(authenticator)
                .setPrefix("Bearer")
                .buildAuthFilter();
    }

    public GoogleCredentials createFirebaseCredentials()
    {
        this.initFirebaseAuthFactory();
        return this.firebaseAuthFactory.getCredentials();
    }

    public FirebaseApp createFirebaseApp()
    {
        this.initFirebaseAuthFactory();
        return this.firebaseAuthFactory.getFirebaseApp();
    }

    public com.google.firebase.auth.FirebaseAuth createFirebaseAuth()
    {
        this.initFirebaseAuthFactory();
        return this.firebaseAuthFactory.getFirebaseAuth();
    }

    public FirebaseDatabase createFirebaseDatabase()
    {
        this.initFirebaseAuthFactory();
        return this.firebaseAuthFactory.getFirebaseDatabase();
    }

    private void initFirebaseAuthFactory()
    {
        if (this.firebaseAuthFactory == null)
        {
            this.firebaseAuthFactory = new FirebaseAuth(
                    this.databaseUrl,
                    this.firebaseConfig);
        }
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
