package com.liftwizard.dropwizard.configuration.clock.system;

import java.time.Clock;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.configuration.clock.ClockFactory;

@JsonTypeName("system")
@AutoService(ClockFactory.class)
public class SystemClockFactory implements ClockFactory
{
    @Nonnull
    @Override
    public Clock createClock()
    {
        return Clock.systemUTC();
    }
}
