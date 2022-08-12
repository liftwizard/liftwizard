/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.executor;

import java.io.File;
import java.net.URL;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExecutorServiceFactoryTest
{
    @Rule
    public final TestRule logMarkerTestRule = new LogMarkerTestRule();

    private final ObjectMapper objectMapper = newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    @Test
    public void executorServiceFactory()
            throws Exception
    {
        URL  resource = Resources.getResource("executor-service-config-test.json5");
        File json     = new File(resource.toURI());

        JsonConfigurationFactory<ExecutorServiceFactory> factory =
                new JsonConfigurationFactory<>(ExecutorServiceFactory.class, this.validator, this.objectMapper, "dw");

        ExecutorServiceFactory executorServiceFactory = factory.build(json);
    }

    @Test
    public void defaultScheduledExecutorServiceFactory()
            throws Exception
    {
        URL  resource = Resources.getResource("executor-service-config-test.json5");
        File json     = new File(resource.toURI());

        JsonConfigurationFactory<ScheduledExecutorServiceFactory> factory =
                new JsonConfigurationFactory<>(
                        ScheduledExecutorServiceFactory.class,
                        this.validator,
                        this.objectMapper,
                        "dw");

        DefaultScheduledExecutorServiceFactory executorServiceFactory = (DefaultScheduledExecutorServiceFactory) factory.build(json);
    }

    @Test
    public void noopScheduledExecutorServiceFactory()
            throws Exception
    {
        URL  resource = Resources.getResource("noop-executor-service-config-test.json5");
        File json     = new File(resource.toURI());

        JsonConfigurationFactory<ScheduledExecutorServiceFactory> factory =
                new JsonConfigurationFactory<>(
                        ScheduledExecutorServiceFactory.class,
                        this.validator,
                        this.objectMapper,
                        "dw");

        NoopScheduledExecutorServiceFactory executorServiceFactory = (NoopScheduledExecutorServiceFactory) factory.build(json);
    }


    @Test
    public void invalidExecutorServiceFactory()
            throws Exception
    {
        URL  resource = Resources.getResource("invalid-executor-service-config-test.json5");
        File json     = new File(resource.toURI());

        JsonConfigurationFactory<ExecutorServiceFactory> factory =
                new JsonConfigurationFactory<>(ExecutorServiceFactory.class, this.validator, this.objectMapper, "dw");

        try
        {
            ExecutorServiceFactory executorServiceFactory = factory.build(json);
            Assert.fail();
        }
        catch (ConfigurationValidationException e)
        {
            String message = e.getMessage();
            assertThat(message, containsString("maxThreads < minThreads"));
        }
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }
}
