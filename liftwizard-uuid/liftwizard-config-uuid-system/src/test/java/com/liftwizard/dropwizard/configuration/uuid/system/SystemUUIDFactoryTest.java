package com.liftwizard.dropwizard.configuration.uuid.system;

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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SystemUUIDFactoryTest
{
    private final ObjectMapper                                  objectMapper = Jackson.newObjectMapper();
    private final Validator                                     validator    = Validators.newValidator();
    private final YamlConfigurationFactory<UUIDSupplierFactory> factory      =
            new YamlConfigurationFactory<>(UUIDSupplierFactory.class, this.validator, this.objectMapper, "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        DiscoverableSubtypeResolver discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        ImmutableList<Class<?>>     discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(SystemUUIDSupplierFactory.class));
    }

    @Test
    public void systemUUID() throws Exception
    {
        URL                 resource    = Resources.getResource("test-config.yml");
        File                yml         = new File(resource.toURI());
        UUIDSupplierFactory uuidFactory = this.factory.build(yml);
        assertThat(uuidFactory, instanceOf(SystemUUIDSupplierFactory.class));
        Supplier<UUID> uuidSupplier = uuidFactory.createUUIDSupplier();
        UUID           uuid         = uuidSupplier.get();
        assertThat(uuid, notNullValue());
    }
}
