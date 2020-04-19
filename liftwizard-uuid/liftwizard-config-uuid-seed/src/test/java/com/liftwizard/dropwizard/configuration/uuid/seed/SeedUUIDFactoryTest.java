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

package com.liftwizard.dropwizard.configuration.uuid.seed;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.function.Supplier;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SeedUUIDFactoryTest
{
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final YamlConfigurationFactory<UUIDSupplierFactory> factory = new YamlConfigurationFactory<>(
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
        URL                 resource    = Resources.getResource("test-config.yml");
        File                yml         = new File(resource.toURI());
        UUIDSupplierFactory uuidFactory = this.factory.build(yml);
        assertThat(uuidFactory, instanceOf(SeedUUIDSupplierFactory.class));
        Supplier<UUID> uuidSupplier     = uuidFactory.createUUIDSupplier();
        UUID           uuid             = uuidSupplier.get();
        String         actualUUIDString = uuid.toString();
        assertThat(actualUUIDString, is("4bb909d0-4c29-3f81-957f-aab6d7f73c9f"));
    }
}
