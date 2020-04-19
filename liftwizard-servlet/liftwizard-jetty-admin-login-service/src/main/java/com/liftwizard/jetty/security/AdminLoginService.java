package com.liftwizard.jetty.security;

import java.util.Objects;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.util.security.Password;

public class AdminLoginService extends AbstractLoginService
{
    private final UserPrincipal adminPrincipal;
    private final String        adminUserName;

    public AdminLoginService(String userName, String password)
    {
        this.adminUserName = Objects.requireNonNull(userName);
        Password credential = new Password(Objects.requireNonNull(password));
        this.adminPrincipal = new UserPrincipal(userName, credential);
    }

    @Override
    protected String[] loadRoleInfo(UserPrincipal principal)
    {
        if (this.adminUserName.equals(principal.getName()))
        {
            return new String[]{"admin"};
        }
        return new String[0];
    }

    @Override
    protected UserPrincipal loadUserInfo(String userName)
    {
        return this.adminUserName.equals(userName) ? this.adminPrincipal : null;
    }
}
