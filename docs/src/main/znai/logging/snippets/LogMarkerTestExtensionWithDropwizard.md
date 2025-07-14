`LogMarkerTestExtension` was used as an annotation in the previous example. When used together with `DropwizardAppExtension`, it ought to be a field to control execution order. Both extensions tear down logging, and `LogMarkerTestExtension` needs to perform its tear down first, to flush its contents to the console.

```java
@RegisterExtension
final DropwizardAppExtension<MyAppConfiguration> dropwizardAppExtension = new DropwizardAppExtension<>(
        MyApplication.class,
        ResourceHelpers.resourceFilePath("test-example.json5"));

@RegisterExtension
final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();
```
