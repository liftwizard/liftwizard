Dropwizard ships with a [dropwizard-migrations](https://www.dropwizard.io/en/latest/manual/migrations.html) bundle.

> The `dropwizard-migrations` module provides you with a wrapper for [Liquibase](https://www.liquibase.org/) database refactoring.

The built-in bundle provides Dropwizard Commands, for a command line interface to run migrations. It does _not_ provide a way to run migrations on application startup. That's where Liftwizard comes in.

To run migrations with Dropwizard, you run a command like `java -jar hello-world.jar db migrate helloworld.yml`.

To run migrations with Liftwizard, you run the usual `server` command, and Liftwizard will run migrations on startup.

There are pros and cons of tying migrations to application startup. The main pros are that you don't have to remember to run migrations, and that they apply to embedded databases in tests. The main con is that migrations can take a long time, and you may not want to block application startup.

To turn it on, add `LiftwizardLiquibaseMigrationBundle` to the list of registered bundles.

```java {title: "HelloWorldApplication.initialize()"}
@Override
public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap)
{
    // ...
    bootstrap.addBundle(new LiftwizardLiquibaseMigrationBundle());
    // ...
}
```

Change the Configuration class to implement `LiquibaseMigrationFactoryProvider`.

```java {title: "HelloWorldConfiguration.java"}
public class HelloWorldConfiguration
        extends Configuration
        implements LiquibaseMigrationFactoryProvider // , ... other interfaces
{
    // ...
}
```

Add a field with type `LiquibaseMigrationFactory`.

:include-java: com/example/helloworld/HelloWorldConfiguration.java {
title: "HelloWorldConfiguration.java field",
surroundedBy: "// include-liquibaseMigrationFactory",
}

Add the getter/setter required by the interface.

:include-java: com/example/helloworld/HelloWorldConfiguration.java {
title: "HelloWorldConfiguration.java methods",
entry: ["getLiquibaseMigrationFactory", "setLiquibaseMigrationFactory"],
}

## Configuration

The `LiftwizardLiquibaseMigrationBundle` requires that you're already using [named data sources](database/named-data-source).

Add a liquibase section to your [json](configuration/json5-configuration) or yaml configuration.

:include-json: test-example.json5 {
title: "example.json5",
include: "$['liquibase', 'absent']",
paths: ["root.liquibase.dataSourceMigrations[0].dataSourceName"],
}

`dataSourceMigrations` is an array, to allow multiple migrations to different data sources.

Each dataSourceMigration's `dataSourceName` must match a dataSource's name in the `dataSources` section.

If no `migrationFileName` is specified, `migrations.xml` is the default.

`migrationFileLocation` can be `classpath` or `filesystem`. `classpath` is the default.

`contexts` are an array of Liquibase [context tags](https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html).

With this configuration in place, migrations will run on application startup.
