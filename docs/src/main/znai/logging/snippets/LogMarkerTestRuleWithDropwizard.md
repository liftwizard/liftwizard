`LogMarkerTestRule` needs to be an inner rule when used together with DropwizardAppRule. Both rules tear down logging, and `LogMarkerTestRule` needs to perform its tear down first, to flush its contents to the console.

```java
private final TestRule logMarkerTestRule = new LogMarkerTestRule();

private final DropwizardAppRule<MyAppConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
        MyApplication.class,
        ResourceHelpers.resourceFilePath("test-example.json5"));

@Rule
public final RuleChain ruleChain = RuleChain
        .outerRule(this.dropwizardAppRule)
        .around(this.logMarkerTestRule);
```

