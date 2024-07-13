Liftwizard includes utilities for asserting that a JSON string equals the contents of a file, using JSON equality semantics. Liftwizard delegates to [JSONassert](https://github.com/skyscreamer/JSONassert) for JSON comparison.

The API is similar to the [file matching API](./matching-files), and re-record mode is enabled with the same environment variable `LIFTWIZARD_FILE_MATCH_RULE_RERECORD`.

The setup is different for the JUnit 4 Rule and JUnit 5 Extension. After setup, both have the same API.

```java
this.jsonMatchExtension.assertFileContents(expectedJsonClassPathLocation, actualJson);
```

If the file does not exist, or the contents do not match, an assertion error is added to an [ErrorCollector](https://junit.org/junit4/javadoc/4.12/org/junit/rules/ErrorCollector.html). If the ErrorCollector contains any errors, the test fails at the end with all expected/actual pairs reported together.

If `LIFTWIZARD_FILE_MATCH_RULE_RERECORD` is set to `true`, `assertJsonContents` will not emit any `AssertionErrors`.

```tabs
"JUnit 4": :include-markdown: snippets/JsonMatchRule.md
"JUnit 5": :include-markdown: snippets/JsonMatchExtension.md
```

