package com.liftwizard.dropwizard.configuration.auth.filter.impersonation;

import java.security.Principal;
import java.util.Objects;

public class ImpersonatedPrincipal implements Principal
{
    private final String name;

    public ImpersonatedPrincipal(String name)
    {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
