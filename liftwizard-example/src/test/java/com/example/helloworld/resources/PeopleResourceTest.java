package com.example.helloworld.resources;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.HelloWorldApplication;
import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.dto.PersonDTO;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoInitializeTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoLoadDataTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoPurgeAllTestRule;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.hamcrest.CoreMatchers.is;

/**
 * Unit tests for {@link PeopleResource}.
 */
public class PeopleResourceTest {
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.json5");

    private final DropwizardAppRule<HelloWorldConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
            HelloWorldApplication.class,
            CONFIG_PATH);

    private final ExternalResource dbMigrateRule = new ExternalResource()
    {
        @Override
        protected void before() throws Throwable
        {
            PeopleResourceTest.this.dropwizardAppRule.getApplication().run("db", "migrate", CONFIG_PATH);
        }
    };

    private final ReladomoInitializeTestRule initializeTestRule =
            new ReladomoInitializeTestRule("reladomo-runtime-configuration/ReladomoRuntimeConfiguration.xml");

    private final ReladomoPurgeAllTestRule purgeAllTestRule = new ReladomoPurgeAllTestRule();
    private final ReladomoLoadDataTestRule loadDataTestRule = new ReladomoLoadDataTestRule();

    private final TestRule logMarkerTestRule = new LogMarkerTestRule();

    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(this.dropwizardAppRule)
            .around(this.dbMigrateRule)
            .around(this.initializeTestRule)
            .around(this.purgeAllTestRule)
            .around(this.loadDataTestRule)
            .around(this.logMarkerTestRule);

    private final PersonDTO personDTO = new PersonDTO("Full Name", "Job Title");

    @Test
    public void createPerson() throws JSONException
    {
        Client client = this.dropwizardAppRule.client();

        {
            Response response = client.target(
                    String.format("http://localhost:%d/people/", this.dropwizardAppRule.getLocalPort()))
                    .request()
                    .post(Entity.entity(this.personDTO, MediaType.APPLICATION_JSON_TYPE));

            this.assertResponseStatus(response, Status.OK);
            String jsonResponse = response.readEntity(String.class);

            //<editor-fold desc="Expected JSON">
            //language=JSON
            String expected = ""
                    + "{\n"
                    + "  \"id\"      : 1,\n"
                    + "  \"fullName\": \"Full Name\",\n"
                    + "  \"jobTitle\": \"Job Title\"\n"
                    + "}\n";
            //</editor-fold>
            JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
        }

        {
            Response response = client.target(
                    String.format("http://localhost:%d/people/", this.dropwizardAppRule.getLocalPort()))
                    .request()
                    .get();

            this.assertResponseStatus(response, Status.OK);
            String jsonResponse = response.readEntity(String.class);

            //<editor-fold desc="Expected JSON">
            //language=JSON
            String expected = ""
                    + "[\n"
                    + "  {\n"
                    + "    \"id\"      : 1,\n"
                    + "    \"fullName\": \"Full Name\",\n"
                    + "    \"jobTitle\": \"Job Title\"\n"
                    + "  }\n"
                    + "]\n";
            //</editor-fold>
            JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
        }
    }

    private void assertResponseStatus(@Nonnull Response response, Status status)
    {
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        Assert.assertThat(entityAsString, response.getStatusInfo(), is(status));
    }
}
