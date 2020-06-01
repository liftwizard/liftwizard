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

package io.liftwizard.dropwizard.configuration.clock.fixed;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;

@JsonTypeName("fixed")
@AutoService(ClockFactory.class)
public class FixedClockFactory implements ClockFactory
{
    private @Valid @NotNull Instant instant      = Instant.parse("2000-12-31T23:59:59Z");
    private @Valid @NotNull String  timeZoneName = "UTC";

    @Nonnull
    @Override
    public Clock createClock()
    {
        ZoneId zoneId = ZoneId.of(this.timeZoneName);
        return Clock.fixed(this.instant, zoneId);
    }

    @JsonProperty
    public Instant getInstant()
    {
        return this.instant;
    }

    @JsonProperty
    public void setInstant(Instant instant)
    {
        this.instant = instant;
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
        TimeZone zoneInfo = TimeZone.getTimeZone(this.timeZoneName);
        if (zoneInfo == null)
        {
            String message = String.format(
                    "Got timeZoneName '%s' but expected one of: %s",
                    this.timeZoneName,
                    Arrays.toString(TimeZone.getAvailableIDs()));
            throw new IllegalStateException(message);
        }
        return zoneInfo != null;
    }
}
