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

package io.liftwizard.servlet.logging.structured.status.info;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.StatusType;

/**
 * Gets information from the jax-rs {@link StatusType} and adds it to the structured argument map.
 *
 * @see <a href="https://liftwizard.io/docs/logging/logging-modules#logging-modules-statusinfostructuredloggingfilter">https://liftwizard.io/docs/logging/logging-modules#logging-modules-statusinfostructuredloggingfilter</a>
 */
public class StatusInfoStructuredLoggingFilter implements ContainerResponseFilter
{
    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext)
    {
        Object structuredArguments = requestContext.getProperty("structuredArguments");
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        StatusType statusInfo = responseContext.getStatusInfo();
        if (statusInfo.getStatusCode() != responseContext.getStatus())
        {
            throw new AssertionError();
        }

        structuredArgumentsMap.put("liftwizard.response.http.statusEnum", statusInfo.toEnum());
        structuredArgumentsMap.put("liftwizard.response.http.statusCode", statusInfo.getStatusCode());
        structuredArgumentsMap.put("liftwizard.response.http.statusFamily", statusInfo.getFamily());
        structuredArgumentsMap.put("liftwizard.response.http.statusPhrase", statusInfo.getReasonPhrase());

        this.getTypeName(responseContext)
                .ifPresent(typeName -> structuredArgumentsMap.put("liftwizard.response.http.entityType", typeName));
    }

    private Optional<String> getTypeName(ContainerResponseContext responseContext)
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
