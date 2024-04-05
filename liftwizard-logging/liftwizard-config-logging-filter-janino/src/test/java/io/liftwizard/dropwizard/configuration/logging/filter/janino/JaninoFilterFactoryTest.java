/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.logging.filter.janino;

import java.util.List;

import javax.validation.Validator;

import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class JaninoFilterFactoryTest
{
    @RegisterExtension
    private final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();

    private final ObjectMapper objectMapper = newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<JaninoFilterFactory> factory = new JsonConfigurationFactory<>(
            JaninoFilterFactory.class,
            this.validator,
            this.objectMapper,
            "dw");

    @Test
    void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        var            discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        List<Class<?>> discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes).contains(JaninoFilterFactory.class);
    }

    @Test
    void filterJanino()
            throws Exception
    {
        JaninoFilterFactory janinoFilterFactory = this.factory.build(
                new ResourceConfigurationSourceProvider(),
                "config-test.json5");
        Filter<ILoggingEvent> filter = janinoFilterFactory.build();

        assertThat(janinoFilterFactory).isInstanceOf(JaninoFilterFactory.class);
        assertThat(filter).isInstanceOf(EvaluatorFilter.class);

        EventEvaluator<?> evaluator = ((EvaluatorFilter<?>) filter).getEvaluator();
        assertThat(evaluator).isInstanceOf(JaninoEventEvaluator.class);
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }
}
