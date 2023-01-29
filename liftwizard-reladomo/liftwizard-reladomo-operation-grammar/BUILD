load("@rules_antlr//antlr:antlr4.bzl", "antlr")
load("antlr_gen.bzl", "antlr_gen")

antlr(
    name = "operation",
    srcs = ["src/main/antlr4/io/liftwizard/model/reladomo/operation/ReladomoOperation.g4"],
    error = True,
    listener = True,
    package = "io.liftwizard.model.reladomo.operation",
    visitor = True,
)

java_library(
    name = "liftwizard-reladomo-operation-grammar",
    srcs = glob(["src/main/**/*.java"]) + [":operation"],
    visibility = ["//visibility:public"],
    deps = [
        "@antlr4_runtime//jar",
        "@maven//:janino_janino",
        "@maven//:org_eclipse_collections_eclipse_collections",
        "@maven//:org_eclipse_collections_eclipse_collections_api",
    ],
)

java_binary(
    name = "antlr4",
    main_class = "org.antlr.v4.Tool",
    runtime_deps = [
        "@maven//:org_antlr_antlr4",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)
