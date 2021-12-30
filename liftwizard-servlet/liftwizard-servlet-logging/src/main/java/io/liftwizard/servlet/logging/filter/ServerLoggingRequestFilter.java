/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package io.liftwizard.servlet.logging.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.LinkedHashMap;
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
import javax.ws.rs.core.UriInfo;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsParameters;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequest;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequestHttp;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.collections.impl.map.mutable.MapAdapter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;

@ConstrainedTo(RuntimeType.SERVER)
public final class ServerLoggingRequestFilter
        implements ContainerRequestFilter
{
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(@Nonnull ContainerRequestContext requestContext)
            throws IOException
    {
        StructuredArguments structuredArguments = (StructuredArguments) requestContext.getProperty("structuredArguments");
        UriInfo             uriInfo             = requestContext.getUriInfo();

        StructuredArgumentsRequestHttp http = structuredArguments.getRequest().getHttp();

        this.addResourceInfo(structuredArguments.getRequest());
        this.addParameters(uriInfo, http);
        this.addPath(requestContext, uriInfo, http);
    }

    private void addResourceInfo(@Nonnull StructuredArgumentsRequest request)
    {
        Objects.requireNonNull(this.resourceInfo);

        Class<?> resourceClass  = this.resourceInfo.getResourceClass();
        Method   resourceMethod = this.resourceInfo.getResourceMethod();

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
        MapIterable<String, String> pathParameters  = this.buildParameters(uriInfo.getPathParameters());
        return new StructuredArgumentsParameters(
                queryParameters,
                pathParameters);
    }

    private MutableMap<String, String> buildParameters(@Nonnull MultivaluedMap<String, String> inputParameters)
    {
        MutableMap<String, String> outputParameters = MapAdapter.adapt(new LinkedHashMap<>());

        inputParameters.forEach((parameterName, parameterValues) ->
        {
            String value     = ListAdapter.adapt(parameterValues).makeString();
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
        String pathTemplate = this.getPathTemplate(requestContext, uriInfo);
        http.getPath().setTemplate(pathTemplate);
        URI absolutePath = uriInfo.getAbsolutePath();
        if (!Objects.equals(http.getPath().getAbsolute(), absolutePath.toString()))
        {
            throw new AssertionError();
        }
    }

    @Nullable
    private String getPathTemplate(@Nonnull ContainerRequestContext requestContext, @Nonnull UriInfo uriInfo)
    {
        if (!(requestContext instanceof ContainerRequest))
        {
            return null;
        }

        ContainerRequest containerRequest     = (ContainerRequest) requestContext;
        String           pathPrefix           = uriInfo.getBaseUri().getPath();
        ExtendedUriInfo  extendedUriInfo      = containerRequest.getUriInfo();
        Resource         matchedModelResource = extendedUriInfo.getMatchedModelResource();
        if (matchedModelResource == null)
        {
            return null;
        }

        String path = matchedModelResource.getPath();
        if (!pathPrefix.endsWith("/"))
        {
            throw new IllegalStateException(pathPrefix);
        }
        String pathWithoutPrefix = path.startsWith("/") ? path.substring(1) : path;
        return pathPrefix + pathWithoutPrefix;
    }
}
