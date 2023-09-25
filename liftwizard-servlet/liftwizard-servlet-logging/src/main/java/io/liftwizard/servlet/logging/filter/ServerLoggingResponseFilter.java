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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.StatusType;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.liftwizard.servlet.logging.typesafe.StructuredArgumentsResponseHttp;

@ConstrainedTo(RuntimeType.SERVER)
public final class ServerLoggingResponseFilter
        implements ContainerResponseFilter
{
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(
            @Nonnull ContainerRequestContext requestContext,
            @Nonnull ContainerResponseContext responseContext)
            throws IOException
    {
        StructuredArguments structuredArguments = (StructuredArguments) requestContext.getProperty("structuredArguments");

        if (structuredArguments.getResponse() == null)
        {
            throw new IllegalStateException();
        }

        StructuredArgumentsResponseHttp http = structuredArguments.getResponse().getHttp();

        StatusType statusInfo = responseContext.getStatusInfo();
        http.getStatus().setStatus(statusInfo.toEnum());
        http.getStatus().setFamily(statusInfo.getFamily());
        http.getStatus().setPhrase(statusInfo.getReasonPhrase());

        this.getTypeName(responseContext).ifPresent(http::setEntityType);
    }

    private Optional<String> getTypeName(@Nonnull ContainerResponseContext responseContext)
    {
        Type entityType = responseContext.getEntityType();
        if (entityType == null)
        {
            return Optional.empty();
        }

        if (entityType instanceof Class<?> aClass)
        {
            return Optional.of(aClass.getCanonicalName());
        }

        if (entityType instanceof ParameterizedType parameterizedType)
        {
            return Optional.ofNullable(parameterizedType.getTypeName());
        }

        throw new AssertionError(entityType.getTypeName());
    }
}
