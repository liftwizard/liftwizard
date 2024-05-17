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

package io.liftwizard.dropwizard.configuration.timezone;

import java.io.IOException;
import java.util.TimeZone;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.serialization.jackson.config.ObjectMapperConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeZoneFactoryTest
{
    @RegisterExtension
    private final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();

    private final ObjectMapper objectMapper = newObjectMapper();
    private final Validator    validator    = Validators.newValidator();

    private final JsonConfigurationFactory<TimeZoneFactory> factory =
            new JsonConfigurationFactory<>(TimeZoneFactory.class, this.validator, this.objectMapper, "dw");

    private static ObjectMapper newObjectMapper()
    {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectMapperConfig.configure(objectMapper);
        return objectMapper;
    }

    @Test
    void deserialize()
            throws ConfigurationException, IOException
    {
        var configurationProvider = new ResourceConfigurationSourceProvider();

        TimeZoneFactory timeZoneFactory = this.factory.build(
                configurationProvider,
                "config-test.json5");
        String   timeZoneName = timeZoneFactory.getTimeZoneName();
        TimeZone timeZone     = timeZoneFactory.build();

        assertThat(timeZoneName).isEqualTo("America/New_York");
        assertThat(timeZone.getDisplayName()).isEqualTo("Eastern Standard Time");
    }

    @Test
    void deserializeSystem()
            throws ConfigurationException, IOException
    {
        var configurationProvider = new ResourceConfigurationSourceProvider();

        TimeZoneFactory timeZoneFactory = this.factory.build(
                configurationProvider,
                "config-test-system.json5");
        String   timeZoneName = timeZoneFactory.getTimeZoneName();
        TimeZone timeZone     = timeZoneFactory.build();

        assertThat(timeZoneName).isEqualTo("system");
        assertThat(timeZone).isEqualTo(TimeZone.getDefault());
    }

    @Test
    void deserializeBad()
    {
        var configurationProvider = new ResourceConfigurationSourceProvider();
        assertThrows(
                ConfigurationValidationException.class,
                () -> this.factory.build(configurationProvider, "config-test-bad.json5"));
    }

    @Test
    void buildUtc()
    {
        TimeZone timeZone1 = getTimeZone("UTC");
        assertThat(timeZone1.getDisplayName()).isEqualTo("Coordinated Universal Time");
        assertThat(timeZone1.getID()).isEqualTo("UTC");
    }

    @Test
    void buildGood()
    {
        TimeZone timeZone2 = getTimeZone("America/New_York");
        assertThat(timeZone2.getDisplayName()).isEqualTo("Eastern Standard Time");
        assertThat(timeZone2.getID()).isEqualTo("America/New_York");
    }

    @Test
    void buildSystem()
    {
        TimeZone timeZone3 = getTimeZone("system");
        assertThat(timeZone3).isEqualTo(TimeZone.getDefault());
    }

    @Test
    void buildBad()
    {
        TimeZone timeZone4 = getTimeZone("not a real timezone");
        assertThat(timeZone4.getDisplayName()).isEqualTo("Greenwich Mean Time");
        assertThat(timeZone4.getID()).isEqualTo("GMT");
    }

    private static TimeZone getTimeZone(String zoneId)
    {
        TimeZoneFactory timezoneFactory = new TimeZoneFactory();
        timezoneFactory.setTimeZoneName(zoneId);
        return timezoneFactory.build();
    }
}
