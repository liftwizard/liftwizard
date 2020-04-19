package com.liftwizard.dropwizard.bundle.clock;

import java.time.Clock;
import java.util.Objects;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ClockBinder
        extends AbstractBinder
{
    private final Clock clock;

    public ClockBinder(Clock clock)
    {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    protected void configure()
    {
        this.bind(this.clock).to(Clock.class);
    }
}
