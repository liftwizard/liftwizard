`JsonMatchExtension` works well with Dropwizard's [`DropwizardAppExtension`](https://www.dropwizard.io/en/stable/manual/testing.html#junit-5) or Liftwizard's `LiftwizardAppExtension`.

```java
public class ExampleTest
{
    @RegisterExtension
    private final LiftwizardAppExtension<?> appExtension = this.getLiftwizardAppExtension();

    @RegisterExtension
    private final JsonMatchExtension jsonMatchExtension = new JsonMatchExtension(this.getClass());

    @Nonnull
    @Override
    private LiftwizardAppExtension<?> getLiftwizardAppExtension()
    {
        return new LiftwizardAppExtension<>(
                ExampleApplication.class,
                ResourceHelpers.resourceFilePath("config-test.json5"));
    }
    
    @Test
    public void smokeTest()
    {
        Response actualResponse = this.appExtension
                .client()
                .target("http://localhost:{port}/api/example")
                .resolveTemplate("port", this.appExtension.getLocalPort())
                .request()
                .get();

        String actualJsonResponse = actualResponse.readEntity(String.class);
        String expectedResponseClassPathLocation = this.getClass().getSimpleName() + "." + testName + ".json";
        this.jsonMatchExtension.assertFileContents(expectedResponseClassPathLocation, actualJsonResponse);
    }
}
```

