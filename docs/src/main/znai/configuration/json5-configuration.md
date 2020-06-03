# Configuration through json5 instead of yaml
 
Dropwizard's configuration is specified in yaml by default. While yaml has nice properties, you may prefer json or some other format.
 
Dropwizard's [documentation](https://www.dropwizard.io/en/latest/manual/core.html#configuration) claims:
 
> If your configuration file doesn't end in .yml or .yaml, Dropwizard tries to parse it as a JSON file.
 
This is easily disproved by renaming example.yml to example.json and trying to run the application. It will incorrectly start without error.
 
Since json syntax is a subset of yml syntax, you can go ahead and convert your configuration file to json without changing the file extension from yaml or yml. However, this approach doesn't prevent you from accidentally using yaml syntax.
 
You can change your application to use json for its configuration using `JsonConfigurationFactoryFactory`.
 
```java
@Override
public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    bootstrap.setConfigurationFactoryFactory(new JsonConfigurationFactoryFactory<>());
    // ...
}
```

`JsonConfigurationFactoryFactory` uses json5 syntax by default, using optional features in Jackson. So you'll still be able to include comments inside your configuration files.
 
You'll have to convert production configuration files. So `example.yml` becomes `example.json5`. You'll also have to convert configuration files used by `DropwizardAppRule` in tests. So `src/test/resources/test-example.yml` becomes `src/test/resources/test-example.json5`
