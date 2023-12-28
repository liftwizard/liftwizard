/*
 * Copyright 2023 Kathleen Kusworo
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

package io.liftwizard.servlet.filter.spa;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.collections.api.list.ImmutableList;

public class SPARedirectFilter
        implements Filter
{
    private final String                redirectSPAPage;
    private final String                cacheControlHeader;
    private final ImmutableList<String> wellKnownPathPrefixes;

    public SPARedirectFilter(
            String redirectSPAPage,
            String cacheControlHeader,
            ImmutableList<String> wellKnownPathPrefixes)
    {
        this.redirectSPAPage       = redirectSPAPage;
        this.cacheControlHeader    = cacheControlHeader;
        this.wellKnownPathPrefixes = wellKnownPathPrefixes;
    }

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        // Let all handlers and filters process before we do anything. That way we can capture if this is a 404.
        chain.doFilter(request, response);
        if (request instanceof HttpServletRequest httpServletRequest && response instanceof HttpServletResponse httpServletResponse)
        {
            String requestedPath = httpServletRequest.getRequestURI();

            // If the requested path is not known (i.e. not reqs for auth, backend, or static files)
            // then redirect to index.html for single-page-app (SPA) routing, and set the response to 200.
            if (!this.isKnownPath(requestedPath) && httpServletResponse.getStatus() == 404)
            {
                if (requestedPath.equals(this.redirectSPAPage))
                {
                    throw new ServletException("SPARedirectFilter redirectSPAPage cannot be the same as the path being redirected to. Both are: " + requestedPath);
                }
                httpServletResponse.setStatus(200);
                httpServletResponse.setHeader("Cache-Control", this.cacheControlHeader);
                RequestDispatcher requestDispatcher = request.getRequestDispatcher(this.redirectSPAPage);
                requestDispatcher.forward(httpServletRequest, httpServletResponse);
            }
        }
    }

    private boolean isKnownPath(String requestedPath)
    {
        return this.wellKnownPathPrefixes.anySatisfy(requestedPath::startsWith);
    }
}
