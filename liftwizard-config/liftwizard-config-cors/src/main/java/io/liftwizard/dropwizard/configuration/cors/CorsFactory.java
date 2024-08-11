/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.cors;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CorsFactory
{
    // Default to disabled since default settings are dangerous
    private boolean enabled;
    private @Valid @NotNull String filterName = "CORS";
    // allowedOrigins = "*" is convenient during development but must be changed in production
    private @Valid @NotNull String allowedOrigins = "*";
    private @Valid @NotNull String allowedHeaders = "X-Requested-With,Content-Type,Accept,Origin,Authorization";
    private @Valid @NotNull String allowedMethods = "OPTIONS,GET,PUT,POST,PATCH,DELETE,HEAD";
    private @Valid @NotNull String allowCredentials = "true";
    private @Valid @NotNull List<String> urlPatterns = List.of("/*");

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getFilterName()
    {
        return this.filterName;
    }

    @JsonProperty
    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public String getAllowedOrigins()
    {
        return this.allowedOrigins;
    }

    @JsonProperty
    public void setAllowedOrigins(String allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedHeaders()
    {
        return this.allowedHeaders;
    }

    @JsonProperty
    public void setAllowedHeaders(String allowedHeaders)
    {
        this.allowedHeaders = allowedHeaders;
    }

    public String getAllowedMethods()
    {
        return this.allowedMethods;
    }

    @JsonProperty
    public void setAllowedMethods(String allowedMethods)
    {
        this.allowedMethods = allowedMethods;
    }

    public String getAllowCredentials()
    {
        return this.allowCredentials;
    }

    @JsonProperty
    public void setAllowCredentials(String allowCredentials)
    {
        this.allowCredentials = allowCredentials;
    }

    public List<String> getUrlPatterns()
    {
        return this.urlPatterns;
    }

    @JsonProperty
    public void setUrlPatterns(List<String> urlPatterns)
    {
        this.urlPatterns = urlPatterns;
    }
}
