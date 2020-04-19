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

package com.liftwizard.servlet.logging.correlation.id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;

/**
 * Adapted from Beadledom's CorrelationIdFilter.
 * https://raw.githubusercontent.com/cerner/beadledom/master/jaxrs/src/main/java/com/cerner/beadledom/jaxrs/provider/CorrelationIdFilter.java
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorrelationIdFilter implements ContainerRequestFilter, ContainerResponseFilter
{
    private static final String DEFAULT_HEADER_NAME = "liftwizard.request.correlationId";
    private final        String headerName;
    private final        String mdcName;

    private final Supplier<UUID> uuidSupplier;

    public CorrelationIdFilter(@Nonnull @Context Supplier<UUID> uuidSupplier)
    {
        this(uuidSupplier, Optional.empty(), Optional.empty());
    }

    public CorrelationIdFilter(
            @Nonnull Supplier<UUID> uuidSupplier,
            @Nonnull Optional<String> headerName,
            @Nonnull Optional<String> mdcName)
    {
        this.uuidSupplier = Objects.requireNonNull(uuidSupplier, "Could not find Supplier<UUID>. Make sure you've registered the bundle com.liftwizard.dropwizard.bundle.uuid.UUIDBundle with Dropwizard.");
        this.headerName   = headerName.orElse(DEFAULT_HEADER_NAME);
        this.mdcName      = mdcName.orElse(DEFAULT_HEADER_NAME);
    }

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        String correlationId = this.getCorrelationIdHeader(requestContext);

        MultiMDCCloseable mdc = (MultiMDCCloseable) requestContext.getProperty("mdc");
        mdc.put(this.mdcName, correlationId);

        requestContext.setProperty(this.mdcName, correlationId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    {
        String correlationId = this.getCorrelationIdProperty(requestContext);
        responseContext.getHeaders().add(this.headerName, correlationId);
    }

    @Nonnull
    private String getCorrelationIdHeader(ContainerRequestContext requestContext)
    {
        String correlationId = requestContext.getHeaderString(this.headerName);
        if (correlationId != null)
        {
            return correlationId;
        }

        UUID uuid = this.uuidSupplier.get();
        return uuid.toString();
    }

    @Nonnull
    private String getCorrelationIdProperty(ContainerRequestContext requestContext)
    {
        String correlationId = (String) requestContext.getProperty(this.mdcName);
        if (correlationId != null)
        {
            return correlationId;
        }

        UUID uuid = this.uuidSupplier.get();
        return uuid.toString();
    }
}
