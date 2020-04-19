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
