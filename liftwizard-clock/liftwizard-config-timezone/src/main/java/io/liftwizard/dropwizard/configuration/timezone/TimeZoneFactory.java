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

package io.liftwizard.dropwizard.configuration.timezone;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.TimeZone;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;

public class TimeZoneFactory
{
    private @Valid @NotNull String timeZoneName = "UTC";

    public TimeZone build()
    {
        return "system".equalsIgnoreCase(this.timeZoneName)
                ? TimeZone.getDefault()
                : TimeZone.getTimeZone(this.timeZoneName);
    }

    @JsonProperty("timeZone")
    public String getTimeZoneName()
    {
        return this.timeZoneName;
    }

    @JsonProperty("timeZone")
    public void setTimeZoneName(String timeZoneName)
    {
        this.timeZoneName = timeZoneName;
    }

    @ValidationMethod(message = "Invalid timeZoneName")
    @JsonIgnore
    public boolean isValidTimezone()
    {
        if ("system".equalsIgnoreCase(this.timeZoneName))
        {
            return true;
        }

        try
        {
            ZoneId.of(this.timeZoneName);
        }
        catch (DateTimeException e)
        {
            return false;
        }
        return true;
    }
}
