package com.liftwizard.dropwizard.bundle.prioritized;

import io.dropwizard.ConfiguredBundle;

public interface PrioritizedBundle<T>
        extends ConfiguredBundle<T>
{
    default int getPriority()
    {
        return 0;
    }
}
