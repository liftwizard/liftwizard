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
 *
 */

package io.liftwizard.dropwizard.configuration.auth.filter.header;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.auth.AuthFilter;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;

@JsonTypeName("header")
@AutoService(AuthFilterFactory.class)
public class HeaderAuthFilterFactory
        implements AuthFilterFactory
{
    @NotNull
    private final String header;
    private final String prefix;

    @JsonCreator
    public HeaderAuthFilterFactory(
            @JsonProperty("header") String header,
            @JsonProperty("prefix") String prefix)
    {
        this.header = header;
        this.prefix = prefix;
    }

    @NotNull
    public String getHeader()
    {
        return this.header;
    }

    public String getPrefix()
    {
        return this.prefix;
    }

    @Nonnull
    @Override
    public AuthFilter<?, HeaderPrincipal> createAuthFilter()
    {
        return new HeaderAuthFilter.Builder(this.header, this.prefix)
                .setAuthenticator(new HeaderAuthenticator(this.prefix))
                .setUnauthorizedHandler(new JSONUnauthorizedHandler())
                .setPrefix("Header")
                .buildAuthFilter();
    }
}
