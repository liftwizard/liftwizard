The `JerseyHttpLoggingBundle` is an alternative to Jersey's `LoggingFeature`. Jersey's `LoggingFeature` can be configured to log or not log bodies, but it cannot be configured to exclude headers. Since headers can include authentication tokens, you may not want to log headers, or only log those in an allow-list.

The bundle can be configured:

* include/exclude request bodies
* include/exclude response bodies
* allow-list of headers
* include/exclude the list of excluded header _names_
* the max body size before truncation

Through code, the bundle can be configured to log using different combinations of slf4j/log4j/logback with context in MDC or OpenTracing or a Map.
 
To turn it on, add `JerseyHttpLoggingBundle` to the list of registered bundles.
 
```java
@Override
public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
    bootstrap.addBundle(new EnvironmentConfigBundle());
 
    bootstrap.addBundle(new ObjectMapperBundle());
    bootstrap.addBundle(new ConfigLoggingBundle());

    StructuredArgumentsMDCLogger structuredLogger = new StructuredArgumentsMDCLogger(bootstrap.getObjectMapper());
    bootstrap.addBundle(new JerseyHttpLoggingBundle(structuredLogger));

    // ...
}
```

The bundle registers filters which gather all the arguments to log. A "logger" is passed into the constructor which abstracts over whether the logging uses logback or log4j, whether the structured arguments are converted into MDC, Markers, or a Map. The `StructuredArgumentsMDCLogger` in the example above logs using slf4j with context in MDC.
 
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

The Liftwizard logging modules add context to slf4j logging through MDC and through "structured logging". There are several bundles and servlet filters to choose from. You can use all of them or cherry-pick the ones that are useful to you.

In order to see the logging in action, we'll need to configure a log format that includes mdc and markers.

### test-example.json5
`src/test/resources/test-example.json5`
```json5
{
  "type"             : "console",
  "timeZone"         : "system",
  "logFormat"        : "%highlight(%-5level) %cyan(%date{HH:mm:ss}) [%white(%thread)] %blue(%marker) {%magenta(%mdc)} %green(%logger): %message%n%red(%rootException)",
  "includeCallerData": true
}
```

Next, lets turn on all the basic filters and see how they change what gets logged.

### HelloWorldApplication.java
`src/main/java/com/example/helloworld/HelloWorldApplication.java`
```java
@Override
public void run(HelloWorldConfiguration configuration, Environment environment) {
    // ...
    environment.getApplicationContext().addFilter(
            StructuredLoggingServletFilter.class,
            "/*",
            EnumSet.of(DispatcherType.REQUEST));
    environment.getApplicationContext().addFilter(
            new FilterHolder(new DurationStructuredLoggingFilter(configuration.getClockFactory().createClock())),
            "/*",
            EnumSet.of(DispatcherType.REQUEST));
 
    environment.jersey().register(CorrelationIdFilter.class);
    environment.jersey().register(ResourceInfoLoggingFilter.class);
    environment.jersey().register(StatusInfoStructuredLoggingFilter.class);
    // ... register other stuff, including jersey resources
}
```


## Logging output

With the logFormat and the filters in place, we can rerun `IntegrationTest` and see the new logs in action.

```console {title: "Logging output (newlines added for clarity)"}
INFO  15:03:56 [dw-22] { \
    liftwizard.response.http.statusCode=200, \
    liftwizard.response.http.statusEnum=OK, \
    liftwizard.response.http.statusPhrase=OK, \
    liftwizard.response.http.statusFamily=SUCCESSFUL, \
    liftwizard.response.http.entityType=com.example.helloworld.api.Saying, \
    liftwizard.time.startTime=1999-12-31T23:59:59.000000Z, \
    liftwizard.time.endTime=2000-01-01T00:00:00.000000Z, \
    liftwizard.time.duration.ms=1000, \
    liftwizard.time.duration.ns=1000000000 \
    liftwizard.time.duration.pretty=1.000000s, \
} \
{ \
    liftwizard.request.correlationId=82681aab-1a69-4fc0-b425-a5cbcbf98411, \
    liftwizard.request.httpMethod=GET, \
    liftwizard.request.httpPath=hello-world, \
    liftwizard.request.httpPathTemplate=/hello-world \
    liftwizard.request.parameter.query.name=Dr. IntegrationTest, \
    liftwizard.request.resourceClassName=com.example.helloworld.resources.HelloWorldResource, \
    liftwizard.request.resourceMethodName=sayHello, \
} com.liftwizard.servlet.logging.structured.argument.StructuredLoggingServletFilter: structured logging
```

Let's take a look at each filter individually.

## StructuredLoggingServletFilter

The `StructuredLoggingServletFilter` is a pre-requisite for structured logging. For the most part, you can pick and choose which dependencies you want to include, but `liftwizard-servlet-logging-structured-argument` is usually required.

`StructuredLoggingServletFilter` is a servlet filter that puts a "structured-logging" `LinkedHashMap` into the `ServletRequest` at the beginning of each request. Next, all other structured logging filters put their context into the same map. Finally, `StructuredLoggingServletFilter` logs the text `"structured logging"` together with all context at the end of each servlet request.

## CorrelationIdFilter

The `CorrelationIdFilter` gets the correlation id from a request header, adds it to the `ContainerResponseContext`, and adds it to MDC. If there is no correlation id, `CorrelationIdFilter` creates one first. The default header name and MDC key are both `"liftwizard.request.correlationId"`.

