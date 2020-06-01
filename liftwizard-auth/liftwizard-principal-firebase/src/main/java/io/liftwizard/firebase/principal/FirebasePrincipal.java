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

package io.liftwizard.firebase.principal;

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
