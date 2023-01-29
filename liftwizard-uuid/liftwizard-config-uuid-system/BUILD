java_library(
    name = "liftwizard-config-uuid-system",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//liftwizard-uuid/liftwizard-config-uuid",
        "@com_google_auto_service",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_code_findbugs_jsr305",
    ],
)

java_test(
    name = "SystemUUIDFactoryTest",
    size = "small",
    srcs = glob(["src/test/java/**/*.java"]),
    classpath_resources = glob(["src/test/resources/**"]),
    deps = [
        ":liftwizard-config-uuid-system",
        "//liftwizard-jackson/liftwizard-jackson-config",
        "//liftwizard-utility/liftwizard-junit-rule-log-marker",
        "//liftwizard-uuid/liftwizard-config-uuid",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:io_dropwizard_dropwizard_configuration",
        "@maven//:io_dropwizard_dropwizard_jackson",
        "@maven//:io_dropwizard_dropwizard_jersey",
        "@maven//:javax_validation_validation_api",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
