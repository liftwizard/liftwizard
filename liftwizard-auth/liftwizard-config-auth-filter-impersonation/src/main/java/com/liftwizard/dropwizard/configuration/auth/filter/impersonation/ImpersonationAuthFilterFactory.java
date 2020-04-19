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
