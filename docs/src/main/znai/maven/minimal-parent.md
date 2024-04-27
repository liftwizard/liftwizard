The most minimal parent pom is `liftwizard-minimal-parent`. If you are able to accept more opinionated defaults, continue to [`liftwizard-profile-parent`](profile-parent). The minimal parent is meant to contain uncontroversial best practices that are applicable to all projects.

## Usage

Inherit from `liftwizard-minimal-parent` in your project's pom.xml:

```xml
<parent>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-minimal-parent</artifactId>
    <version>${liftwizard.version}</version>
</parent>
```

## What you will get

The following sections describe the best practices that are enforced by `liftwizard-minimal-parent`. You will not need to configure these in your project's pom.xml.

## Resource encodings

If you encounter a warning like: `[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!` this is because the project does not [specify a character encoding scheme](https://maven.apache.org/plugins/maven-resources-plugin/examples/encoding.html#specifying-a-character-encoding-scheme) to configure `maven-resources-plugin`.

`liftwizard-minimal-parent` specifies the character encoding scheme in the `properties` section of the pom.xml.

:include-file: liftwizard-minimal-parent/pom.xml {
title: "liftwizard-minimal-parent/pom.xml",
surroundedBy: ["region sourceEncoding"]
}

This will become unnecessary starting with maven 4.x.

## Reproducible builds

> [Reproducible builds](https://reproducible-builds.org/)  are a set of software development practices that create an independently-verifiable path from source to binary code. A build is  **reproducible**  if given the same source code, build environment and build instructions, any party can recreate  **bit-by-bit**  identical copies of all specified artifacts.


You can [enable Reproducible Builds mode for plugins](https://maven.apache.org/guides/mini/guide-reproducible-builds.html#how-do-i-configure-my-maven-build) by specifying locking down the outputTimestamp property.

:include-file: liftwizard-minimal-parent/pom.xml {
title: "liftwizard-minimal-parent/pom.xml",
surroundedBy: ["region outputTimestamp"]
}

You will also need to run `mvn artifact:check-buildplan` and `mvn verify artifact:compare` as described in the guide to validate that builds are truly reproducible.

## Default Goal

You can specify the default goal to run when you run `mvn` without any arguments.

:include-file: liftwizard-minimal-parent/pom.xml {
title: "liftwizard-minimal-parent/pom.xml",
surroundedBy: ["region defaultGoal"]
}

`verify` is a better choice than `install` in the presence of concurrent builds that may write to `.m2/repository` simultaneously.

`verify` is a better choice than `clean verify` because developers may build up state like test files and test databases under `target/` and may not expect them to be deleted by default. It's easy to run `mvn clean` when you need it.

## Plugins which are bound and enabled by default

Maven builds are configured by binding plugins to [lifecycle phases](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#default-lifecycle). Even if you don't declare any plugins in your pom.xml, maven will still bind [some plugins](https://maven.apache.org/ref/3.9.6/maven-core/default-bindings.html#plugin-bindings-for-jar-packaging) to the ["main" phases](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#packaging).

All versions of maven bind the [same plugins](https://maven.apache.org/ref/3.9.6/maven-core/default-bindings.html#plugin-bindings-for-jar-packaging), but newer versions of maven bind newer versions of the plugins. Maven 3.9.6 binds:

| Plugin                 | Version| Phase         |
|------------------------|--------|---------------|
| maven-resources-plugin | 3.3.1  | resources     |
| maven-compiler-plugin  | 3.11.0 | compile       |
| maven-resources-plugin | 3.3.1  | testResources |
| maven-compiler-plugin  | 3.11.0 | testCompile   |
| maven-surefire-plugin  | 3.2.2  | test          |
| maven-jar-plugin       | 3.3.0  | jar           |
| maven-install-plugin   | 3.1.1  | install       |
| maven-deploy-plugin    | 3.1.1  | deploy        |

If you don't specify the versions of the plugins, different members of the team could be using different versions of the plugins. This could lead to different builds on different machines.

It's becoming more common to lock down the version of maven itself, but this wasn't always the case. If you haven't specified the versions of these plugins, [maven-enforcer-plugin](https://maven.apache.org/enforcer/enforcer-rules/requirePluginVersions.html) will log an error like:

```
[ERROR] Rule 3: org.apache.maven.enforcer.rules.RequirePluginVersions failed with message:

Some plugins are missing valid versions or depend on Maven 3.9.5 defaults (LATEST, RELEASE as plugin version are not allowed)
   org.apache.maven.plugins:maven-compiler-plugin. 	The version currently in use is 3.11.0 via default lifecycle bindings
   org.apache.maven.plugins:maven-surefire-plugin. 	The version currently in use is 3.1.2 via default lifecycle bindings
   org.apache.maven.plugins:maven-jar-plugin. 		The version currently in use is 3.3.0 via default lifecycle bindings
   org.apache.maven.plugins:maven-clean-plugin. 	The version currently in use is 3.2.0 via default lifecycle bindings
   org.apache.maven.plugins:maven-install-plugin. 	The version currently in use is 3.1.1 via default lifecycle bindings
   org.apache.maven.plugins:maven-site-plugin. 		The version currently in use is 3.12.1 via default lifecycle bindings
   org.apache.maven.plugins:maven-resources-plugin. 	The version currently in use is 3.3.1 via default lifecycle bindings
   org.apache.maven.plugins:maven-deploy-plugin. 	The version currently in use is 3.1.1 via default lifecycle bindings
```

To avoid this, we specify versions of the plugins in the parent pom.

:include-file: liftwizard-minimal-parent/pom.xml {
title: "liftwizard-minimal-parent/pom.xml",
surroundedBy: ["region Plugins which are bound and enabled by default"]
}

## Learning Maven

Maven can be confusing due to the extent of the "convention over configuration" approach.

For example, to answer "how does maven run compilation before tests" you would need to learn:

- Plugins which are bound and enabled by default
- `maven-surefire-plugin` is the plugin that handles tests
- `maven-compiler-plugin` binds to the `compile` and `testCompile` phases. `maven-surefire-plugin` binds to the `test` phase
- In the [lifecycle phases](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#default-lifecycle) `compile` comes before `testCompile` which comes before `test`.

None of this information appears in `pom.xml`, and little of it is logged during the build.

To make it easier to understand, `liftwizard-minimal-parent` includes region markers surrounding each plugin that label the phase that the plugin is bound to. The sections are sorted by phase.

:include-file: liftwizard-minimal-parent/pom.xml {
title: "liftwizard-minimal-parent/pom.xml",
surroundedBy: ["region Phase 22: install"],
surroundedByKeep: true
}

## No phase

Any maven plugin can be run from the command line with `mvn groupId:artifactId:goal`, and configured using command line arguments, without binding it to a phase in the pom.xml.
