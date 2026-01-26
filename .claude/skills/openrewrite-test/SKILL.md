---
name: openrewrite-test
description: This skill should be used when writing or modifying tests for OpenRewrite recipes in the Liftwizard project, when the user asks to "write recipe tests", "add test coverage", "test OpenRewrite recipe", "fix failing recipe test", or discusses test structure patterns for OpenRewrite. Provides guidance on test method naming, combining test scenarios, and coverage best practices.
---

# OpenRewrite Recipe Test Development

Guidance for writing effective tests for OpenRewrite recipes in the Liftwizard project.

## Test Structure Requirements

**Every test class must have exactly 2 test methods:**

1. **Positive test method** (with `@DocumentExample`):
    - Name: `replacePatterns`
    - one `java(before, after)` call with one before/after pair
    - All transformation scenarios combined into a single code example
    - Shows the complete range of what the recipe can handle

2. **Negative test method**:
    - Name: `doNotReplaceInvalidPatterns`
    - one `java(code)` call with one code example
    - All non-transformation scenarios combined into a single code example
    - No expected changes in output

## Single java() Call Per Test

**Each test method should have exactly one `java()` call.**

❌ **WRONG** - Multiple java() pairs:

```java
@Test
void replacePatterns() {
    this.rewriteRun(
        java(before1, after1),
        java(before2, after2),
        java(before3, after3)
    );
}
```

✅ **CORRECT** - Single java() with one full example:

```java
@Test
void replacePatterns() {
    this.rewriteRun(
        java(
            """
            // one example containing all test scenarios
            """,
            """
            // one after example showing all transformations
            """
        )
    );
}
```

## Test Method Naming

**Use identical method names across all test classes:**

- `replacePatterns` - Positive test (always with `@DocumentExample`)
- `doNotReplaceInvalidPatterns` - Negative test

**Avoid these patterns:**

- `testReplaceNullSafeEquals` (includes "test" prefix)
- `replaceAssertThrowsTest` (includes "test" suffix)
- `replaceNullSafeEqualsPatterns` (too specific to recipe)
- `replaceHashMapConstructorVariations` (too specific to recipe)

Consistency over specificity - all test classes use the same two method names.

## Combining Test Scenarios

**Combine ALL related scenarios into one code example.**

Instead of separate java() calls or methods:

```java
// WRONG - separate methods
@Test void replaceWithMessage() { ... }
@Test void replaceWithoutMessage() { ... }

// ALSO WRONG - multiple java() pairs
this.rewriteRun(
    java(withMessage, withMessageExpected),
    java(withoutMessage, withoutMessageExpected)
);
```

Combine into one example:

```java
@Test
@DocumentExample
void replacePatterns() {
    this.rewriteRun(
        java(
            """
            import ...;

            class Test {
                // All variations in one class:
                void withMessage() { Verify.assertSize("msg", 1, list); }
                void withoutMessage() { Verify.assertSize(1, list); }
                void withZero() { Verify.assertSize(0, list); }
                void withVariable() { Verify.assertSize(n, list); }
            }
            """,
            """
            import ...;

            class Test {
                // All expected outputs in one class:
                void withMessage() { assertThat(list).as("msg").hasSize(1); }
                void withoutMessage() { assertThat(list).hasSize(1); }
                void withZero() { assertThat(list).isEmpty(); }
                void withVariable() { assertThat(list).hasSize(n); }
            }
            """
        )
    );
}
```

**What to include in the single example:**

- Different parameter combinations (with/without message, with/without initialCapacity)
- Different data types (String, Integer, List)
- Different syntax forms (diamond operator, explicit generics, raw types)
- Different contexts (fields, local variables, return statements)
- Edge cases like zero values or empty collections
- FieldAccess expressions that should be ignored

## Complete Example Test Pattern

