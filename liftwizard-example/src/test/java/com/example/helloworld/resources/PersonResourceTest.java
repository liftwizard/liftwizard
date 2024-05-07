package com.example.helloworld.resources;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.HelloWorldApplication;
import com.example.helloworld.HelloWorldConfiguration;
import io.dropwizard.testing.ResourceHelpers;
import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import io.liftwizard.reladomo.test.extension.ReladomoTestFile;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class PersonResourceTest
{
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.json5");

    @RegisterExtension
    @Order(1)
    private final LiftwizardAppExtension<HelloWorldConfiguration> dropwizardAppExtension = new LiftwizardAppExtension<>(
            HelloWorldApplication.class,
            CONFIG_PATH);

    @RegisterExtension
    @Order(2)
    private final BeforeEachCallback dbMigrateRule = context -> this.dropwizardAppExtension
            .getApplication()
            .run("db", "migrate", CONFIG_PATH);

    @RegisterExtension
    @Order(3)
    private final ReladomoInitializeExtension initializeExtension =
            new ReladomoInitializeExtension("reladomo-runtime-configuration/ReladomoRuntimeConfiguration.xml");

    @RegisterExtension
    @Order(4)
    private final ReladomoPurgeAllExtension purgeAllExtension  = new ReladomoPurgeAllExtension();

    @RegisterExtension
    @Order(5)
    private final ReladomoLoadDataExtension loadDataExtension  = new ReladomoLoadDataExtension();

    @RegisterExtension
    @Order(6)
    private final LogMarkerTestExtension    logMarkerExtension = new LogMarkerTestExtension();

    @Test
    @ReladomoTestFile("test-data/person.txt")
    public void getPersonSuccess() throws JSONException
    {
        Response response = this.getPersonResponse(1);
        this.assertResponseStatus(response, Status.OK);
        String jsonResponse = response.readEntity(String.class);

        // <editor-fold desc="Expected JSON">
        // language=JSON
        String expected = """
                {
                  "id"      : 1,
                  "fullName": "Full Name",
                  "jobTitle": "Job Title"
                }\s""";
        // </editor-fold>
        JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
    }

    @Test
    @ReladomoTestFile("test-data/person.txt")
    public void getPersonNotFound() throws JSONException
    {
        Response response = this.getPersonResponse(2);
        this.assertResponseStatus(response, Status.NOT_FOUND);
        String jsonResponse = response.readEntity(String.class);

        // <editor-fold desc="Expected JSON">
        // language=JSON
        String expected = """
                {
                  "code"   : 404,
                  "message": "No such user."
                }\s""";
        // </editor-fold>
        JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
    }

    private Response getPersonResponse(int personId)
    {
        return this
                .dropwizardAppExtension.client()
                .target("http://localhost:{port}/people/{personId}")
                .resolveTemplate("port", this.dropwizardAppExtension.getLocalPort())
                .resolveTemplate("personId", personId)
                .request()
                .get();
    }

    private void assertResponseStatus(@Nonnull Response response, Status status)
    {
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        Assertions.assertThat(response.getStatusInfo().toEnum()).as(entityAsString).isEqualTo(status);
    }
}
