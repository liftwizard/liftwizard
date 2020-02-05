package com.liftwizard.dropwizard.configuration.clock.fixed;

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
import com.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.dropwizard.validation.ValidationMethod;

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
