load("@rules_antlr//antlr:antlr4.bzl", "antlr")

antlr(
    name = "operation",
    srcs = ["src/main/antlr4/io/liftwizard/model/reladomo/operation/ReladomoOperation.g4"],
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
