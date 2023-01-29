workspace(name = "liftwizard")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Maven artifacts.

RULES_JVM_EXTERNAL_TAG = "4.5"

RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" %
          RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

# Mavel artifact versions here should match those in pom.xml.
maven_install(
    artifacts = [
        "ch.qos.logback:logback-classic:1.2.11",
        "ch.qos.logback:logback-core:1.2.11",
        "ch.qos.reload4j:reload4j:1.2.24",
        "com.fasterxml.jackson.core:jackson-annotations:2.14.1",
        "com.fasterxml.jackson.datatype:jackson-datatype-eclipse-collections:2.14.1",
        "com.goldmansachs.reladomo:reladomo-gen-util:18.0.0",
        "com.goldmansachs.reladomo:reladomo-serial:18.0.0",
        "com.goldmansachs.reladomo:reladomo-test-util:18.0.0",
        "com.goldmansachs.reladomo:reladomo:18.0.0",
        "com.goldmansachs.reladomo:reladomogen:18.0.0",
        "com.goldmansachs.xsd2bean:xsd2beangen:1.5.0",
        "com.google.auto.service:auto-service-annotations:1.0.1",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.errorprone:error_prone_annotations:2.18.0",
        "com.google.firebase:firebase-admin:8.1.0",
        "com.google.guava:guava:31.1-jre",
        "com.graphql-java:graphql-java:15.0",
        "com.h2database:h2:1.4.200",
        "com.mattbertolini:liquibase-slf4j:2.0.0",
        "com.smoketurner.dropwizard:graphql-core:1.3.17-1",
        "com.vaadin.external.google:android-json:0.0.20131108.vaadin1",
        "io.dropwizard:dropwizard-auth:1.3.29",
        "io.dropwizard:dropwizard-bom:1.3.29",
        "io.dropwizard:dropwizard-db:1.3.29",
        "io.dropwizard:dropwizard-migrations:1.3.29",
        "io.logz.logback:logzio-logback-appender:1.0.28",
        "io.opentracing:opentracing-api:0.33.0",
        "io.opentracing:opentracing-util:0.33.0",
        "jakarta.activation:jakarta.activation-api:2.1.1",
        "jakarta.xml.bind:jakarta.xml.bind-api:4.0.0",
        "janino:janino:2.5.10",
        "javax.activation:activation:1.1.1",
        "javax.annotation:javax.annotation-api:1.3.2",
        "javax.inject:javax.inject:1",
        "javax.validation:validation-api:2.0.1.Final",
        "javax.ws.rs:javax.ws.rs-api:2.1.1",
        "joda-time:joda-time:2.12.2",
        "junit:junit:4.13.2",
        "net.logstash.logback:logstash-logback-encoder:7.2",
        "org.antlr:antlr4-runtime:4.9.3",
        "org.apache.commons:commons-lang3:3.12.0",
        "org.apache.commons:commons-text:1.10.0",
        "org.apache.httpcomponents:httpclient:4.5.14",
        "org.apache.httpcomponents:httpcore:4.4.16",
        "org.apache.maven.plugin-tools:maven-plugin-annotations:3.7.1",
        "org.apache.maven:maven-artifact:3.8.7",
        "org.apache.maven:maven-core:3.6.3",
        "org.apache.maven:maven-model:3.8.7",
        "org.apache.maven:maven-plugin-api:3.6.3",
        "org.apache.maven:maven-project:3.0-alpha-2",
        "org.checkerframework:checker-qual:3.29.0",
        "org.codehaus.plexus:plexus-classworlds:2.7.0",
        "org.codehaus.plexus:plexus-compiler-api:2.13.0",
        "org.codehaus.plexus:plexus-component-annotations:2.1.1",
        "org.codehaus.plexus:plexus-interpolation:1.26",
        "org.codehaus.plexus:plexus-utils:3.5.0",
        "org.eclipse.collections:eclipse-collections-api:11.1.0",
        "org.eclipse.collections:eclipse-collections:11.1.0",
        "org.fusesource.jansi:jansi:2.4.0",
        "org.glassfish.hk2.external:aopalliance-repackaged:3.0.3",
        "org.glassfish.hk2:hk2-api:2.5.0-b32",
        "org.glassfish.jaxb:jaxb-runtime:4.0.1",
        "org.hamcrest:hamcrest-core:1.3",
        "org.liquibase:liquibase-core:3.10.3",
        "org.reflections:reflections:0.9.9",
        "org.skyscreamer:jsonassert:1.5.1",
        "org.slf4j:slf4j-api:1.7.36",
        "org.slf4j:slf4j-nop:2.0.6",
        "org.slf4j:slf4j-simple:2.0.6",
        "org.sonatype.plexus:plexus-build-api:0.0.7",
        "org.springframework:spring-web:5.3.23",
        "org.yaml:snakeyaml:1.33",
        "p6spy:p6spy:3.9.1",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)

# ANTLR

http_archive(
    name = "rules_antlr",
    sha256 = "26e6a83c665cf6c1093b628b3a749071322f0f70305d12ede30909695ed85591",
    strip_prefix = "rules_antlr-0.5.0",
    urls = ["https://github.com/marcohu/rules_antlr/archive/0.5.0.tar.gz"],
)

load("@rules_antlr//antlr:repositories.bzl", "rules_antlr_dependencies")

rules_antlr_dependencies("4.8")

# ========================================

# Native way to install the AutoService annotation processor.
# Replace it with a Maven version at some point.

load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")

java_import_external(
    name = "com_google_code_findbugs_jsr305",
    jar_sha256 = "905721a0eea90a81534abb7ee6ef4ea2e5e645fa1def0a5cd88402df1b46c9ed",
    jar_urls = [
        "http://domain-registry-maven.storage.googleapis.com/repo1.maven.org/maven2/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar",
        "http://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar",
    ],
    licenses = ["notice"],  # The Apache Software License, Version 2.0
)

java_import_external(
    name = "com_google_errorprone_error_prone_annotations",
    jar_sha256 = "e7749ffdf03fb8ebe08a727ea205acb301c8791da837fee211b99b04f9d79c46",
    jar_urls = [
        "http://domain-registry-maven.storage.googleapis.com/repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.0.15/error_prone_annotations-2.0.15.jar",
        "http://repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.0.15/error_prone_annotations-2.0.15.jar",
    ],
    licenses = ["notice"],  # Apache 2.0
)

java_import_external(
    name = "com_google_guava",
    jar_sha256 = "7baa80df284117e5b945b19b98d367a85ea7b7801bd358ff657946c3bd1b6596",
    jar_urls = [
        "http://repo1.maven.org/maven2/com/google/guava/guava/23.0/guava-23.0.jar",
        "http://domain-registry-maven.storage.googleapis.com/repo1.maven.org/maven2/com/google/guava/guava/23.0/guava-23.0.jar",
    ],
    licenses = ["notice"],  # The Apache Software License, Version 2.0
    exports = [
        "@com_google_code_findbugs_jsr305",
        "@com_google_errorprone_error_prone_annotations",
    ],
)

java_import_external(
    name = "com_google_auto_common",
    jar_sha256 = "eee75e0d1b1b8f31584dcbe25e7c30752545001b46673d007d468d75cf6b2c52",
    jar_urls = [
        "http://domain-registry-maven.storage.googleapis.com/repo1.maven.org/maven2/com/google/auto/auto-common/0.7/auto-common-0.7.jar",
        "http://repo1.maven.org/maven2/com/google/auto/auto-common/0.7/auto-common-0.7.jar",
    ],
    licenses = ["notice"],  # Apache 2.0
    deps = ["@com_google_guava"],
)

java_import_external(
    name = "com_google_auto_service",
    extra_build_file_content = "\n".join([
        "java_plugin(",
        "    name = \"AutoServiceProcessor\",",
        "    output_licenses = [\"unencumbered\"],",
        "    processor_class = \"com.google.auto.service.processor.AutoServiceProcessor\",",
        "    deps = [\":processor\"],",
        ")",
        "",
        "java_library(",
        "    name = \"com_google_auto_service\",",
        "    exported_plugins = [\":AutoServiceProcessor\"],",
        "    exports = [\":compile\"],",
        ")",
    ]),
    generated_linkable_rule_name = "processor",
    generated_rule_name = "compile",
    jar_sha256 = "46808c92276b4c19e05781963432e6ab3e920b305c0e6df621517d3624a35d71",
    jar_urls = [
        "http://domain-registry-maven.storage.googleapis.com/repo1.maven.org/maven2/com/google/auto/service/auto-service/1.0-rc2/auto-service-1.0-rc2.jar",
        "http://repo1.maven.org/maven2/com/google/auto/service/auto-service/1.0-rc2/auto-service-1.0-rc2.jar",
    ],
    licenses = ["notice"],  # Apache 2.0
    neverlink = True,
    deps = [
        "@com_google_auto_common",
        "@com_google_guava",
    ],
)
