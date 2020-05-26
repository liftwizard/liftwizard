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

package com.liftwizard.dropwizard.configuration.http.logging;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Size;

public class JerseyHttpLoggingFactory
{
    // Should usually be disabled in production
    private          boolean enabled       = true;
    private @NotNull String  level         = "INFO";
    private @NotNull String  verbosity     = "PAYLOAD_ANY";
    private @NotNull Size    maxEntitySize = Size.kilobytes(8);

    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getLevel()
    {
        return this.level;
    }

    @JsonProperty
    public void setLevel(String level)
    {
        this.level = level;
    }

    public String getVerbosity()
    {
        return this.verbosity;
    }

    @JsonProperty
    public void setVerbosity(String verbosity)
    {
        this.verbosity = verbosity;
    }

    public Size getMaxEntitySize()
    {
        return this.maxEntitySize;
    }

    @JsonProperty
    public void setMaxEntitySize(Size maxEntitySize)
    {
        this.maxEntitySize = maxEntitySize;
    }
}
