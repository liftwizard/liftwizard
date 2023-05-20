/*
 * Copyright 2022 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.auth.filter.header;

import java.io.File;
import java.net.URL;
import java.security.Principal;
import java.util.List;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class HeaderAuthFilterFactoryTest
{
    @Rule
    public final TestRule logMarkerTestRule = new LogMarkerTestRule();

    private final ObjectMapper objectMapper = newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<AuthFilterFactory> factory =
            new JsonConfigurationFactory<>(AuthFilterFactory.class, this.validator, this.objectMapper, "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        var            discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        List<Class<?>> discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(HeaderAuthFilterFactory.class));
    }

    @Test
    public void headerAuthFilter()
            throws Exception
    {
        URL               resource          = Resources.getResource("config-test.json5");
        File              json              = new File(resource.toURI());
        AuthFilterFactory authFilterFactory = this.factory.build(json);
        assertThat(authFilterFactory, instanceOf(HeaderAuthFilterFactory.class));
        AuthFilter<?, ? extends Principal> authFilter = authFilterFactory.createAuthFilter();
        assertThat(authFilter, instanceOf(HeaderAuthFilter.class));
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }
}
