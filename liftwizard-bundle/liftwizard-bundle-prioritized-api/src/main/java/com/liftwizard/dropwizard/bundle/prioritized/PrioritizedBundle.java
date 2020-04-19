package com.liftwizard.dropwizard.bundle.prioritized;

import javax.annotation.Nonnull;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public interface PrioritizedBundle<T>
        extends ConfiguredBundle<T>
{
    String MDC_BUNDLE   = "liftwizard.bundle";
    String MDC_PRIORITY = "liftwizard.priority";

    default int getPriority()
    {
        return 0;
    }

    default <C> C safeCastConfiguration(Class<C> aClass, Object configuration)
    {
        if (aClass.isInstance(configuration))
        {
            return aClass.cast(configuration);
        }

        String message = String.format(
                "Expected configuration to implement %s but found %s",
                aClass.getCanonicalName(),
                configuration.getClass().getCanonicalName());
        throw new IllegalStateException(message);
    }

    @Override
    default void initialize(@Nonnull Bootstrap<?> bootstrap)
    {
        try (
                MDCCloseable mdc1 = MDC.putCloseable(MDC_BUNDLE, this.getClass().getSimpleName());
                MDCCloseable mdc2 = MDC.putCloseable(MDC_PRIORITY, String.valueOf(this.getPriority())))
        {
            this.initializeWithMdc(bootstrap);
        }
    }

    default void initializeWithMdc(@Nonnull Bootstrap<?> bootstrap)
    {
    }

    @Override
    default void run(@Nonnull T configuration, @Nonnull Environment environment) throws Exception
    {
        try (
                MDCCloseable mdc1 = MDC.putCloseable(MDC_BUNDLE, this.getClass().getSimpleName());
                MDCCloseable mdc2 = MDC.putCloseable(MDC_PRIORITY, String.valueOf(this.getPriority())))
        {
            this.runWithMdc(configuration, environment);
        }
    }

    void runWithMdc(@Nonnull T configuration, @Nonnull Environment environment) throws Exception;
}
