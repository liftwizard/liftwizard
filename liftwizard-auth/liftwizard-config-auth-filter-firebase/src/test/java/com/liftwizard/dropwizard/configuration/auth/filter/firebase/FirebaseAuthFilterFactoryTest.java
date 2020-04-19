package com.liftwizard.dropwizard.configuration.auth.filter.firebase;

import java.io.File;
import java.net.URL;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class FirebaseAuthFilterFactoryTest
{
    private final ObjectMapper                                objectMapper = Jackson.newObjectMapper();
    private final Validator                                   validator    = Validators.newValidator();
    private final YamlConfigurationFactory<AuthFilterFactory> factory      =
            new YamlConfigurationFactory<>(AuthFilterFactory.class, this.validator, this.objectMapper, "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        DiscoverableSubtypeResolver discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        ImmutableList<Class<?>>     discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(FirebaseAuthFilterFactory.class));
    }

    @Test
    public void firebaseAuthFilter() throws Exception
    {
        URL               resource          = Resources.getResource("test-config.yml");
        File              yml               = new File(resource.toURI());
        AuthFilterFactory authFilterFactory = this.factory.build(yml);
        assertThat(authFilterFactory, instanceOf(FirebaseAuthFilterFactory.class));
    }
}
