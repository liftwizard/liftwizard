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
