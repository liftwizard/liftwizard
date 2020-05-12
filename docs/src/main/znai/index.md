# Liftwizard

Liftwizard is a collection of bundles and add-ons for [Dropwizard](https://www.dropwizard.io/), the Java framework for writing web services.

There are very few dependencies between the bundles, so you can pick and choose the ones you want.

## Module groups

The bundles can be loosely grouped into categories.
* Dropwizard configuration
* JSON serialization/deserialization
* Servlet client/server logging
* [Reladomo](https://github.com/goldmansachs/reladomo) ORM integration for Dropwizard
* Other Dropwizard utility

## Guide structure

In this guide, we'll start with the application [`dropwizard-example`](https://github.com/dropwizard/dropwizard/tree/master/dropwizard-example) which is a maven module that's part of the main Dropwizard repository. We'll gradually turn it into [`liftwizard-example`](https://github.com/motlin/liftwizard/tree/master/liftwizard-example), an application with an identical service api that uses as many Liftwizard features as possible.

