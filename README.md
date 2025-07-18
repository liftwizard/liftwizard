# Liftwizard

![Maven Central](https://img.shields.io/maven-central/v/io.liftwizard/liftwizard)
![GitHub](https://img.shields.io/github/license/liftwizard/liftwizard)
![CircleCI](https://img.shields.io/circleci/build/gh/liftwizard/liftwizard/main)
[![Reproducible Builds](https://img.shields.io/badge/Reproducible_Builds-ok-green?labelColor=blue)](https://github.com/jvm-repo-rebuild/reproducible-central#io.liftwizard:liftwizard)

![Lines of code](https://img.shields.io/tokei/lines/github/liftwizard/liftwizard)
![GitHub repo size](https://img.shields.io/github/repo-size/liftwizard/liftwizard)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/liftwizard/liftwizard)

Liftwizard is a collection of bundles and add-ons for [Dropwizard](https://www.dropwizard.io/), the Java framework for writing web services.

There are very few dependencies between the bundles, so you can pick and choose the ones you want.

## Module groups

The bundles can be loosely grouped into categories.

- Dropwizard configuration and bundles
- Jackson JSON serialization/deserialization
- Servlet client/server logging
- [Reladomo](https://github.com/goldmansachs/reladomo) ORM integration for Dropwizard
- JUnit 4 and JUnit 5 test utilities
- OpenRewrite recipes for code modernization

## OpenRewrite Recipes

Liftwizard includes OpenRewrite recipes for automating code modernization tasks. These recipes help migrate from older APIs to newer ones and apply best practices.

### Eclipse Collections Recipes

- **SimplifyNegatedEmptyChecks**: Simplifies `!iterable.isEmpty()` to `iterable.notEmpty()` and `!iterable.notEmpty()` to `iterable.isEmpty()`
- **SimplifyNegatedSatisfies**: Simplifies negated satisfies checks for Eclipse Collections
- **VerifyAssertThrowsToAssertJ**: Migrates `Verify.assertThrows()` calls to AssertJ's `assertThatThrownBy()` pattern

### Usage

To use these recipes in your project, add the following to your `pom.xml`:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.42.0</version>
    <configuration>
        <activeRecipes>
            <recipe>io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsBestPractices</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>io.liftwizard</groupId>
            <artifactId>liftwizard-rewrite</artifactId>
            <version>${liftwizard.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

Then run: `mvn rewrite:run`

## Documentation

See the full docs at <https://liftwizard.io/docs/>