```java
class RecipeNameTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new RecipeName())
            .parser(JavaParser.fromJavaVersion());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
            java(
                """
                import org.example.OldApi;
                import java.util.Map;

                class Test<T> {
                    // Field declarations
                    private final List<String> fieldEmpty = new OldList<>();
                    private final List<Integer> fieldCapacity = new OldList<>(10);

                    void testMethod() {
                        // Local variables - various generic forms
                        List<String> diamond = new OldList<>();
                        List rawType = new OldList();
                        List<Map<String, Integer>> nested = new OldList<>();
                        List<? extends Number> wildcard = new OldList<>();

                        // Edge cases
                        List<String> explicit = new OldList<String>();
                    }

                    // Return statement context
                    List<T> factory() {
                        return new OldList<>();
                    }
                }
                """,
                """
                import org.example.NewApi;
                import java.util.Map;

                class Test<T> {
                    // Field declarations
                    private final List<String> fieldEmpty = NewApi.empty();
                    private final List<Integer> fieldCapacity = NewApi.withCapacity(10);

                    void testMethod() {
                        // Local variables - various generic forms
                        List<String> diamond = NewApi.empty();
                        List rawType = NewApi.empty();
                        List<Map<String, Integer>> nested = NewApi.empty();
                        List<? extends Number> wildcard = NewApi.empty();

                        // Edge cases
                        List<String> explicit = NewApi.<String>empty();
                    }

                    // Return statement context
                    List<T> factory() {
                        return NewApi.empty();
                    }
                }
                """
            )
        );
    }

    @Test
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
            java(
                """
                import org.example.OldList;
                import java.util.Collections;

                class Test {
                    // Concrete type - should NOT transform
                    private final OldList<String> concreteField = new OldList<>();

                    void testMethod() {
                        OldList<String> concreteLocal = new OldList<>();
                    }

                    // FieldAccess expressions - should not crash
                    private static final Object EMPTY = Collections.EMPTY_SET;
                }
                """
            )
        );
    }
}
```

## Test Coverage Best Practices

- Add coverage for bounded wildcards and unknown types
- Test both positive patterns (should transform) and negative patterns (should not transform)
- Add test coverage for field initialization and constructor initialization, not just local variables
- Look at the original source code to find method overloads, and test all overloads
- Include FieldAccess expressions in negative tests to ensure no crashes

## YAML Recipe Integration Tests

Create one integration test class per YAML recipe file to ensure all recipes work together without interference:

- Use `.recipeFromResources()` to load the YAML recipe
- Combine test cases from individual recipe tests
- Exclude YAML files that are customizations of upstream OpenRewrite recipes

## Type Safety in Tests

### Using classpath() vs dependsOn()

**Prefer classpath()** when possible:

- Add `.classpath("eclipse-collections-api")` to JavaTemplate builders for real Eclipse Collections types
- Add `.classpath("eclipse-collections")` for implementation classes if needed
- Almost always works and is better than dependsOn()

**Use dependsOn()** when necessary:

- `Verify` class stubs in AssertJ migration tests (eclipse-collections-testutils classpath doesn't work)

### Removing typeValidationOptions(TypeValidation.none())

**Process:**

1. Remove `.typeValidationOptions(TypeValidation.none())` line
2. Run test
3. If passes, commit
4. If fails, investigate using [OpenRewrite FAQ](https://docs.openrewrite.org/reference/faq#im-seeing-lst-contains-missing-or-invalid-type-information-in-my-recipe-unit-tests-how-to-resolve)

**Common Fix:** Add appropriate `classpath()` entries to JavaTemplate builders to provide real type information

**Fundamental Limitations** (cannot remove TypeValidation.none()):

- Refaster-generated method references lack proper type information
- These tests must keep `.typeValidationOptions(TypeValidation.none())`

## Refactoring Verbose Tests

To refactor verbose tests into the preferred pattern:

1. **Identify all positive test methods** (those testing successful transformations)
2. **Combine their test cases** into a SINGLE `java(before, after)` call
3. **Rename the positive method** to `replacePatterns`
4. **Keep only one negative test** named `doNotReplaceInvalidPatterns` with one `java(code)` call
5. **Move `@DocumentExample`** to the combined positive method
6. **Delete the extra test methods**
