java_library(
    name = "liftwizard-config-clock-system",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//liftwizard-clock/liftwizard-config-clock",
        "@com_google_auto_service",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:janino_janino",
        "@maven//:org_eclipse_collections_eclipse_collections",
        "@maven//:org_eclipse_collections_eclipse_collections_api",
    ],
)

java_test(
    name = "SystemClockFactoryTest",
    size = "small",
    srcs = glob(["src/test/java/**/*.java"]),
    classpath_resources = glob(["src/test/resources/**"]),
    deps = [
        ":liftwizard-config-clock-system",
        "//liftwizard-clock/liftwizard-config-clock",
        "//liftwizard-jackson/liftwizard-jackson-config",
        "//liftwizard-utility/liftwizard-junit-rule-log-marker",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:io_dropwizard_dropwizard_configuration",
        "@maven//:io_dropwizard_dropwizard_jackson",
        "@maven//:io_dropwizard_dropwizard_jersey",
        "@maven//:janino_janino",
        "@maven//:javax_validation_validation_api",
        "@maven//:junit_junit",
        "@maven//:org_eclipse_collections_eclipse_collections",
        "@maven//:org_eclipse_collections_eclipse_collections_api",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
