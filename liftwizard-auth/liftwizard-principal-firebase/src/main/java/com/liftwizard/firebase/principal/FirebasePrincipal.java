package com.liftwizard.firebase.principal;

import java.security.Principal;

public class FirebasePrincipal implements Principal
{
    private final String  name;
    private final String  displayName;
    private final String  email;
    private final Boolean emailVerified;
    private final String  issuer;
    private final String  picture;
    private final String  signInProvider;

    public FirebasePrincipal(
            String name,
            String displayName,
            String email,
            Boolean emailVerified,
            String issuer,
            String picture,
            String signInProvider)
    {
        this.name           = name;
        this.displayName    = displayName;
        this.email          = email;
        this.emailVerified  = emailVerified;
        this.issuer         = issuer;
        this.picture        = picture;
        this.signInProvider = signInProvider;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public String getEmail()
    {
        return this.email;
    }

    public Boolean getEmailVerified()
    {
        return this.emailVerified;
    }

    public String getIssuer()
    {
        return this.issuer;
    }

    public String getPicture()
    {
        return this.picture;
    }

    public String getSignInProvider()
    {
        return this.signInProvider;
    }

    @Override
    public String toString()
    {
        return "{name=" + this.name + ", displayName=" + this.displayName + "}";
    }
}
