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

package io.liftwizard.dropwizard.configuration.http.logging;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.DataSizeUnit;
import io.dropwizard.validation.MinDataSize;
import io.dropwizard.validation.ValidationMethod;

public class JerseyHttpLoggingFactory
{
    // Should usually be disabled in production
    private boolean enabled                = true;
    private boolean logRequests            = true;
    private boolean logRequestBodies       = true;
    private boolean logResponses           = true;
    private boolean logResponseBodies      = true;
    private boolean logRequestHeaderNames  = true;
    private boolean logExcludedRequestHeaderNames;
    private boolean logResponseHeaderNames = true;
    private boolean logExcludedResponseHeaderNames;

    @NotNull
    private List<String> includedRequestHeaders = List.of(
            "Host",
            "User-Agent",
            "Content-Type");

    @NotNull
    private List<String> includedResponseHeaders = List.of(
            "Host",
            "User-Agent",
            "Content-Type");

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.BYTES)
    private DataSize maxEntitySize = DataSize.kilobytes(8);

    @JsonProperty
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @JsonProperty
    public boolean isLogRequests()
    {
        return this.logRequests;
    }

    @JsonProperty
    public void setLogRequests(boolean logRequests)
    {
        this.logRequests = logRequests;
    }

    @JsonProperty
    public boolean isLogRequestBodies()
    {
        return this.logRequestBodies;
    }

    @JsonProperty
    public void setLogRequestBodies(boolean logRequestBodies)
    {
        this.logRequestBodies = logRequestBodies;
    }

    @JsonProperty
    public boolean isLogResponses()
    {
        return this.logResponses;
    }

    @JsonProperty
    public void setLogResponses(boolean logResponses)
    {
        this.logResponses = logResponses;
    }

    @JsonProperty
    public boolean isLogResponseBodies()
    {
        return this.logResponseBodies;
    }

    @JsonProperty
    public void setLogResponseBodies(boolean logResponseBodies)
    {
        this.logResponseBodies = logResponseBodies;
    }

    @JsonProperty
    public boolean isLogRequestHeaderNames()
    {
        return this.logRequestHeaderNames;
    }

    @JsonProperty
    public void setLogRequestHeaderNames(boolean logRequestHeaderNames)
    {
        this.logRequestHeaderNames = logRequestHeaderNames;
    }

    @JsonProperty
    public boolean isLogExcludedRequestHeaderNames()
    {
        return this.logExcludedRequestHeaderNames;
    }

    @JsonProperty
    public void setLogExcludedRequestHeaderNames(boolean logExcludedRequestHeaderNames)
    {
        this.logExcludedRequestHeaderNames = logExcludedRequestHeaderNames;
    }

    @JsonProperty
    public boolean isLogResponseHeaderNames()
    {
        return this.logResponseHeaderNames;
    }

    @JsonProperty
    public void setLogResponseHeaderNames(boolean logResponseHeaderNames)
    {
        this.logResponseHeaderNames = logResponseHeaderNames;
    }

    @JsonProperty
    public boolean isLogExcludedResponseHeaderNames()
    {
        return this.logExcludedResponseHeaderNames;
    }

    @JsonProperty
    public void setLogExcludedResponseHeaderNames(boolean logExcludedResponseHeaderNames)
    {
        this.logExcludedResponseHeaderNames = logExcludedResponseHeaderNames;
    }

    @JsonProperty
    public List<String> getIncludedRequestHeaders()
    {
        return Collections.unmodifiableList(this.includedRequestHeaders);
    }

    @JsonProperty
    public void setIncludedRequestHeaders(List<String> includedRequestHeaders)
    {
        this.includedRequestHeaders = Collections.unmodifiableList(includedRequestHeaders);
    }

    @JsonProperty
    public List<String> getIncludedResponseHeaders()
    {
        return Collections.unmodifiableList(this.includedResponseHeaders);
    }

    @JsonProperty
    public void setIncludedResponseHeaders(List<String> includedResponseHeaders)
    {
        this.includedResponseHeaders = Collections.unmodifiableList(includedResponseHeaders);
    }

    @JsonProperty
    public DataSize getMaxEntitySize()
    {
        return this.maxEntitySize;
    }

    @JsonProperty
    public void setMaxEntitySize(DataSize maxEntitySize)
    {
        this.maxEntitySize = maxEntitySize;
    }

    @ValidationMethod(message = "Logging request bodies requires logging requests")
    @JsonIgnore
    public boolean isValidRequestBodies()
    {
        return !this.logRequestBodies || this.logRequests;
    }

    @ValidationMethod(message = "Logging request header names requires logging requests")
    @JsonIgnore
    public boolean isValidRequestHeaderNames()
    {
        return !this.logRequestHeaderNames || this.logRequests;
    }

    @ValidationMethod(message = "Logging request headers requires logging requests")
    @JsonIgnore
    public boolean isValidRequestHeaders()
    {
        return this.includedRequestHeaders.isEmpty() || this.logRequests;
    }

    @ValidationMethod(message = "Logging response bodies requires logging responses")
    @JsonIgnore
    public boolean isValidResponseBodies()
    {
        return !this.logResponseBodies || this.logResponses;
    }

    @ValidationMethod(message = "Logging response header names requires logging responses")
    @JsonIgnore
    public boolean isValidResponseHeaderNames()
    {
        return !this.logResponseHeaderNames || this.logResponses;
    }

    @ValidationMethod(message = "Logging response headers requires logging responses")
    @JsonIgnore
    public boolean isValidResponseHeaders()
    {
        return this.includedResponseHeaders.isEmpty() || this.logResponses;
    }
}
