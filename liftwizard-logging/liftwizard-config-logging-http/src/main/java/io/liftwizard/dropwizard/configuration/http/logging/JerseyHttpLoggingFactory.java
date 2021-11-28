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

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;
import io.dropwizard.validation.MinSize;
import io.dropwizard.validation.ValidationMethod;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class JerseyHttpLoggingFactory
{
    // Should usually be disabled in production
    private boolean enabled                = true;
    private boolean logRequests            = true;
    private boolean logRequestBodies       = true;
    private boolean logResponses           = true;
    private boolean logResponseBodies      = true;
    private boolean logExcludedHeaderNames = true;

    private @NotNull ImmutableList<String> includedHeaders = Lists.immutable.with(
            "Host",
            "User-Agent",
            "Content-Type");

    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private @NotNull Size maxEntitySize = Size.kilobytes(8);

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
    public boolean isLogExcludedHeaderNames()
    {
        return this.logExcludedHeaderNames;
    }

    @JsonProperty
    public void setLogExcludedHeaderNames(boolean logExcludedHeaderNames)
    {
        this.logExcludedHeaderNames = logExcludedHeaderNames;
    }

    @JsonProperty
    public ImmutableList<String> getIncludedHeaders()
    {
        return this.includedHeaders;
    }

    @JsonProperty
    public void setIncludedHeaders(ImmutableList<String> includedHeaders)
    {
        this.includedHeaders = includedHeaders;
    }

    @JsonProperty
    public Size getMaxEntitySize()
    {
        return this.maxEntitySize;
    }

    @JsonProperty
    public void setMaxEntitySize(Size maxEntitySize)
    {
        this.maxEntitySize = maxEntitySize;
    }

    @ValidationMethod(message = "Logging request bodies requires logging requests")
    @JsonIgnore
    public boolean isValidRequest()
    {
        return !this.logRequestBodies || this.logRequests;
    }

    @ValidationMethod(message = "Logging response bodies requires logging responses")
    @JsonIgnore
    public boolean isValidResponse()
    {
        return !this.logResponseBodies || this.logResponses;
    }
}
