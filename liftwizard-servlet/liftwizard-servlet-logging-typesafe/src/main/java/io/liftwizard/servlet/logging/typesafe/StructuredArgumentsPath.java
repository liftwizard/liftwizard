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

public class StructuredArgumentsPath
{
    private final String absolute;
    private final String full;
    private       String template;

    public StructuredArgumentsPath(String absolute, String full)
    {
        this.absolute = Objects.requireNonNull(absolute);
        this.full     = Objects.requireNonNull(full);
    }

    @JsonProperty
    public String getAbsolute()
    {
        return this.absolute;
    }

    @JsonProperty
    public String getFull()
    {
        return this.full;
    }

    public void setTemplate(String template)
    {
        if (this.template != null)
        {
            throw new IllegalStateException(this.template);
        }
        this.template = Objects.requireNonNull(template);
    }

    @JsonProperty
    public String getTemplate()
    {
        return this.template;
    }
}
