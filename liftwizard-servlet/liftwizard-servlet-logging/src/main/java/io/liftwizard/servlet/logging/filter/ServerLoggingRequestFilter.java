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

package io.liftwizard.servlet.logging.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsParameters;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequest;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequestHttp;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.collections.impl.map.mutable.MapAdapter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;

@ConstrainedTo(RuntimeType.SERVER)
public final class ServerLoggingRequestFilter
        implements ContainerRequestFilter
{
    @Context
    private ResourceInfo resourceInfo;

    private final Function<Principal, Map<String, Object>> principalBuilder;

    public ServerLoggingRequestFilter(Function<Principal, Map<String, Object>> principalBuilder)
    {
        this.principalBuilder = Objects.requireNonNull(principalBuilder);
    }

    @Override
    public void filter(@Nonnull ContainerRequestContext requestContext)
            throws IOException
    {
        StructuredArguments structuredArguments =
                (StructuredArguments) requestContext.getProperty("structuredArguments");
        UriInfo uriInfo = requestContext.getUriInfo();

        StructuredArgumentsRequestHttp http = structuredArguments.getRequest().getHttp();

        this.addResourceInfo(structuredArguments.getRequest());
        this.addParameters(uriInfo, http);
        this.addPath(requestContext, uriInfo, http);
        this.addSecurityContext(requestContext, http);
    }

    private void addResourceInfo(@Nonnull StructuredArgumentsRequest request)
    {
        Objects.requireNonNull(this.resourceInfo);

        Class<?> resourceClass = this.resourceInfo.getResourceClass();
        Method resourceMethod = this.resourceInfo.getResourceMethod();

        // Could be null during error responses like 404 and 405
        if (resourceClass != null)
        {
            request.setResourceClass(resourceClass);
        }
        if (resourceMethod != null)
        {
            request.setResourceMethod(resourceMethod);
        }
    }

    private void addParameters(@Nonnull UriInfo uriInfo, @Nonnull StructuredArgumentsRequestHttp http)
    {
        StructuredArgumentsParameters parameters = this.buildParameters(uriInfo);
        http.setParameters(parameters);
    }

    @Nonnull
    private StructuredArgumentsParameters buildParameters(@Nonnull UriInfo uriInfo)
    {
        MapIterable<String, String> queryParameters = this.buildParameters(uriInfo.getQueryParameters());
        MapIterable<String, String> pathParameters = this.buildParameters(uriInfo.getPathParameters());
        return new StructuredArgumentsParameters(
                queryParameters,
                pathParameters);
    }

    private MutableMap<String, String> buildParameters(@Nonnull MultivaluedMap<String, String> inputParameters)
    {
        MutableMap<String, String> outputParameters = MapAdapter.adapt(new LinkedHashMap<>());

        inputParameters.forEach((parameterName, parameterValues) ->
        {
            String value = ListAdapter.adapt(parameterValues).makeString();
            String duplicate = outputParameters.put(parameterName, value);
            if (duplicate != null)
            {
                throw new IllegalStateException(duplicate);
            }
        });

        return outputParameters.asUnmodifiable();
    }

    private void addPath(
            @Nonnull ContainerRequestContext requestContext,
            @Nonnull UriInfo uriInfo,
            @Nonnull StructuredArgumentsRequestHttp http)
    {
        String baseUriPath = uriInfo.getBaseUri().getPath();
        String pathTemplate = this.getPathTemplate(requestContext);
        http.getPath().setBaseUriPath(baseUriPath);
        http.getPath().setTemplate(pathTemplate);
        URI absolutePath = uriInfo.getAbsolutePath();
        if (!Objects.equals(http.getPath().getAbsolute(), absolutePath.toString()))
        {
            throw new AssertionError();
        }
    }

    @Nullable
    private String getPathTemplate(@Nonnull ContainerRequestContext requestContext)
    {
        if (!(requestContext instanceof ContainerRequest containerRequest))
        {
            return null;
        }

        ExtendedUriInfo extendedUriInfo = containerRequest.getUriInfo();
        List<UriTemplate> matchedTemplates = extendedUriInfo.getMatchedTemplates();
        if (matchedTemplates.isEmpty())
        {
            return null;
        }

        return ListAdapter.adapt(matchedTemplates)
                .asReversed()
                .collect(UriTemplate::getTemplate)
                .makeString("");
    }

    private void addSecurityContext(
            @Nonnull ContainerRequestContext requestContext,
            StructuredArgumentsRequestHttp http)
    {
        SecurityContext securityContext = requestContext.getSecurityContext();
        String authenticationScheme = securityContext.getAuthenticationScheme();
        Principal userPrincipal = securityContext.getUserPrincipal();

        http.setAuthenticationScheme(authenticationScheme);
        if (userPrincipal != null)
        {
            http.setPrincipal(this.principalBuilder.apply(userPrincipal));
        }
    }
}
