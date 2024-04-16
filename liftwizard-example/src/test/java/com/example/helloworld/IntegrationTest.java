package com.example.helloworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.dto.PersonDTO;
import io.dropwizard.testing.ResourceHelpers;
import io.liftwizard.junit.extension.app.LiftwizardAppExtension;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import io.liftwizard.reladomo.test.extension.ReladomoInitializeExtension;
import io.liftwizard.reladomo.test.extension.ReladomoLoadDataExtension;
import io.liftwizard.reladomo.test.extension.ReladomoPurgeAllExtension;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class IntegrationTest {
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
    @Order(6)
    private final LogMarkerTestExtension    logMarkerExtension = new LogMarkerTestExtension();

    @Test
    public void testHelloWorld() throws Exception {
        Response response = this.dropwizardAppExtension
                .client()
                .target(String.format("http://localhost:%d/hello-world", this.dropwizardAppExtension.getLocalPort()))
                .queryParam("name", "Dr. IntegrationTest")
                .request()
                .get();

            this.assertResponseStatus(response, Status.OK);

            String jsonResponse = response.readEntity(String.class);
            // language=JSON
            String expected = """
                    {
                      "id"     : 1,
                      "content": "Hello, Dr. IntegrationTest!"
                    }
                    """;
            JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
    }

    protected void assertResponseStatus(@Nonnull Response response, Status status)
    {
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        assertThat(response.getStatusInfo().toEnum()).as(entityAsString).isEqualTo(status);
    }

    @Test
    public void validDateParameter() {
        String date = this.dropwizardAppExtension
                .client().target("http://localhost:" + this.dropwizardAppExtension.getLocalPort() + "/hello-world/date")
                .queryParam("date", "2022-01-20")
                .request()
                .get(String.class);
        assertThat(date).isEqualTo("2022-01-20T00:00:00.000Z");
    }

    @Test
    public void invalidDateParameter() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> this.dropwizardAppExtension
                        .client().target("http://localhost:" + this.dropwizardAppExtension.getLocalPort() + "/hello-world/date")
                        .queryParam("date", "abc")
                        .request()
                        .get(String.class));
    }

    @Test
    public void noDateParameter() {
        String date = this.dropwizardAppExtension
                .client().target("http://localhost:" + this.dropwizardAppExtension.getLocalPort() + "/hello-world/date")
                .request()
                .get(String.class);
        assertThat(date).isEmpty();
    }

    @Test
    public void testPostPerson() {
        PersonDTO person = new PersonDTO("Dr. IntegrationTest", "Chief Wizard");
        PersonDTO newPerson = this.postPerson(person);
        assertThat(newPerson.getId()).isNotNull();
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @Test
    public void testRenderingPersonFreemarker() throws Exception {
        this.testRenderingPerson("view_freemarker");
    }

    @Test
    public void testRenderingPersonMustache() throws Exception {
        this.testRenderingPerson("view_mustache");
    }

    private void testRenderingPerson(String viewName)
    {
        PersonDTO person    = new PersonDTO( "Dr. IntegrationTest", "Chief Wizard");
        PersonDTO    newPerson = this.postPerson(person);
        String url = "http://localhost:" + this.dropwizardAppExtension.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
        Response response = this.dropwizardAppExtension.client().target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200.getStatusCode());
    }

    private PersonDTO postPerson(PersonDTO person) {
        return this.dropwizardAppExtension
                .client().target("http://localhost:" + this.dropwizardAppExtension.getLocalPort() + "/people")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(PersonDTO.class);
    }

    @Test
    public void testLogFileWritten() throws IOException {
        // The log file is using a size and time based policy, which used to silently
        // fail (and not write to a log file). This test ensures not only that the
        // log file exists, but also contains the log line that jetty prints on startup
        Path log = Paths.get("./logs/application.log");
        assertThat(log).exists();
        String actual = Files.readString(log);
        assertThat(actual).contains("0.0.0.0:" + this.dropwizardAppExtension.getLocalPort());
    }
}
