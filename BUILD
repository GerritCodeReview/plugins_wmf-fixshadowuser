load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "wmf-fixshadowuser",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: wmf-fixshadowuser",
        "Gerrit-Module: com.googlesource.gerrit.plugins.wmf.fixshadowuser.Module",
        "Implementation-Title: WMF Fix Shadow User",
        "Implementation-Version: 0.0.1~201807201449",
        "Implementation-URL: https://gerrit-review.googlesource.com/admin/repos/plugins/wmf-fixshadowuser",
    ],
    resources = glob(["src/main/resources/**/*"]),
)
