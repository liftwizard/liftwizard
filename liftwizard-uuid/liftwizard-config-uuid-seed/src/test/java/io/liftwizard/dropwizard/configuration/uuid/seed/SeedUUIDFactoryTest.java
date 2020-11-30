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

package io.liftwizard.dropwizard.configuration.uuid.seed;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.function.Supplier;

import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SeedUUIDFactoryTest
{
    @Rule
    public final TestRule logMarkerTestRule = new LogMarkerTestRule();

    private final ObjectMapper objectMapper = newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<UUIDSupplierFactory> factory = new JsonConfigurationFactory<>(
            UUIDSupplierFactory.class,
            this.validator,
            this.objectMapper,
            "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        DiscoverableSubtypeResolver discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        ImmutableList<Class<?>>     discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(SeedUUIDSupplierFactory.class));
    }

    @Test
    public void seedUUID() throws Exception
    {
        URL                 resource    = Resources.getResource("config-test.json5");
        File                json        = new File(resource.toURI());
        UUIDSupplierFactory uuidFactory = this.factory.build(json);
        assertThat(uuidFactory, instanceOf(SeedUUIDSupplierFactory.class));
        Supplier<UUID> uuidSupplier     = uuidFactory.createUUIDSupplier();
        UUID           uuid             = uuidSupplier.get();
        String         actualUUIDString = uuid.toString();
        assertThat(actualUUIDString, is("4bb909d0-4c29-3f81-957f-aab6d7f73c9f"));
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper, true, Include.NON_ABSENT);
        return objectMapper;
    }
}
