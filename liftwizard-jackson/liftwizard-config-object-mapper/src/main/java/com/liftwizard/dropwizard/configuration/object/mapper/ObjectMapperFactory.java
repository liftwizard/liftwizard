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
 */

package com.liftwizard.dropwizard.configuration.object.mapper;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectMapperFactory
{
    private          boolean enabled                = true;
    // Should usually be false in production
    private          boolean prettyPrint            = true;
    private @NotNull Include serializationInclusion = Include.NON_ABSENT;

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isPrettyPrint()
    {
        return this.prettyPrint;
    }

    @JsonProperty
    public void setPrettyPrint(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }

    public Include getSerializationInclusion()
    {
        return this.serializationInclusion;
    }

    @JsonProperty
    public void setSerializationInclusion(Include serializationInclusion)
    {
        this.serializationInclusion = serializationInclusion;
    }
}
