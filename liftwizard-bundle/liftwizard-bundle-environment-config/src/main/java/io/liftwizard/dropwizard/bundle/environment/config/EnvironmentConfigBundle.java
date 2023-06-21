/*
 * Copyright 2020 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.dropwizard.bundle.environment.config;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

/**
 * Supports environment variable substitution inside Dropwizard configuration files.
 *
 * @see EnvironmentVariableSubstitutor
 * @see SubstitutingSourceProvider
 * @see <a href="https://liftwizard.io/docs/configuration/environment-variables#environmentconfigbundle">https://liftwizard.io/docs/configuration/environment-variables#environmentconfigbundle</a>
 */
public class EnvironmentConfigBundle
        implements ConfiguredBundle<Object>
{
    private final boolean strict;

    public EnvironmentConfigBundle()
    {
        this(false);
    }

    public EnvironmentConfigBundle(boolean strict)
    {
        this.strict = strict;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
        try (MDCCloseable ignored = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.initializeWithMdc(bootstrap);
        }
    }

    private void initializeWithMdc(Bootstrap<?> bootstrap)
    {
        ConfigurationSourceProvider configurationSourceProvider = bootstrap.getConfigurationSourceProvider();

        EnvironmentVariableSubstitutor environmentVariableSubstitutor = new EnvironmentVariableSubstitutor(this.strict);
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
