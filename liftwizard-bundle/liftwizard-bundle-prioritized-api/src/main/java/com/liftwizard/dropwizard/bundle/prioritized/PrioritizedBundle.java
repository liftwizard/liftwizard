package com.liftwizard.dropwizard.bundle.prioritized;

import io.dropwizard.ConfiguredBundle;

public interface PrioritizedBundle<T>
        extends ConfiguredBundle<T>
{
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
}
