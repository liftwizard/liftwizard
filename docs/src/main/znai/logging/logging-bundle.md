# ConfigLoggingBundle

The `ConfigLoggingBundle` logs the Dropwizard configuration to slf4j at INFO level, by serializing the in-memory configuration object to json. It does not echo the contents of the configuration file back. The output will contain default values that were not specified in the original configuration file. Some other values will be normalized or pretty printed.

To turn it on, add `ConfigLoggingBundle` to the list of registered bundles.

:include-java: com/example/helloworld/HelloWorldApplication.java {entry: "initialize", readMore: true, readMoreLines: 8}

Now `HelloWorldApplication` will log something like this on startup:

```
INFO  12:53:29 [main]  {liftwizard.priority=-8, liftwizard.bundle=ConfigLoggingBundle} com.liftwizard.dropwizard.bundle.config.logging.ConfigLoggingBundle: Inferred Dropwizard configuration:
```
```json5 {title: "Output configuration"}
{
  "template": "Hello, %s!",
  "defaultName": "Stranger",
  "configLoggingFactory": {
    "enabled": true
  },
  // ...
  "metrics": {
    "frequency": "1 minute",
    "reporters": [ ]
  }
}
```

:include-json: test-example.json5 {title: "Original configuration", collapsedPaths: ['root.database', 'root.server', 'root.logging']}}

Note that the `metrics` section at the end was not specified in `test-example.json5`. It comes from serializing the output of `io.dropwizard.Configuration.getMetricsFactory()`.

This output can be helpful for fleshing out the configuration file with default options to make it easier to edit. For example, it's much easier to flip a boolean flag from `false` to `true` than to first figure out where in the configuration file it belongs and the exact spelling of its key.

:include-java: io/dropwizard/Configuration.java {title: "io.dropwizard.Configuration", entry: "getMetricsFactory"}

The `ConfigLoggingBundle` also logs the "default" configuration at the `DEBUG` level. It does this by instantiating a new copy of the configuration class using the default no-arg constructor, serializing it to json, and logging it. The default configuration output can be useful for finding redundant configuration to remove.
