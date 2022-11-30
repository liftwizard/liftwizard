Dropwizard provides an interface called `ManagedDataSource`.

:include-java: io/dropwizard/db/ManagedDataSource.java {title: "io.dropwizard.db.ManagedDataSource", entry: "ManagedDataSource"}

It's just a `io.dropwizard.lifecycle.Managed` and a `javax.sql.DataSource`. It's a `DataSource` with start/stop lifecycle methods.

`ManagedDataSource` works well when you have one of them. When you have multiple data sources, it can be difficult to tie them together through configuration. For example, if you use Liquibase for migrations, [you'd need to write code](https://www.dropwizard.io/en/latest/manual/migrations.html#support-for-adding-multiple-migration-bundles) to tie specific migrations to specific data sources; it cannot be done through configuration alone.

Liftwizard provides `NamedDataSource`, which is a `ManagedDataSource` with a name. [Other Liftwizard bundles](liquibase-migrations.md) expect `NamedDataSource`s to be configured, and refer to them by name in their own configuration. In the liquibase example, we could tie specific migrations to specific data sources through configuration alone.

Different named data sources can refer to different databases, or the same database configured different ways. In the following example, we have one data source for Postgres, and three data sources to connect to h2; over the network, in-memory, and on disk.

:include-json: test-example.json5 {
title: "example.json5",
include: "$['absent', 'dataSources']",
paths: ["root.dataSources[0].name", "root.dataSources[1].name", "root.dataSources[2].name", "root.dataSources[3].name"],
}

To use named data sources, start by changing the Configuration class to implement `NamedDataSourceFactoryProvider`.

```java {title: "HelloWorldConfiguration.java"}
public class HelloWorldConfiguration
        extends Configuration
        implements NamedDataSourceProvider // , ... other interfaces
{
    // ...
}
```

Add a field with type `NamedDataSourcesFactory`.

:include-java: com/example/helloworld/HelloWorldConfiguration.java {
title: "HelloWorldConfiguration.java field",
surroundedBy: "// include-namedDataSourcesFactory",
}

Add the getter/setter required by the interface.

:include-java: com/example/helloworld/HelloWorldConfiguration.java {
title: "HelloWorldConfiguration.java methods",
entry: ["getNamedDataSourcesFactory", "setNamedDataSourcesFactory"],
}

Now we can use the named data sources in the configuration of other bundles. For example, we use the data source named `h2-tcp` in the liquibase configuration.

:include-json: test-example.json5 {
title: "example.json5",
include: "$['absent', 'liquibase']",
paths: ["root.liquibase.dataSourceMigrations[0].dataSourceName"],
}
