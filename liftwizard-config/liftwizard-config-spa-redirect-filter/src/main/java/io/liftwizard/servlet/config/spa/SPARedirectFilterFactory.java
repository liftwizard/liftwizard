/*
 * Copyright 2023 Craig Motlin
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

package io.liftwizard.servlet.config.spa;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SPARedirectFilterFactory
{
    private boolean enabled            = true;
    private String  redirectSPAPage    = "/index.html";
    private String  cacheControlHeader = "no-cache, max-age=0";

    // Well known prefixes include /api/ for REST endpoints and /.well-known/ for OIDC callbacks.
    private List<String> wellKnownPathPrefixes = List.of("/api/", "/.well-known/");

    public String getRedirectSPAPage()
    {
        return this.redirectSPAPage;
    }

    @JsonProperty
    public void setRedirectSPAPage(String redirectSPAPage)
    {
        this.redirectSPAPage = redirectSPAPage;
    }

    public String getCacheControlHeader()
    {
        return this.cacheControlHeader;
    }

    @JsonProperty
    public void setCacheControlHeader(String cacheControlHeader)
    {
        this.cacheControlHeader = cacheControlHeader;
    }

    public List<String> getWellKnownPathPrefixes()
    {
        return this.wellKnownPathPrefixes;
    }

    @JsonProperty
    public void setWellKnownPathPrefixes(List<String> wellKnownPathPrefixes)
    {
        this.wellKnownPathPrefixes = wellKnownPathPrefixes;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
