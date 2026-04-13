Liftwizard includes a set of [OpenRewrite](https://docs.openrewrite.org/) recipes for automated code refactoring. These recipes help migrate your Java project to [Eclipse Collections](https://eclipse.dev/collections/), improve existing Eclipse Collections code with best practices, and transform logging to use SLF4J parameterized format.

## Main Composite Recipes

Most projects will use these composite recipes:

| Composite Recipe           | Description                                                        |
| -------------------------- | ------------------------------------------------------------------ |
| **Adoption**               | Migrate from Java Collections Framework to Eclipse Collections     |
| **Best Practices**         | Optimize existing Eclipse Collections code with idiomatic patterns |
| **Logging Best Practices** | Transform logging to SLF4J parameterized format                    |

**Adoption** (`io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption`) transforms JCF code like `new ArrayList<>()` into Eclipse Collections equivalents like `Lists.mutable.empty()`.

**Best Practices** (`io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsBestPractices`) transforms verbose patterns into idiomatic Eclipse Collections code, such as replacing `richIterable.size() > 0` with `richIterable.notEmpty()`.

**Logging Best Practices** (`io.liftwizard.rewrite.LoggingBestPractices`) transforms eager logging patterns to use SLF4J parameterized logging, combining Liftwizard recipes with OpenRewrite's [rewrite-logging-frameworks](https://docs.openrewrite.org/recipes/java/logging/slf4j) recipes.

## All Composite Recipes

Ten composite recipes are available in total:

| Composite Recipe                                                                             | Description                                                    |
| -------------------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| `io.liftwizard.rewrite.BestPractices`                                                        | General Java best practices for code quality and style         |
| `io.liftwizard.rewrite.logging.Log4j1ToSlf4j1`                                               | Migrate Log4j 1 to SLF4J, skipping object logging files        |
| `io.liftwizard.rewrite.LoggingBestPractices`                                                 | Transform logging to SLF4J parameterized format                |
| `io.liftwizard.rewrite.assertj.AssertJMigration`                                             | Migrate from Eclipse Collections testutils to AssertJ          |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsBestPractices`                  | Optimize existing Eclipse Collections usage                    |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption`                       | Migrate from Java Collections Framework to Eclipse Collections |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsRemoval`                        | Replace Eclipse Collections APIs with Java alternatives        |
| `io.liftwizard.rewrite.eclipse.collections.adoption.unsafe.EclipseCollectionsAdoptionUnsafe` | Adoption patterns that may change semantics with nulls         |
| `io.liftwizard.testing.junit.JupiterBestPractices`                                           | JUnit Jupiter best practices for test quality                  |
| `io.liftwizard.rewrite.dropwizard.testing.DropwizardTestingJUnit5Migration`                  | Migrate Dropwizard JUnit 4 testing rules to JUnit 5 extensions |
