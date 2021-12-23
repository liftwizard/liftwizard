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

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import io.liftwizard.servlet.logging.feature.LoggingConfig;
import io.liftwizard.servlet.logging.feature.LoggingFeature;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsHttp;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequest;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsRequestHttp;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;

public abstract class AbstractLoggingFilter
{
    public static final String ENTITY_LOGGER_PROPERTY = LoggingFeature.class.getName() + ".entityLogger";

    private static final Comparator<Entry<String, List<String>>> COMPARATOR = Entry.comparingByKey(String::compareToIgnoreCase);

    protected final LoggingConfig loggingConfig;

    @Context
    private ResourceInfo resourceInfo;

    public AbstractLoggingFilter(@Nonnull LoggingConfig loggingConfig)
    {
        this.loggingConfig = Objects.requireNonNull(loggingConfig);
    }

    protected void addHeaders(
            @Nonnull StructuredArgumentsHttp structuredArgumentsHttp,
            @Nonnull MultivaluedMap<String, String> headers)
    {
        ImmutableList<String> includedHeaders        = this.loggingConfig.getIncludedHeaders();
        boolean               logExcludedHeaderNames = this.loggingConfig.isLogExcludedHeaderNames();
        MutableList<String>   excludedHeaders        = Lists.mutable.empty();

        if (logExcludedHeaderNames)
        {
            structuredArgumentsHttp.initializeExcludedHeaders();
        }

        for (Entry<String, List<String>> headerEntry : this.getSortedHeaders(headers.entrySet()))
        {
            List<?> val    = headerEntry.getValue();
            String  header = headerEntry.getKey();

            if (includedHeaders.contains(header))
            {
                String value = ListAdapter.adapt(val).makeString();
                structuredArgumentsHttp.addHeader(header, value);
            }
            else if (logExcludedHeaderNames)
            {
                excludedHeaders.add(header);
                structuredArgumentsHttp.addExcludedHeader(header);
            }
        }
    }

    private Set<Entry<String, List<String>>> getSortedHeaders(@Nonnull Set<Entry<String, List<String>>> headers)
    {
        Set<Entry<String, List<String>>> sortedHeaders = new TreeSet<>(COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    @Nonnull
    protected StructuredArguments initRequestStructuredArguments(
            @Nonnull ContainerRequestContext requestContext,
            String event)
    {
        StructuredArguments structuredArguments = new StructuredArguments();
        structuredArguments.setEvent(event);
        requestContext.setProperty("structuredArguments", structuredArguments);

        UriInfo uriInfo = requestContext.getUriInfo();

        StructuredArgumentsRequestHttp http = structuredArguments.getRequest().getHttp();

        this.addPathInfo(requestContext, uriInfo, http);
        this.addResourceInfo(structuredArguments.getRequest());
        this.addQueryParameters(uriInfo, http);
        this.addPathParameters(uriInfo, http);
        this.addPathTemplate(requestContext, uriInfo, http);
        this.addHeaders(http, requestContext.getHeaders());
        return structuredArguments;
    }

    private void addPathInfo(
            @Nonnull ContainerRequestContext requestContext,
            UriInfo uriInfo,
            StructuredArgumentsRequestHttp http)
    {
        http.getPath().setAbsolute(uriInfo.getAbsolutePath());
        http.setMethod(requestContext.getMethod());
    }

    private void addResourceInfo(StructuredArgumentsRequest request)
    {
        Objects.requireNonNull(this.resourceInfo);

        // Could be null during error responses like 404 and 405

        @Nullable
        Class<?> resourceClass = this.resourceInfo.getResourceClass();
        @Nullable
        Method resourceMethod = this.resourceInfo.getResourceMethod();

        if (resourceClass != null)
        {
            request.setResourceClass(resourceClass);
        }
        if (resourceMethod != null)
        {
            request.setResourceMethod(resourceMethod);
        }
    }

    private void addQueryParameters(UriInfo uriInfo, StructuredArgumentsRequestHttp http)
    {
        uriInfo.getQueryParameters().forEach((parameterName, parameterValues) ->
        {
            String value = ListAdapter.adapt(parameterValues).makeString();
            http.addQueryParameter(parameterName, value);
        });
    }

    private void addPathParameters(UriInfo uriInfo, StructuredArgumentsRequestHttp http)
    {
        uriInfo.getPathParameters().forEach((parameterName, parameterValues) ->
        {
            String value = ListAdapter.adapt(parameterValues).makeString();
            http.addPathParameter(parameterName, value);
        });
    }

    private void addPathTemplate(
            @Nonnull ContainerRequestContext requestContext,
            @Nonnull UriInfo uriInfo,
            @Nonnull StructuredArgumentsRequestHttp http)
    {
        if (!(requestContext instanceof ContainerRequest))
        {
            return;
        }

        ContainerRequest containerRequest = (ContainerRequest) requestContext;
        String           pathPrefix = uriInfo.getBaseUri().getPath();
        ExtendedUriInfo  extendedUriInfo = containerRequest.getUriInfo();
        Resource         matchedModelResource = extendedUriInfo.getMatchedModelResource();
        if (matchedModelResource == null)
        {
            return;
        }

        String path              = matchedModelResource.getPath();
        if (!pathPrefix.endsWith("/"))
        {
            throw new IllegalStateException(pathPrefix);
        }
        String pathWithoutPrefix = path.startsWith("/") ? path.substring(1) : path;
        String pathTemplate = pathPrefix + pathWithoutPrefix;
        http.getPath().setTemplate(pathTemplate);
    }
}
