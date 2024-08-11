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

package io.liftwizard.dropwizard.configuration.system.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SystemPropertiesFactory
{
    private final Map<String, String> systemProperties = new LinkedHashMap<>();
    private boolean strict;

    @JsonAnyGetter
    public Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    @JsonAnySetter
    public void setSystemProperties(String name, String value)
    {
        this.systemProperties.put(name, value);
    }

    @JsonProperty
    public boolean isStrict()
    {
        return this.strict;
    }

    @JsonProperty
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }
}
