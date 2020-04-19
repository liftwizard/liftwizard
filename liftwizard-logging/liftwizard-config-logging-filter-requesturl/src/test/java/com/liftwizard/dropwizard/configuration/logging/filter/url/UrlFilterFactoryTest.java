package com.liftwizard.dropwizard.configuration.logging.filter.url;

import java.io.File;
import java.net.URL;

import javax.validation.Validator;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UrlFilterFactoryTest
{
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<RequestUrlFilterFactory> factory = new JsonConfigurationFactory<>(
            RequestUrlFilterFactory.class,
            this.validator,
            this.objectMapper,
            "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        DiscoverableSubtypeResolver discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        ImmutableList<Class<?>>     discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(RequestUrlFilterFactory.class));
    }

    @Test
    public void filterUrl() throws Exception
    {
        URL                     resource         = Resources.getResource("test-config.json");
        File                    json             = new File(resource.toURI());
        RequestUrlFilterFactory urlFilterFactory = this.factory.build(json);
        Filter<IAccessEvent>    filter           = urlFilterFactory.build();
        FakeAccessEvent         bannedEvent      = new FakeAccessEvent("banned");
        FakeAccessEvent         allowedEvent     = new FakeAccessEvent("allowed");

        assertThat(urlFilterFactory, instanceOf(RequestUrlFilterFactory.class));
        assertThat(filter.decide(bannedEvent), is(FilterReply.DENY));
        assertThat(filter.decide(allowedEvent), is(FilterReply.NEUTRAL));
    }
}
