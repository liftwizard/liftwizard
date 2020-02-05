package com.liftwizard.dropwizard.configuration.clock.fixed;

import java.io.File;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.liftwizard.dropwizard.configuration.clock.ClockFactory;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FixedClockFactoryTest
{
    private final ObjectMapper                           objectMapper = Jackson.newObjectMapper();
    private final Validator                              validator    = Validators.newValidator();
    private final JsonConfigurationFactory<ClockFactory> factory      =
            new JsonConfigurationFactory<>(ClockFactory.class, this.validator, this.objectMapper, "dw");

    @Test
    public void isDiscoverable()
    {
        // Make sure the types we specified in META-INF gets picked up
        DiscoverableSubtypeResolver discoverableSubtypeResolver = new DiscoverableSubtypeResolver();
        ImmutableList<Class<?>>     discoveredSubtypes          = discoverableSubtypeResolver.getDiscoveredSubtypes();
        assertThat(discoveredSubtypes, hasItem(FixedClockFactory.class));
    }

    @Test
    public void fixedClock() throws Exception
    {
        URL          resource     = Resources.getResource("test-config.json");
        File         json         = new File(resource.toURI());
        ClockFactory clockFactory = this.factory.build(json);
        assertThat(clockFactory, instanceOf(FixedClockFactory.class));
        Clock clock = clockFactory.createClock();
        assertThat(clock.getZone(), is(ZoneId.of("America/New_York")));
        Instant actualInstant   = clock.instant();
        Instant expectedInstant = Instant.parse("2000-12-31T23:59:59Z");
        assertThat(actualInstant, is(expectedInstant));
    }
}
