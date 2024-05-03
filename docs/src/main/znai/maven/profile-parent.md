The "profile" parent pom inherits from [`liftwizard-minimal-parent`](minimal-parent). If you are able to accept more opinionated defaults, continue to [`bill-of-materials`](bill-of-materials). The profile parent contains a number of plugins you may want to enable, each wrapped individually in a maven profile.

## Usage

Inherit from `liftwizard-profile-parent` in your project's pom.xml:

```xml
<parent>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-profile-parent</artifactId>
    <version>${liftwizard.version}</version>
</parent>
```

## What you will get

The following sections describe the profiles that are added by `liftwizard-profile-parent`. You will not need to configure these in your project's pom.xml if you inherit from `liftwizard-profile-parent`. You can enable the profiles using `mvn --activate-profiles <profile1>,<profile2>,...`

## Active by default

According to [the docs](https://maven.apache.org/guides/introduction/introduction-to-profiles.html):

> Profiles can be active by default using a configuration like the following in a POM.

```xml
<profiles>
  <profile>
    <id>profile-name</id>
    <activation>
      <activeByDefault>true</activeByDefault>
    </activation>
    ...
  </profile>
</profiles>
```

> This profile will automatically be active for all builds unless another profile in the same POM is activated using one of the previously described methods. All profiles that are active by default are automatically deactivated when a profile in the POM is activated on the command line or through its activation config.

This is confusing for new users, who are first confused to find some profiles are enabled by default, and later confused to find out that they are no longer enabled. No profiles in `liftwizard-profile-parent` are active by default, and we recommend avoiding `activeByDefault` in your project's pom.xml too.

## maven-enforcer-plugin

:include-file: liftwizard-profile-parent/pom.xml {
title: "liftwizard-profile-parent/pom.xml",
surroundedBy: ["region Phase 1: validate"]
}

> The Enforcer plugin provides goals to control certain environmental constraints such as Maven version, JDK version and OS family along with many more built-in rules and user created rules.

The [dependencyConvergence](https://maven.apache.org/enforcer/enforcer-rules/dependencyConvergence.html#dependency-convergence) rule requires that dependency version numbers converge. If a project has two dependencies, A and B, both depending on the same artifact, C, this rule will fail the build if A depends on a different version of C than the version of C depended on by B.

The [requirePluginVersions](https://maven.apache.org/enforcer/enforcer-rules/requirePluginVersions.html) rule enforces that all plugins have a version defined, either in the plugin or pluginManagement section of the pom or a parent pom.

The [bannedDependencies](https://maven.apache.org/enforcer/enforcer-rules/bannedDependencies.html) rule is configured to ban all loggers except Log4j 1.x and Logback.

The [banDuplicatePomDependencyVersions](https://maven.apache.org/enforcer/enforcer-rules/banDuplicatePomDependencyVersions.html) checks that there are no duplicate dependencies declared in the POM of the project. Duplicate dependencies are dependencies which have the same group id, artifact id, type and classifier.

## extra-enforcer-rules

The [`extra-enforcer-rules` project](https://www.mojohaus.org/extra-enforcer-rules/) provides extra rules which are not part of the standard rule set. The `liftwizard-minimal-parent` configures `maven-enforcer-plugin` to use the `extra-enforcer-rules`.

:include-file: liftwizard-minimal-parent/pom.xml {
title: "liftwizard-minimal-parent/pom.xml",
surroundedBy: ["region Phase 1: validate"]
}
