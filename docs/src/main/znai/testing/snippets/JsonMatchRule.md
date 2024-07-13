`JsonMatchRule` works well with Dropwizard's [`DropwizardAppRule`](https://www.dropwizard.io/en/release-2.1.x/manual/testing.html#junit-4).

```java
public class ExampleTest
{
    @Rule
    private final DropwizardAppRule<HelloWorldConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
            ExampleApplication.class,
            ResourceHelpers.resourceFilePath("config-test.json5"));

    @Rule
    public final JsonMatchRule jsonMatchRule = new JsonMatchRule(this.getClass());

    @Test
    public void smokeTest()
    {
        Response actualResponse = this.dropwizardAppRule
                .client()
                .target("http://localhost:{port}/api/example")
                .resolveTemplate("port", this.dropwizardAppRule.getLocalPort())
                .request()
                .get();

        String actualJsonResponse = actualResponse.readEntity(String.class);
        String expectedResponseClassPathLocation = this.getClass().getSimpleName() + "." + testName + ".json";
        this.jsonMatchRule.assertFileContents(expectedResponseClassPathLocation, actualJsonResponse);
    }
}
```

