/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.servlet.logging.typesafe;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredArgumentsRequest
{
    private final StructuredArgumentsRequestHttp http = new StructuredArgumentsRequestHttp();

    private Class<?> resourceClass;
    private String resourceMethod;

    public StructuredArgumentsRequestHttp getHttp()
    {
        return this.http;
    }

    public Class<?> getResourceClass()
    {
        return this.resourceClass;
    }

    public void setResourceClass(@Nonnull Class<?> resourceClass)
    {
        if (this.resourceClass != null)
        {
            throw new AssertionError(this.resourceClass);
        }
        this.resourceClass = Objects.requireNonNull(resourceClass);
    }

    @JsonProperty
    public String getResourceMethod()
    {
        return this.resourceMethod;
    }

    public void setResourceMethod(@Nonnull Method resourceMethod)
    {
        if (this.resourceMethod != null)
        {
            throw new AssertionError(this.resourceMethod);
        }
        this.resourceMethod = Objects.requireNonNull(resourceMethod.getName());
    }
}