## ResourceInfoLoggingFilter

The `ResourceInfoLoggingFilter` gets information from the jax-rs `UriInfo` and adds it to MDC.

This includes:
* liftwizard.request.httpPath
* liftwizard.request.httpMethod
* liftwizard.request.resourceClassName
* liftwizard.request.resourceMethodName
* liftwizard.request.httpPathTemplate

In addition, for every query and path parameter, it adds a key of the form:
* liftwizard.request.parameter.query.<parameterName>
* liftwizard.request.parameter.path.<parameterName>

## StatusInfoStructuredLoggingFilter
The `StatusInfoStructuredLoggingFilter` gets information from the jax-rs `StatusType` and adds it to the structured argument map.

This includes:
* liftwizard.response.http.statusEnum
* liftwizard.response.http.statusCode
* liftwizard.response.http.statusFamily
* liftwizard.response.http.statusPhrase
* liftwizard.response.http.entityType

## DurationStructuredLoggingFilter
`DurationStructuredLoggingFilter` is a servlet filter that adds request/response timing information to the structured argument map.

This includes:
* liftwizard.time.endTime
* liftwizard.time.duration.pretty
* liftwizard.time.duration.ms
* liftwizard.time.duration.ns

## Sources of randomness

Since the `CorrelationIdFilter` may need to generate ids if none are passed in, it needs a factory of UUIDs.

Since the `DurationStructuredLoggingFilter` generates timing information, it needs a clock.

```java
@Override
public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
    bootstrap.addBundle(new EnvironmentConfigBundle());
 
    bootstrap.addBundle(new ClockBundle());
    bootstrap.addBundle(new UUIDBundle());
 
    // ...
    });
}
```

For more information on configuring sources of randomness, see the documentation for liftwizard-clock and liftwizard-uuid.

## Logstash encoder

`liftwizard-config-logging-logstash-file` is a Dropwizard `AppenderFactory`. It sets up a file appender that logs one json object per log statement. The json is formatted by [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) and is ready to be parsed by logstash.

Let's add the logstash-file appender to the list of configured appenders.

### test-example.json5
`src/test/resources/test-example.json5`
```json5
{
  // ...
  "logging": {
    "level": "INFO",
    "appenders": [
      {
        "type": "console",
        "logFormat": "%highlight(%-5level) %cyan(%date{HH:mm:ss}) [%white(%thread)] %green(%logger): %message <%blue(%marker)> <%magenta(%mdc)>%n%red(%rootException)",
        "timeZone": "UTC",
        "includeCallerData": true,
      },
      {
        "type": "file-logstash",
        "threshold": "ALL",
        "timeZone": "UTC",
        "includeCallerData": true,
        "currentLogFilename": "./logs/logstash.json",
        "archivedLogFilenamePattern": "./logs/logstash-%d.json",
        "encoder": {
          "includeContext": true,
          "includeMdc": true,
          "includeStructuredArguments": true,
          "includedNonStructuredArguments": false,
          "includeTags": true,
          "prettyPrint": true,
          "serializationInclusion": "NON_ABSENT"
        }
      }
    ]
  },
  // ...
}
```

### logstash.json
`logs/logstash.json` snippet
```json
{
  "@timestamp": "1999-12-31T23:59:59.000-00:00",
  "@version": "1",
  "message": "structured logging",
  "logger_name": "com.liftwizard.servlet.logging.structured.argument.StructuredLoggingServletFilter",
  "thread_name": "dw-22",
  "level": "INFO",
  "level_value": 20000,
  "liftwizard.request.resourceMethodName": "sayHello",
  "liftwizard.request.parameter.query.name": "Dr. IntegrationTest",
  "liftwizard.request.resourceClassName": "com.example.helloworld.resources.HelloWorldResource",
  "liftwizard.request.httpPath": "hello-world",
  "liftwizard.request.correlationId": "82681aab-1a69-4fc0-b425-a5cbcbf98411",
  "liftwizard.request.httpMethod": "GET",
  "liftwizard.request.httpPathTemplate": "/hello-world",
  "liftwizard.time.startTime": "1999-12-31T23:59:59.000000Z",
  "liftwizard.response.http.statusEnum": "OK",
  "liftwizard.response.http.statusCode": 200,
  "liftwizard.response.http.statusFamily": "SUCCESSFUL",
  "liftwizard.response.http.statusPhrase": "OK",
  "liftwizard.response.http.entityType": "com.example.helloworld.api.Saying",
  "liftwizard.time.endTime": "2000-01-01T00:00:00.000000Z",
  "liftwizard.time.duration.pretty": "1.000000s",
  "liftwizard.time.duration.ms": 1000,
  "liftwizard.time.duration.ns": 1000000000,
  "caller_class_name": "com.liftwizard.servlet.logging.structured.argument.StructuredLoggingServletFilter",
  "caller_method_name": "log",
  "caller_file_name": "StructuredLoggingServletFilter.java",
  "caller_line_number": 86
}
```

`StructuredLoggingServletFilter`, `CorrelationIdFilter`, `ResourceInfoLoggingFilter`, `StatusInfoStructuredLoggingFilter`, and `DurationStructuredLoggingFilter` live in the following modules.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-servlet-logging-structured-argument</artifactId>
</dependency>

<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-servlet-logging-correlation-id</artifactId>
</dependency>

<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-servlet-logging-resource-info</artifactId>
</dependency>
```
