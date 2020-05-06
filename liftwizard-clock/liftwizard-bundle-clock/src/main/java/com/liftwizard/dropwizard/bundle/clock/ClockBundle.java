package com.liftwizard.dropwizard.bundle.clock;

import java.time.Clock;

import javax.annotation.Nonnull;

import com.liftwizard.dropwizard.configuration.clock.ClockFactory;
import com.liftwizard.dropwizard.configuration.clock.ClockFactoryProvider;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public class ClockBundle
        implements ConfiguredBundle<ClockFactoryProvider>
{
    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(ClockFactoryProvider configuration, @Nonnull Environment environment)
    {
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.runWithMdc(configuration, environment);
        }
    }

    private void runWithMdc(
            ClockFactoryProvider configuration,
            @Nonnull Environment environment)
    {
        ClockFactory clockFactory = configuration.getClockFactory();
        Clock        clock        = clockFactory.createClock();
        ClockBinder  clockBinder  = new ClockBinder(clock);
        environment.jersey().register(clockBinder);
    }
}
