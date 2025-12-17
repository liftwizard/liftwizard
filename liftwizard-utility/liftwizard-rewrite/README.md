# Liftwizard OpenRewrite Recipes

[OpenRewrite](https://docs.openrewrite.org/) is a tool for automated code refactoring. These recipes help migrate your Java project to [Eclipse Collections](https://eclipse.dev/collections/) or improve existing Eclipse Collections code with best practices.

## Main Composite Recipes

Most projects will use these two composite recipes:

| Composite Recipe   | Description                                                        |
| ------------------ | ------------------------------------------------------------------ |
| **Adoption**       | Migrate from Java Collections Framework to Eclipse Collections     |
| **Best Practices** | Optimize existing Eclipse Collections code with idiomatic patterns |

**Adoption** (`io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption`) transforms JCF code like `new ArrayList<>()` into Eclipse Collections equivalents like `Lists.mutable.empty()`.

**Best Practices** (`io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsBestPractices`) transforms verbose patterns into idiomatic Eclipse Collections code, such as replacing `richIterable.size() > 0` with `richIterable.notEmpty()`.

## Getting Started

### Configure Maven Plugin

Add the rewrite-maven-plugin with the liftwizard-rewrite dependency:

```xml
<properties>
    <liftwizard.version>2.1.37</liftwizard.version>
    <rewrite-maven-plugin.version>6.25.0</rewrite-maven-plugin.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.openrewrite.maven</groupId>
            <artifactId>rewrite-maven-plugin</artifactId>
            <version>${rewrite-maven-plugin.version}</version>
            <configuration>
                <activeRecipes>
                    <recipe>io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption</recipe>
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
    </plugins>
</build>
```

### Step 3: Run Recipes

```bash
# Preview changes (dry run)
mvn rewrite:dryRun

# Apply all configured recipes
mvn rewrite:run

# Run a specific recipe
mvn rewrite:run -Drewrite.activeRecipes=io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption
```

## Full Recipe Reference

For a complete listing of all recipes and their transformations, see [RECIPES.md](RECIPES.md).
