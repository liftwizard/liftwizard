The `JerseyHttpLoggingBundle` logs all requests and responses to slf4j. The `verbosity` and `maxEntitySize` are configurable.
 
To turn it on, add `JerseyHttpLoggingBundle` to the list of registered bundles.
 
```java
@Override
public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
    bootstrap.addBundle(new EnvironmentConfigBundle());
 
    bootstrap.addBundle(new ObjectMapperBundle());
    bootstrap.addBundle(new ConfigLoggingBundle());
 
    bootstrap.addBundle(new JerseyHttpLoggingBundle());
 
    // ...
}
```
 
If `ObjectMapperBundle` is also registered, the json bodies will be pretty-printed.
 
```
INFO  21:01:18 [dw-26 - POST /people]  {} com.liftwizard.dropwizard.bundle.httplogging.JerseyHttpLoggingBundle: 1 * Server has received a request on thread dw-26 - POST /people
1 > POST http://localhost:59980/people
1 > Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2
1 > Connection: keep-alive
1 > Content-Length: 83
1 > Content-Type: application/json
1 > Host: localhost:59980
1 > User-Agent: Jersey/2.25.1 (HttpUrlConnection 11.0.7)
{
  "id" : 0,
  "fullName" : "Dr. IntegrationTest",
  "jobTitle" : "Chief Wizard"
}
 
INFO  21:01:18 [dw-26 - POST /people]  {liftwizard.request.resourceMethodName=createPerson, liftwizard.request.resourceClassName=com.example.helloworld.resources.PeopleResource, liftwizard.request.httpPath=people, liftwizard.request.correlationId=4bb909d0-4c29-3f81-957f-aab6d7f73c9f, liftwizard.request.httpMethod=POST, liftwizard.request.httpPathTemplate=/people} com.liftwizard.dropwizard.bundle.httplogging.JerseyHttpLoggingBundle: 1 * Server responded with a response on thread dw-26 - POST /people
1 < 200
1 < Content-Type: application/json
1 < liftwizard.request.correlationId: 4bb909d0-4c29-3f81-957f-aab6d7f73c9f
{
  "id" : 1,
  "fullName" : "Dr. IntegrationTest",
  "jobTitle" : "Chief Wizard"
}
```

`JerseyHttpLoggingBundle` lives in the `liftwizard-bundle-logging-http` module.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-bundle-logging-http</artifactId>
</dependency>
```
