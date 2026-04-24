## Configure Maven Plugin

Add the `rewrite-maven-plugin` with the `liftwizard-rewrite` dependency:

```xml
<properties>
    <liftwizard.version>2.1.38</liftwizard.version>
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
                    <recipe>io.liftwizard.rewrite.LoggingBestPractices</recipe>
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

## Run Recipes

```bash
# Preview changes (dry run)
mvn rewrite:dryRun

# Apply all configured recipes
mvn rewrite:run

# Run a specific recipe
mvn rewrite:run -Drewrite.activeRecipes=io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption
```
