package com.example.helloworld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.dto.PersonDTO;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.liftwizard.reladomo.test.rule.ReladomoInitializeTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoPurgeAllTestRule;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class IntegrationTest {
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.json5");

    private final DropwizardAppRule<HelloWorldConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
            HelloWorldApplication.class,
            CONFIG_PATH);

    private final ExternalResource dbMigrateRule = new ExternalResource()
    {
        @Override
        protected void before() throws Throwable
        {
            IntegrationTest.this.dropwizardAppRule.getApplication().run("db", "migrate", CONFIG_PATH);
        }
    };

    private final ReladomoInitializeTestRule initializeTestRule =
            new ReladomoInitializeTestRule("reladomo-runtime-configuration/ReladomoRuntimeConfiguration.xml");


    private final ReladomoPurgeAllTestRule purgeAllTestRule = new ReladomoPurgeAllTestRule();

    @Rule
    public final RuleChain ruleChain = RuleChain.emptyRuleChain()
            .around(this.dropwizardAppRule)
            .around(this.dbMigrateRule)
            .around(this.initializeTestRule)
            .around(this.purgeAllTestRule);

    @Test
    public void testHelloWorld() throws Exception {
        Response response = this.dropwizardAppRule
                .client()
                .target(String.format("http://localhost:%d/hello-world", dropwizardAppRule.getLocalPort()))
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
        final PersonDTO person = new PersonDTO("Dr. IntegrationTest", "Chief Wizard");
        final PersonDTO newPerson = postPerson(person);
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
        final PersonDTO person    = new PersonDTO( "Dr. IntegrationTest", "Chief Wizard");
        final PersonDTO    newPerson = postPerson(person);
        final String url = "http://localhost:" + dropwizardAppRule.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
        Response response = dropwizardAppRule.client().target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    private PersonDTO postPerson(PersonDTO person) {
        return dropwizardAppRule.client().target("http://localhost:" + dropwizardAppRule.getLocalPort() + "/people")
                .request()
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(PersonDTO.class);
    }

    @Test
    public void testLogFileWritten() throws IOException {
        // The log file is using a size and time based policy, which used to silently
        // fail (and not write to a log file). This test ensures not only that the
        // log file exists, but also contains the log line that jetty prints on startup
        final Path log = Paths.get("./logs/application.log");
        assertThat(log).exists();
        final String actual = new String(Files.readAllBytes(log), UTF_8);
        assertThat(actual).contains("0.0.0.0:" + dropwizardAppRule.getLocalPort());
    }
}
