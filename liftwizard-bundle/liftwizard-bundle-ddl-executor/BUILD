java_library(
    name = "liftwizard-bundle-ddl-executor",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//liftwizard-bundle/liftwizard-bundle-prioritized-api",
        "//liftwizard-config/liftwizard-config-data-source",
        "//liftwizard-config/liftwizard-config-ddl-executor",
        "//liftwizard-reladomo/liftwizard-ddl-executor",
        "@com_google_auto_service",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:io_dropwizard_dropwizard_core",
        "@maven//:janino_janino",
        "@maven//:org_eclipse_collections_eclipse_collections",
        "@maven//:org_eclipse_collections_eclipse_collections_api",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
