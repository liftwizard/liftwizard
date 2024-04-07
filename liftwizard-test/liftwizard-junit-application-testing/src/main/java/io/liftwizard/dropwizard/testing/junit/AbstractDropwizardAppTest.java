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
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.junit.rule.match.json.JsonMatchRule;
import io.liftwizard.reladomo.test.rule.ReladomoLoadDataTestRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractDropwizardAppTest
{
    @Rule
    public final JsonMatchRule jsonMatchRule = new JsonMatchRule(this.getClass());

    protected final DropwizardAppRule<?> appRule                  = this.getDropwizardAppRule();
    protected final TestRule             reladomoLoadDataTestRule = new ReladomoLoadDataTestRule();
    protected final TestRule             logMarkerTestRule        = new LogMarkerTestRule();

    @Rule
    public final TestRule rule = RuleChain
            .outerRule(this.appRule)
            .around(this.reladomoLoadDataTestRule)
            .around(this.logMarkerTestRule);

    @Nonnull
    protected abstract DropwizardAppRule getDropwizardAppRule();

    protected Client getClient(@Nonnull String testName)
    {
        var jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTimeout(Duration.minutes(5));

        String className  = this.getClass().getCanonicalName();
        String clientName = className + "." + testName;

        return new JerseyClientBuilder(this.appRule.getEnvironment())
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

        this.jsonMatchRule.assertFileContents(expectedResponseClassPathLocation, actualJsonResponse);
    }

    protected void assertResponseStatus(@Nonnull Response response, Status status)
    {
        assertThat(response.hasEntity()).isTrue();
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        assertThat(response.getStatusInfo()).as(entityAsString).isEqualTo(status);
    }
}
