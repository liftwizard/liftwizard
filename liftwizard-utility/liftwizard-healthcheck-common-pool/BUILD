java_library(
    name = "liftwizard-healthcheck-common-pool",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "@maven//:io_dropwizard_metrics_metrics_core",
    ],
    deps = [
        "//liftwizard-logging/liftwizard-logging-mdc-closeable",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:io_dropwizard_metrics_metrics_healthchecks",
        "@maven//:org_eclipse_collections_eclipse_collections",
        "@maven//:org_eclipse_collections_eclipse_collections_api",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_test(
    name = "CommonPoolHealthCheckTest",
    size = "small",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob(["src/test/resources/*"]),
    deps = [
        ":liftwizard-healthcheck-common-pool",
        "//liftwizard-utility/liftwizard-junit-rule-log-marker",
        "@maven//:io_dropwizard_metrics_metrics_healthchecks",
        "@maven//:janino_janino",
        "@maven//:junit_junit",
        "@maven//:org_eclipse_collections_eclipse_collections",
        "@maven//:org_eclipse_collections_eclipse_collections_api",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
