package com.liftwizard.dropwizard.configuration.clock.incrementing;

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
import com.liftwizard.clock.incrementing.IncrementingClock;
import com.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.ValidationMethod;

@JsonTypeName("incrementing")
@AutoService(ClockFactory.class)
public class IncrementingClockFactory implements ClockFactory
{
    private @Valid @NotNull Instant instant      = Instant.parse("2000-12-31T23:59:59Z");
    private @Valid @NotNull String  timeZoneName = "UTC";

    @NotNull
    @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS, inclusive = false)
    private io.dropwizard.util.Duration incrementAmount = io.dropwizard.util.Duration.seconds(1);

    @Nonnull
    @Override
    public Clock createClock()
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
