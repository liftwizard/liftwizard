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
    - Tests ALL successful transformation scenarios in a single test method
    - Multiple related variations combined into one example
    - Shows the complete range of what the recipe can handle

2. **Negative test method**:
    - Name: `doNotReplaceInvalidPatterns`
    - Tests ALL cases where transformation should NOT occur
    - Edge cases, invalid patterns, unrelated code
    - No expected changes in output

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

**Combine related scenarios into single test methods.**

Instead of separate methods:

```java
@Test void replaceWithMessage() { ... }
@Test void replaceWithoutMessage() { ... }
@Test void replaceWithZero() { ... }
@Test void replaceWithVariable() { ... }
```

Combine into one test:

```java
@Test
@DocumentExample
void replacePatterns() {
    this.rewriteRun(
        java(
            """
            // All "with message" examples
            // All "without message" examples
            // Zero count examples
            // Variable examples
            // Any other variations
            """
        )
    );
}
```

**What counts as "related scenarios":**

- Different parameter combinations (with/without message, with/without initialCapacity)
- Different data types (String, Integer, List)
- Different syntax forms (diamond operator, explicit generics, raw types)
- Different contexts (fields, local variables, return statements, if conditions)
- Edge cases like zero values or empty collections

**Create separate tests ONLY when:**

- Testing completely different recipes/transformations
- Separating positive cases (replacements happen) from negative cases (no changes)

## Example Test Pattern

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
                // Input showing ALL transformation cases:
                // - Basic case
                // - With message parameter
                // - Without message parameter
                // - Different types
                // - Edge cases
                // - Any other variations
                """,
                """
                // Expected output for all cases
                """
            )
        );
    }

    @Test
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
            java(
                """
                // Input showing ALL cases where no transformation occurs:
                // - Already correct code
                // - Invalid patterns
                // - Edge cases that shouldn't transform
                // - Unrelated code
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
2. **Combine their test cases** into a single `java()` block in the positive method
3. **Rename the positive method** to `replacePatterns`
4. **Keep only one negative test** named `doNotReplaceInvalidPatterns`
5. **Move `@DocumentExample`** to the combined positive method
6. **Delete the extra test methods**
