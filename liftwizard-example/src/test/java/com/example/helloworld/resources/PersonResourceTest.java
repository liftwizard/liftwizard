package com.example.helloworld.resources;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.HelloWorldApplication;
import com.example.helloworld.HelloWorldConfiguration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoInitializeTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoLoadDataTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoPurgeAllTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoTestFile;
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

public class PersonResourceTest
{
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.json5");

    private final DropwizardAppRule<HelloWorldConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
            HelloWorldApplication.class,
            CONFIG_PATH);

    private final ExternalResource dbMigrateRule = new ExternalResource()
    {
        @Override
        protected void before() throws Throwable
        {
            PersonResourceTest.this.dropwizardAppRule.getApplication().run("db", "migrate", CONFIG_PATH);
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

    @Test
    @ReladomoTestFile("test-data/person.txt")
    public void getPersonSuccess() throws JSONException
    {
        Response response = this.getPersonResponse(1);
        this.assertResponseStatus(response, Status.OK);
        String jsonResponse = response.readEntity(String.class);

        //<editor-fold desc="Expected JSON">
        //language=JSON
        String expected = """
                {
                  "id"      : 1,
                  "fullName": "Full Name",
                  "jobTitle": "Job Title"
                }\s""";
        //</editor-fold>
        JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
    }

    @Test
    @ReladomoTestFile("test-data/person.txt")
    public void getPersonNotFound() throws JSONException
    {
        Response response = this.getPersonResponse(2);
        this.assertResponseStatus(response, Status.NOT_FOUND);
        String jsonResponse = response.readEntity(String.class);

        //<editor-fold desc="Expected JSON">
        //language=JSON
        String expected = """
                {
                  "code"   : 404,
                  "message": "No such user."
                }\s""";
        //</editor-fold>
        JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
    }

    private Response getPersonResponse(int personId)
    {
        Client client = this.dropwizardAppRule.client();

        return client.target(
                String.format("http://localhost:%d/people/{personId}", this.dropwizardAppRule.getLocalPort()))
                .resolveTemplate("personId", personId)
                .request()
                .get();
    }

    private void assertResponseStatus(@Nonnull Response response, Status status)
    {
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        Assert.assertThat(entityAsString, response.getStatusInfo(), is(status));
    }
}
