package com.liftwizard.dropwizard.bundle.environment.config;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class EnvironmentConfigBundle
        implements ConfiguredBundle<Object>
{
    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        ConfigurationSourceProvider configurationSourceProvider = bootstrap.getConfigurationSourceProvider();

        EnvironmentVariableSubstitutor environmentVariableSubstitutor = new EnvironmentVariableSubstitutor();
        environmentVariableSubstitutor.setPreserveEscapes(true);

        ConfigurationSourceProvider wrapped = new SubstitutingSourceProvider(
                configurationSourceProvider,
                environmentVariableSubstitutor);

        bootstrap.setConfigurationSourceProvider(wrapped);
    }

    @Override
    public void run(Object configuration, Environment environment)
    {
    }
}
