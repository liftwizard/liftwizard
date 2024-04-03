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

package io.liftwizard.dropwizard.configuration.uuid.system;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SystemUUIDFactoryTest
{
    @RegisterExtension
    private final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();

    private final ObjectMapper objectMapper = newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<UUIDSupplierFactory> factory =
            new JsonConfigurationFactory<>(UUIDSupplierFactory.class, this.validator, this.objectMapper, "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        var            discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        List<Class<?>> discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(SystemUUIDSupplierFactory.class));
    }

    @Test
    public void systemUUID()
            throws Exception
    {
        UUIDSupplierFactory uuidFactory = this.factory.build(
                new ResourceConfigurationSourceProvider(),
                "config-test.json5");
        assertThat(uuidFactory, instanceOf(SystemUUIDSupplierFactory.class));
        Supplier<UUID> uuidSupplier = uuidFactory.createUUIDSupplier();
        UUID           uuid         = uuidSupplier.get();
        assertThat(uuid, notNullValue());
    }

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }
}
