def _antlr_gen_impl(ctx):
    pass

antlr_gen = rule(
    attrs = {
        "deps": attr.label_list(),
        "grammar": attr.label(
            allow_single_file = [".g4"],
            mandatory = True,
        ),
        "_jdk": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_runtime"),
            providers = [java_common.JavaRuntimeInfo],
        ),
        "antlr_binary": attr.label(
            allow_single_file = True,
            cfg = "host",
            # default = Label(":antlr"),
        ),
    },
    outputs = {
        "codegen": "%{name}_codegen.srcjar",
    },
    implementation = _antlr_gen_impl,
)
