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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredArgumentsRequestHttp
        extends StructuredArgumentsHttp
{
    @JsonProperty
    private final StructuredArgumentsPath path = new StructuredArgumentsPath();

    private String method;

    private StructuredArgumentsParameters parameters;

    @JsonProperty
    public StructuredArgumentsPath getPath()
    {
        return this.path;
    }

    @JsonProperty
    public String getMethod()
    {
        return this.method;
    }

    public void setMethod(String method)
    {
        if (this.method != null)
        {
            throw new AssertionError(this.method);
        }
        this.method = Objects.requireNonNull(method);
    }

    @JsonProperty
    public StructuredArgumentsParameters getParameters()
    {
        return this.parameters;
    }

    public void addQueryParameter(String key, String value)
    {
        if (this.parameters == null)
        {
            this.parameters = new StructuredArgumentsParameters();
        }

        this.parameters.addQueryParameter(key, value);
    }

    public void addPathParameter(String key, String value)
    {
        if (this.parameters == null)
        {
            this.parameters = new StructuredArgumentsParameters();
        }

        this.parameters.addPathParameter(key, value);
    }
}
