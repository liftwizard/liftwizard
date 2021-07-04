In unit tests, it can be useful to suppress all logging for successful tests, but still log everything when tests fail.

In order to accomplish this, we need to buffer all logging before we know the result of the test, and then flush or clear the buffer once we know the outcome.

## BufferedAppender

`BufferedAppender` is the logback appender that buffers all logging until it receives a `CLEAR` or `FLUSH` marker.

You can use  directly in logback configuration. It requires a delegate appender for flushing, declared using an `appender-ref`.

:include-xml: logback-test.xml {title: "logback-test.xml"}

`BufferedAppender` lives in the `liftwizard-logging-buffered-appender` module.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-logging-buffered-appender</artifactId>
    <scope>test</scope>
</dependency>
```

## LogMarkerTestRule

`LogMarkerTestRule` is a JUnit `Rule` that clears the buffer before all tests and flushes the buffer after failed tests. It does this by logging `CLEAR` and `FLUSH` markers.

```java
@Rule
public final TestRule logMarkerTestRule = new LogMarkerTestRule();
```

`LogMarkerTestRule` lives in the `liftwizard-junit-rule-log-marker` module.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-junit-rule-log-marker</artifactId>
    <scope>test</scope>
</dependency>
```

## BufferedAppenderFactory

The `BufferedAppenderFactory` allows you to use an appender with the type `buffered` where you would otherwise use `console` in your Dropwizard configuration.

```json5
  logging: {
    level: "INFO",
    appenders: [
      {
        type: "buffered",
        timeZone: "system",
        logFormat: "%highlight(%-5level) %cyan(%date{HH:mm:ss}) [%white(%thread)] %blue(%marker) {%magenta(%mdc)} %green(%logger): %message%n%red(%rootException)",
        includeCallerData: true,
      }
    ]
  }
```

 This is primarily useful for tests that use `DropwizardAppRule`.
 
```java
@Rule
public final TestRule logMarkerTestRule = new LogMarkerTestRule();

@Rule
public final DropwizardAppRule<HelloWorldConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
        HelloWorldApplication.class,
        ResourceHelpers.resourceFilePath("test-example.json5"));
```

`BufferedAppenderFactory` lives in the `liftwizard-config-logging-buffered` module.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-config-logging-buffered</artifactId>
    <scope>test</scope>
</dependency>
```
