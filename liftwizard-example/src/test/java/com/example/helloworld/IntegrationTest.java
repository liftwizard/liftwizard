package com.example.helloworld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Person;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class IntegrationTest {

    private static final String TMP_FILE = createTempFile();
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.json5");

    @ClassRule
    public static final DropwizardAppRule<HelloWorldConfiguration> RULE = new DropwizardAppRule<>(
            HelloWorldApplication.class, CONFIG_PATH,
            ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE));

    @BeforeClass
    public static void migrateDb() throws Exception {
        RULE.getApplication().run("db", "migrate", CONFIG_PATH);
    }

    private static String createTempFile() {
        try {
            return File.createTempFile("test-example", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testHelloWorld() throws Exception {
        Response response = RULE
                .client()
                .target(String.format("http://localhost:%d/hello-world", RULE.getLocalPort()))
                .queryParam("name", "Dr. IntegrationTest")
                .request()
                .get();

            this.assertResponseStatus(response, Status.OK);

            String jsonResponse = response.readEntity(String.class);
            //language=JSON
            String expected = ""
                    + "{\n"
                    + "  \"id\"     : 1,\n"
                    + "  \"content\": \"Hello, Dr. IntegrationTest!\"\n"
                    + "}\n";
            JSONAssert.assertEquals(jsonResponse, expected, jsonResponse, JSONCompareMode.STRICT);
    }

    protected void assertResponseStatus(@Nonnull Response response, Status status)
    {
        response.bufferEntity();
        String entityAsString = response.readEntity(String.class);
        Assert.assertThat(entityAsString, response.getStatusInfo(), is(status));
    }

    @Test
    public void testPostPerson() throws Exception {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard");
        final Person newPerson = postPerson(person);
        assertThat(newPerson.getId()).isNotNull();
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @Test
    public void testRenderingPersonFreemarker() throws Exception {
        testRenderingPerson("view_freemarker");
    }

    @Test
    public void testRenderingPersonMustache() throws Exception {
        testRenderingPerson("view_mustache");
    }

    private void testRenderingPerson(String viewName) throws Exception {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard");
        final Person newPerson = postPerson(person);
        final String url = "http://localhost:" + RULE.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
        Response response = RULE.client().target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    private Person postPerson(Person person) {
        return RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/people")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Person.class);
    }

    @Test
    public void testLogFileWritten() throws IOException {
        // The log file is using a size and time based policy, which used to silently
        // fail (and not write to a log file). This test ensures not only that the
        // log file exists, but also contains the log line that jetty prints on startup
        final Path log = Paths.get("./logs/application.log");
        assertThat(log).exists();
        final String actual = new String(Files.readAllBytes(log), UTF_8);
        assertThat(actual).contains("0.0.0.0:" + RULE.getLocalPort());
    }
}
