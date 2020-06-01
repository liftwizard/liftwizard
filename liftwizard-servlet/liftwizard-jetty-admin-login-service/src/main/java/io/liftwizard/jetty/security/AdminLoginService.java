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

package io.liftwizard.jetty.security;

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
