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

package io.liftwizard.dropwizard.configuration.auth.filter.header;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.dropwizard.auth.Authenticator;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.MDC;

public class HeaderAuthenticator
        implements Authenticator<String, HeaderPrincipal>
{
    private static final String HEADER_PRINCIPAL_NAME = "liftwizard.auth.header.principalName";

    private static final ImmutableList<String> MDC_KEYS = Lists.immutable.with(HEADER_PRINCIPAL_NAME);

    private final String headerPrefix;

    public HeaderAuthenticator(String headerPrefix)
    {
        String prefix = headerPrefix;
        if (prefix != null && !prefix.endsWith(" "))
        {
            prefix += " ";
        }
        this.headerPrefix = prefix;
    }

    public static ImmutableList<String> getMDCKeys()
    {
        return MDC_KEYS;
    }

    @Nonnull
    @Override
    public Optional<HeaderPrincipal> authenticate(String headerValue)
    {
        if (headerValue == null)
        {
            return Optional.empty();
        }

        if (this.headerPrefix == null)
        {
            MDC.put(HEADER_PRINCIPAL_NAME, headerValue);
            HeaderPrincipal principal = new HeaderPrincipal(headerValue);
            return Optional.of(principal);
        }

        // Check that header value starts with the expected prefix.
        if (!headerValue.startsWith(this.headerPrefix))
        {
            return Optional.empty();
        }

        // Split off the prefix and use the remainder as the principal name.
        String principalName = headerValue.substring(this.headerPrefix.length());
        if (principalName.isEmpty())
        {
            return Optional.empty();
        }

        MDC.put(HEADER_PRINCIPAL_NAME, principalName);
        HeaderPrincipal principal = new HeaderPrincipal(principalName);
        return Optional.of(principal);
    }
}
