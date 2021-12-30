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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
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
        http.getStatus().setName(statusInfo.toEnum());
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

        if (entityType instanceof Class)
        {
            Class<?> aClass = (Class<?>) entityType;
            return Optional.of(aClass.getCanonicalName());
        }

        if (entityType instanceof ParameterizedType)
        {
            ParameterizedType parameterizedType = (ParameterizedType) entityType;
            Type              rawType           = parameterizedType.getRawType();
            if (rawType != List.class)
            {
                throw new AssertionError(parameterizedType.getTypeName());
            }

            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length != 1)
            {
                throw new AssertionError(parameterizedType.getTypeName());
            }

            return Optional.of(actualTypeArguments[0].getTypeName());
        }

        throw new AssertionError(entityType.getTypeName());
    }
}
