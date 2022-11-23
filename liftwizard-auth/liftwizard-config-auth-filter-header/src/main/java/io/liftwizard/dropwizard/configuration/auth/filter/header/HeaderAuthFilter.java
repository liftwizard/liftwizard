/*
 * Copyright 2022 Craig Motlin
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

import java.util.List;
import java.util.Objects;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;

import io.dropwizard.auth.AuthFilter;

@Priority(Priorities.AUTHENTICATION)
public class HeaderAuthFilter
        extends AuthFilter<String, HeaderPrincipal>
{
    private final String headerName;
    private final String headerPrefix;

    public HeaderAuthFilter(String headerName, String headerPrefix)
    {
        this.headerName   = Objects.requireNonNull(headerName);
        this.headerPrefix = headerPrefix;
    }

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        List<String> headerValues = requestContext.getHeaders().get(this.headerName);
        if (headerValues == null || headerValues.size() != 1)
        {
            throw new WebApplicationException(this.unauthorizedHandler.buildResponse(this.headerName, "unused realm"));
        }

        String credentials = headerValues.get(0);
        if (!this.authenticate(requestContext, credentials, "Header"))
        {
            throw new WebApplicationException(this.unauthorizedHandler.buildResponse(this.headerName, this.headerPrefix));
        }
    }

    public static class Builder
            extends AuthFilterBuilder<String, HeaderPrincipal, HeaderAuthFilter>
    {
        private final String headerName;
        private final String headerPrefix;

        public Builder(String headerName, String headerPrefix)
        {
            this.headerName   = Objects.requireNonNull(headerName);
            this.headerPrefix = headerPrefix;
            this.setAuthenticator(new HeaderAuthenticator(headerPrefix));
            this.setUnauthorizedHandler(new JSONUnauthorizedHandler());
        }

        @Override
        protected HeaderAuthFilter newInstance()
        {
            return new HeaderAuthFilter(this.headerName, this.headerPrefix);
        }
    }
}
