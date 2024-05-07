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
import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * Unit tests for {@link PeopleResource}.
 */
public class PeopleResourceTest {
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

    private final PersonDTO personDTO = new PersonDTO("Full Name", "Job Title");

    @Test
    public void createPerson() throws JSONException
    {
        Client client = this.dropwizardAppExtension.client();

        {
            Response response = client
                    .target("http://localhost:{port}/people/")
                    .resolveTemplate("port", this.dropwizardAppExtension.getLocalPort())
                    .request()
                    .post(Entity.entity(this.personDTO, MediaType.APPLICATION_JSON_TYPE));

            this.assertResponseStatus(response, Status.OK);
            String jsonResponse = response.readEntity(String.class);

            // <editor-fold desc="Expected JSON">
            // language=JSON
            String expected = """
                    {
                      "id"      : 1,
                      "fullName": "Full Name",
                      "jobTitle": "Job Title"
                    }
                    """;
            // </editor-fold>
            JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
        }

        {
            Response response = client
                    .target("http://localhost:{port}/people/")
                    .resolveTemplate("port", this.dropwizardAppExtension.getLocalPort())
                    .request()
                    .get();

            this.assertResponseStatus(response, Status.OK);
            String jsonResponse = response.readEntity(String.class);

            // <editor-fold desc="Expected JSON">
            // language=JSON
            String expected = """
                    [
                      {
                        "id"      : 1,
                        "fullName": "Full Name",
                        "jobTitle": "Job Title"
                      }
                    ]
                    """;
            // </editor-fold>
            JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
        }
    }

    private void assertResponseStatus(@Nonnull Response response, Status status)
    {
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        Assertions.assertThat(response.getStatusInfo().toEnum()).as(entityAsString).isEqualTo(status);
    }
}
