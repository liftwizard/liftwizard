package com.example.helloworld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Person;
import com.example.helloworld.dto.PersonDTO;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoInitializeTestRule;
import io.liftwizard.reladomo.test.rule.ReladomoPurgeAllTestRule;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.CoreMatchers.is;

@ExtendWith(DropwizardExtensionsSupport.class)
class IntegrationTest {
    private static final String CONFIG = "test-example.json5";

    @TempDir
    static Path tempDir;
    static Supplier<String> CURRENT_LOG = wrap(() -> tempDir.resolve("application.log").toString());


    static Supplier<String> ARCHIVED_LOG = wrap(() -> tempDir.resolve("application-%d-%i.log.gz").toString());

    static final DropwizardAppExtension<HelloWorldConfiguration> APP = new DropwizardAppExtension<>(
            HelloWorldApplication.class,
            CONFIG,
            new ResourceConfigurationSourceProvider(),
            config("logging.appenders[1].currentLogFilename", CURRENT_LOG),
            config("logging.appenders[1].archivedLogFilenamePattern", ARCHIVED_LOG)
    );

    private final ReladomoInitializeTestRule initializeTestRule =
            new ReladomoInitializeTestRule("reladomo-runtime-configuration/ReladomoRuntimeConfiguration.xml");

    private final ReladomoPurgeAllTestRule purgeAllTestRule = new ReladomoPurgeAllTestRule();

    private final TestRule logMarkerTestRule = new LogMarkerTestRule();

    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(this.initializeTestRule)
            .around(this.purgeAllTestRule)
            .around(this.logMarkerTestRule);

    @BeforeAll
    public static void migrateDb() throws Exception {
        APP.getApplication().run("db", "migrate", resourceFilePath(CONFIG));
    }

    private static Supplier<String> wrap(Supplier<String> supplier)
    {
        return () -> {
            String result = supplier.get();
            System.out.println(result);
            return result;
        };
    }

    @Test
    void testHelloWorld() {
        Response response = APP
                .client()
                .target(String.format("http://localhost:%d/hello-world", APP.getLocalPort()))
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


    @Nested
    class DateParameterTests {
        @Test
        void validDateParameter() {
            final String date = APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world/date")
                    .queryParam("date", "2022-01-20")
                    .request()
                    .get(String.class);
            assertThat(date).isEqualTo("2022-01-20");
        }

        @ParameterizedTest
        @ValueSource(strings = {"null", "abc", "0"})
        void invalidDateParameter(String value) {
            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world/date")
                            .queryParam("date", value)
                            .request()
                            .get(String.class));
        }

        @Test
        void noDateParameter() {
            final String date = APP.client().target("http://localhost:" + APP.getLocalPort() + "/hello-world/date")
                    .request()
                    .get(String.class);
            assertThat(date).isEmpty();
        }
    }

    @Test
    void testPostPerson() {
        final Person person = new Person("Dr. IntegrationTest", "Chief Wizard", 1525);
        final Person newPerson = postPerson(person);
        assertThat(newPerson.getId()).isNotNull();
        assertThat(newPerson.getFullName()).isEqualTo(person.getFullName());
        assertThat(newPerson.getJobTitle()).isEqualTo(person.getJobTitle());
    }

    @ParameterizedTest
    @ValueSource(strings={"view_freemarker", "view_mustache"})
    void testRenderingPerson(String viewName) {
        final PersonDTO person = new PersonDTO("Dr. IntegrationTest", "Chief Wizard", 1525);
        final PersonDTO newPerson = postPerson(person);
        final String url = "http://localhost:" + APP.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
        Response response = APP.client().target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
    }

    private PersonDTO postPerson(PersonDTO person) {
        return APP.client().target("http://localhost:" + APP.getLocalPort() + "/people")
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
