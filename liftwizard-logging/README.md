# Logging modules

The Liftwizard logging modules add context to slf4j logging through MDC and through "structured logging". There are several bundles and servlet filters to choose from. You can use all of them or cherry-pick the ones that are useful to you.

In order to see the logging in action, we'll need to configure a log format that includes mdc and markers.

### `src/test/resources/test-example.json5`
```json5
{
  "type"             : "console",
  "timeZone"         : "system",
"logFormat"        : "%highlight(%-5level) %cyan(%date{HH:mm:ss}) [%white(%thread)] %blue(%marker) {%magenta(%mdc)} %green(%logger): %message%n%red(%rootException)",
"includeCallerData": true
}
```

Next, lets turn on all the basic filters and see how they change what gets logged.

### `src/main/java/com/example/helloworld/HelloWorldApplication.java`
```java
@Override
public void run(HelloWorldConfiguration configuration, Environment environment) {
    // ...
    environment.getApplicationContext().addFilter(
            StructuredArgumentLoggingFilter.class,
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

```
INFO  15:03:56 [dw-22] {liftwizard.time.startTime=2020-05-06T19:03:56.967412Z, liftwizard.response.http.statusEnum=OK, liftwizard.response.http.statusCode=200, liftwizard.response.http.statusFamily=SUCCESSFUL, liftwizard.response.http.statusPhrase=OK, liftwizard.response.http.entityType=com.example.helloworld.api.Saying, liftwizard.time.endTime=2020-05-06T19:03:56.984585Z, liftwizard.time.duration.pretty=0.017173s, liftwizard.time.duration.ms=17, liftwizard.time.duration.ns=17173000} {liftwizard.request.resourceMethodName=sayHello, liftwizard.request.parameter.query.name=Dr. IntegrationTest, liftwizard.request.resourceClassName=com.example.helloworld.resources.HelloWorldResource, liftwizard.request.httpPath=hello-world, liftwizard.request.correlationId=82681aab-1a69-4fc0-b425-a5cbcbf98411, liftwizard.request.httpMethod=GET, liftwizard.request.httpPathTemplate=/hello-world} com.liftwizard.servlet.logging.structured.argument.StructuredArgumentLoggingFilter: structured logging
```

This can be hard to see in a single line but it includes:
* liftwizard.request.correlationId=82681aab-1a69-4fc0-b425-a5cbcbf98411
* liftwizard.request.httpMethod=GET
* liftwizard.request.httpPath=hello-world
* liftwizard.request.httpPathTemplate=/hello-world
* liftwizard.request.parameter.query.name=Dr. IntegrationTest
* liftwizard.request.resourceClassName=com.example.helloworld.resources.HelloWorldResource
* liftwizard.request.resourceMethodName=sayHello
* liftwizard.response.http.entityType=com.example.helloworld.api.Saying
* liftwizard.response.http.statusCode=200
* liftwizard.response.http.statusEnum=OK
* liftwizard.response.http.statusFamily=SUCCESSFUL
* liftwizard.response.http.statusPhrase=OK
* liftwizard.time.duration.ms=17
* liftwizard.time.duration.ns=17173000
* liftwizard.time.duration.pretty=0.017173s
* liftwizard.time.endTime=2020-05-06T19:03:56.984585Z
* liftwizard.time.startTime=2020-05-06T19:03:56.967412Z

Let's take a look at each filter individually.

## StructuredArgumentLoggingFilter

The `StructuredArgumentLoggingFilter` is a pre-requisite for structured logging. For the most part, you can pick and choose which dependencies you want to include, but `liftwizard-servlet-logging-structured-argument` is usually required.
 
`StructuredArgumentLoggingFilter` is a servlet filter that puts a "structured-logging" `LinkedHashMap` into the `ServletRequest` at the beginning of each request. Next, all other structured logging filters put their context into the same map. Finally, `StructuredArgumentLoggingFilter` logs the text `"structured logging"` together with all context at the end of each servlet request.

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

### `src/test/resources/test-example.json5`
```json5
  logging: {
    level: "INFO",
    appenders: [
      {
        "type"             : "console",
        "timeZone"         : "system",
        "logFormat"        : "%highlight(%-5level) %cyan(%date{HH:mm:ss}) [%white(%thread)] %blue(%marker) {%magenta(%mdc)} %green(%logger): %message%n%red(%rootException)",
        "includeCallerData": true
      },
      {
        "type"                      : "logstash-file",
        "currentLogFilename"        : "./logs/logstash.json",
        "archivedLogFilenamePattern": "./logs/logstash-%d.json",
        "includeCallerData"         : true
      }
    ]
  }
```

### `logs/logstash.json` snippet
```json
{
  "@timestamp": "2020-05-06T15:03:56.984-04:00",
  "@version": "1",
  "message": "structured logging",
  "logger_name": "com.liftwizard.servlet.logging.structured.argument.StructuredArgumentLoggingFilter",
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
  "liftwizard.time.startTime": "2020-05-06T19:03:56.967412Z",
  "liftwizard.response.http.statusEnum": "OK",
  "liftwizard.response.http.statusCode": 200,
  "liftwizard.response.http.statusFamily": "SUCCESSFUL",
  "liftwizard.response.http.statusPhrase": "OK",
  "liftwizard.response.http.entityType": "com.example.helloworld.api.Saying",
  "liftwizard.time.endTime": "2020-05-06T19:03:56.984585Z",
  "liftwizard.time.duration.pretty": "0.017173s",
  "liftwizard.time.duration.ms": 17,
  "liftwizard.time.duration.ns": 17173000,
  "caller_class_name": "com.liftwizard.servlet.logging.structured.argument.StructuredArgumentLoggingFilter",
  "caller_method_name": "log",
  "caller_file_name": "StructuredArgumentLoggingFilter.java",
  "caller_line_number": 86
}
```
