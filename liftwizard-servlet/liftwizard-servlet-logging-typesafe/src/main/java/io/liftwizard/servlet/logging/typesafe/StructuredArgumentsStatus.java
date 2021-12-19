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

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredArgumentsStatus
{
    private Status  name;
    private Integer code;
    private Family family;
    private String phrase;

    @JsonProperty
    public Status getName()
    {
        return this.name;
    }

    public void setName(Status name)
    {
        if (this.name != null)
        {
            throw new AssertionError(name);
        }
        // Name can occasionally be null, for http codes like 422 which are used by Dropwizard but don't appear in the Status enumeration
        this.name = name;
    }

    @JsonProperty
    public Integer getCode()
    {
        return this.code;
    }

    public void setCode(int code)
    {
        if (this.code != null)
        {
            throw new AssertionError(this.code);
        }
        this.code = code;
    }

    @JsonProperty
    public Family getFamily()
    {
        return this.family;
    }

    public void setFamily(Family family)
    {
        if (this.family != null)
        {
            throw new AssertionError(this.family);
        }
        this.family = Objects.requireNonNull(family);
    }

    @JsonProperty
    public String getPhrase()
    {
        return this.phrase;
    }

    public void setPhrase(String phrase)
    {
        if (this.phrase != null)
        {
            throw new AssertionError(this.phrase);
        }
        this.phrase = Objects.requireNonNull(phrase);
    }
}
