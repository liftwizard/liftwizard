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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuredArgumentsStatus
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredArgumentsStatus.class);

    private Status  status;
    private Integer code;
    private Family  family;
    private String  phrase;

    @JsonProperty
    public Status getStatus()
    {
        return this.status;
    }

    public void setStatus(Status status)
    {
        // It's possible to overwrite a non-null status with another non-null status due to the try-catch block in org.glassfish.jersey.server.ServerRuntime.process().
        // It happens when there is an error while processing an otherwise successful response.
        // For example, we try to return a successful response, but there is an error serializing the response body, because Jackson calls a getter which throws.

        // Name can occasionally be null, for http codes like 422 which are used by Dropwizard but don't appear in the Status enumeration
        this.status = status;
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
            LOGGER.warn("Overwriting code '{}' with '{}'.", this.code, code);
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
            LOGGER.warn("Overwriting family '{}' with '{}'.", this.family, family);
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
            LOGGER.warn("Overwriting phrase '{}' with '{}'.", this.phrase, phrase);
        }
        this.phrase = Objects.requireNonNull(phrase);
    }
}
