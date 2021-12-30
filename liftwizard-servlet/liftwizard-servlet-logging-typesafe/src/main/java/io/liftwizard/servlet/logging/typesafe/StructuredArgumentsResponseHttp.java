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

public class StructuredArgumentsResponseHttp
        extends StructuredArgumentsHttp
{
    private final StructuredArgumentsStatus status = new StructuredArgumentsStatus();

    private String entityType;
    private String contentType;
    private String body;

    @JsonProperty
    public StructuredArgumentsStatus getStatus()
    {
        return this.status;
    }

    @JsonProperty
    public String getEntityType()
    {
        return this.entityType;
    }

    public void setEntityType(String entityType)
    {
        if (this.entityType != null)
        {
            throw new AssertionError(this.entityType);
        }
        this.entityType = Objects.requireNonNull(entityType);
    }

    @JsonProperty
    public String getContentType()
    {
        return this.contentType;
    }

    public void setContentType(String contentType)
    {
        if (this.contentType != null)
        {
            throw new AssertionError(this.contentType);
        }
        this.contentType = Objects.requireNonNull(contentType);
    }

    @JsonProperty
    public String getBody()
    {
        return this.body;
    }

    public void setBody(String body)
    {
        if (this.body != null)
        {
            throw new AssertionError(this.body);
        }
        this.body = Objects.requireNonNull(body);
    }
}
