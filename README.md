# Liftwizard

## Summary

Liftwizard is a collection of bundles and add-ons for the [Dropwizard](https://www.dropwizard.io/) framework for writing Java web services.

There are very few dependencies between the bundles, so you can pick and choose the ones you want.

# Liftwizard modules: Table of Contents

* Configuration
    * [`liftwizard-bundle-environment-config`](liftwizard-bundle/liftwizard-bundle-environment-config/README.md): Accept configuration substitutions from environment variables
    * [`liftwizard-configuration-factory-json`](liftwizard-config/liftwizard-configuration-factory-json/README.md): Convert configuration files from yaml to json
    * [`liftwizard-bundle-logging-config`](liftwizard-logging/liftwizard-bundle-logging-config/README.md) Log actual and default configuration objects as json
* Jackson
    * [`liftwizard-bundle-object-mapper`](liftwizard-jackson/liftwizard-bundle-object-mapper/README.md) Configure Jackson's ObjectMapper with pretty printing, json5 features, and formatted timestamps
* Logging
    * [`liftwizard-logging`](liftwizard-logging/README.md) Add servlet/jersey filters for logging request and response metadata
    * [`liftwizard-bundle-logging-http`](liftwizard-logging/liftwizard-bundle-logging-http/README.md) Add Jersey filter for logging request and response data
* Additional utility
    * [`liftwizard-config-logging-filter-requesturl`](liftwizard-logging/liftwizard-config-logging-filter-requesturl/README.md)
