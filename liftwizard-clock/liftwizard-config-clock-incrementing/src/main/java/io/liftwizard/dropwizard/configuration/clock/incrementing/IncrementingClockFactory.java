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

package io.liftwizard.dropwizard.configuration.clock.incrementing;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;
import io.liftwizard.clock.incrementing.IncrementingClock;
import io.liftwizard.dropwizard.configuration.clock.ClockFactory;

@JsonTypeName("incrementing")
@AutoService(ClockFactory.class)
public class IncrementingClockFactory implements ClockFactory
{
    private @Valid @NotNull Instant instant      = Instant.parse("2000-12-31T23:59:59Z");
    private @Valid @NotNull String  timeZoneName = "UTC";

    @NotNull
    @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS, inclusive = false)
    private io.dropwizard.util.Duration incrementAmount = io.dropwizard.util.Duration.seconds(1);

    private IncrementingClock incrementingClock;

    @Nonnull
    @Override
    public Clock createClock()
    {
        if (this.incrementingClock == null)
        {
            this.incrementingClock = this.createIncrementingClock();
        }
        return this.incrementingClock;
    }

    private IncrementingClock createIncrementingClock()
    {
        ZoneId   zoneId      = ZoneId.of(this.timeZoneName);
        long     nanoseconds = this.incrementAmount.toNanoseconds();
        Duration duration    = Duration.ofNanos(nanoseconds);
        return new IncrementingClock(this.instant, zoneId, duration);
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

    @JsonProperty
    public io.dropwizard.util.Duration getIncrementAmount()
    {
        return this.incrementAmount;
    }

    @JsonProperty
    public void setIncrementAmount(io.dropwizard.util.Duration incrementAmount)
    {
        this.incrementAmount = incrementAmount;
    }

    @ValidationMethod(message = "Invalid timeZoneName")
    @JsonIgnore
    public boolean isValidTimezone()
    {
        TimeZone zoneInfo = TimeZone.getTimeZone(this.timeZoneName);
        if (zoneInfo != null)
        {
            return true;
        }

        String message = "Got timeZoneName '%s' but expected one of: %s".formatted(
                this.timeZoneName,
                Arrays.toString(TimeZone.getAvailableIDs()));
        throw new IllegalStateException(message);
    }
}
