package com.liftwizard.dropwizard.configuration.config.logging;

import com.liftwizard.dropwizard.configuration.enabled.EnabledFactory;

public interface ConfigLoggingFactoryProvider
{
    EnabledFactory getConfigLoggingFactory();
}
