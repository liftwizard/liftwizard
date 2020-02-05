package com.liftwizard.dropwizard.configuration.auth.filter.impersonation;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.dropwizard.auth.Authenticator;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.MDC;

public class ImpersonationAuthenticator implements Authenticator<String, ImpersonatedPrincipal>
{
    private static final String IMPERSONATION_PRINCIPAL_NAME = "liftwizard.auth.impersonation.principalName";

    private static final ImmutableList<String> MDC_KEYS = Lists.immutable.with(IMPERSONATION_PRINCIPAL_NAME);

    public static ImmutableList<String> getMDCKeys()
    {
        return MDC_KEYS;
    }

    @Nonnull
    @Override
    public Optional<ImpersonatedPrincipal> authenticate(String principalName)
    {
        MDC.put(IMPERSONATION_PRINCIPAL_NAME, principalName);
        ImpersonatedPrincipal impersonatedUser = new ImpersonatedPrincipal(principalName);
        return Optional.of(impersonatedUser);
    }
}
