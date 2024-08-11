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

package io.liftwizard.dropwizard.testing.junit;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.junit.extension.match.json.JsonMatchExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public abstract class AbstractDropwizardAppTest
{
    @RegisterExtension
    protected final JsonMatchExtension jsonMatchExtension = new JsonMatchExtension(this.getClass());
    @RegisterExtension
    protected final LiftwizardAppExtension<?> appExtension = this.getDropwizardAppExtension();
    @RegisterExtension
    protected final ReladomoLoadDataExtension reladomoLoadDataExtension = new ReladomoLoadDataExtension();
    @RegisterExtension
    protected final LogMarkerTestExtension logMarkerExtension = new LogMarkerTestExtension();

    @Nonnull
    protected abstract LiftwizardAppExtension<?> getDropwizardAppExtension();

    protected Client getClient(@Nonnull String testName)
    {
        var jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTimeout(Duration.minutes(5));

        String className = this.getClass().getCanonicalName();
        String clientName = className + "." + testName;

        return new JerseyClientBuilder(this.appExtension.getEnvironment())
                .using(jerseyClientConfiguration)
                .build(clientName);
    }

    protected void assertEmptyResponse(Status expectedStatus, Response actualResponse)
    {
        assertThat(actualResponse.hasEntity()).isFalse();
        assertThat(actualResponse.getStatusInfo()).isEqualTo(expectedStatus);
    }

    protected void assertResponse(String testName, Status expectedStatus, Response actualResponse)
    {
        this.assertResponseStatus(actualResponse, expectedStatus);
        String actualJsonResponse = actualResponse.readEntity(String.class);

        String expectedResponseClassPathLocation = this.getClass().getSimpleName() + "." + testName + ".json";

        this.jsonMatchExtension.assertFileContents(expectedResponseClassPathLocation, actualJsonResponse);
    }

    protected void assertResponseStatus(@Nonnull Response response, Status status)
    {
        assertThat(response.hasEntity()).isTrue();
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        assertThat(response.getStatusInfo()).as(entityAsString).isEqualTo(status);
    }
}
