`LogMarkerTestExtension` is a JUnit 5 `Extension` that clears the buffer before all tests and flushes the buffer after failed tests. It does this by logging `CLEAR` and `FLUSH` markers.

```java
@RegisterExtension
private final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();
```

`LogMarkerTestExtension` lives in the `liftwizard-junit-extension-log-marker` module.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-junit-extension-log-marker</artifactId>
    <scope>test</scope>
</dependency>
```
