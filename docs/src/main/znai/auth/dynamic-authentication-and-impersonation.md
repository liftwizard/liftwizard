Liftwizard supports the dynamic configuration of [dropwizard-auth](https://www.dropwizard.io/en/stable/manual/auth.html) which enables using different authorization methods in production and tests, without adding conditionals - without adding any code at all. For example, we can configure impersonation authorization in tests with the following config.

```json5
{
  authFilters: [
    {
      type: "header",
      header: "Authorization",
      prefix: "Impersonation",
    },
  ],
}
```

## Setup

To get started, add a dependency on `liftwizard-bundle-auth-filter` and add `AuthFilterBundle` to the registered bundles.

Modify the application's configuration class to implement `AuthFilterFactoryProvider` and add a dependency on `liftwizard-config-auth-filter` if it does not already extend `AbstractLiftwizardConfiguration`.

## Test configuration

For tests, you'll typically want to use header-based impersonation.

Add a dependency on `liftwizard-config-auth-filter-header`.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-config-auth-filter-header</artifactId>
    <scope>test</scope>
</dependency>
```

Add an `authFilters` list to `config-test.json5` containing just one filter, of type `header`.

```json5
{
  authFilters: [
    {
      type: "header",
      header: "Authorization",
      prefix: "Impersonation",
    },
  ],
}
```

## Test code

Impersonation authorization works well in tests that uses Dropwizard test utilities, like `DropwizardAppExtension`, `LiftwizardAppExtension`, or `DropwizardAppRule`. There is no change to test setup code, only to the test configuration file. The test code will include headers on client requests, like this.

```java
@Test
void smokeTest()
{
    Client client = this.appExtension.client();

    Response response = client
            .target("http://localhost:{port}/api/example")
            .resolveTemplate("port", this.appExtension.getLocalPort())
            .request()
            .header("Authorization", "Impersonation User ID")
            .get();

    // add assertions here
}
```

Whenever we use `dropwizard-auth`, some of our Jersey resource methods will be authenticated. The authenticated methods will be annotated with a security annotation such as `@PermitAll`. The user principal will be passed in as a parameter and annotated like `@Auth Principal principal`. The header authorizer will take the string passed in the header (`"Impersonation User ID"` in this example), remove its prefix (`"User ID"`), and make that string accessible via `principal.getName()`.

Since the header is sent on each request, we can write tests involving multiple users. For example, we can write a test that asserts:

- `User 1` can create an entry, and gets HTTP 201 Created.
- `User 2` cannot edit or delete the entry, and gets HTTP 403 Forbidden.
- `User 1` can edit or delete the entry, and gets HTTP 200 OK.

## Production configuration

The production authentication filter dependencies and configuration will depend on the method of authentication used in production. For example, the configuration to use Firebase for auth would look like this.

```json5
{
  authFilters: [
    {
      type: "firebase",
      databaseUrl: "https://example.firebaseio.com",
      firebaseConfig: "${FIREBASE_CONFIG}",
    },
  ],
}
```

