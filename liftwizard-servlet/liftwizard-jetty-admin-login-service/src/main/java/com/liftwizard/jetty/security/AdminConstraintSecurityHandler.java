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

package com.liftwizard.jetty.security;

import javax.annotation.Nonnull;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;

public class AdminConstraintSecurityHandler extends ConstraintSecurityHandler
{
    public static final String ADMIN_ROLE = "admin";

    public AdminConstraintSecurityHandler(String userName, String password)
    {
        this.setAuthenticator(new BasicAuthenticator());

        Constraint        constraint        = this.getConstraint();
        ConstraintMapping constraintMapping = this.getConstraintMapping(constraint);
        this.addConstraintMapping(constraintMapping);

        AdminLoginService adminLoginService = this.getAdminLoginService(userName, password);
        this.setLoginService(adminLoginService);
    }

    @Nonnull
    private Constraint getConstraint()
    {
        Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ADMIN_ROLE);
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{ADMIN_ROLE});
        return constraint;
    }

    @Nonnull
    private ConstraintMapping getConstraintMapping(Constraint constraint)
    {
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");
        return constraintMapping;
    }

    @Nonnull
    private AdminLoginService getAdminLoginService(String userName, String password)
    {
        return new AdminLoginService(userName, password);
    }
}
