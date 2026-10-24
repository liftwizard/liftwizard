"""Print the OpenRewrite recipe modules that have to be built from source, one artifactId:version per line, dependencies first.

The starting point is every org.openrewrite.recipe dependency of rewrite-maven-plugin in the effective POMs given as arguments. From there it follows the dependencies in each module's official POM on the Code Genome Project. A module has to be built when Code Genome refuses its jar, which it does for every module under the Moderne Source Available License.

Credentials come from the CODEGENOME_USERNAME and CODEGENOME_TOKEN environment variables.
"""

import os
import subprocess
import sys
import xml.etree.ElementTree as ElementTree

CODEGENOME_URL = "https://artifacts.codegenomeproject.org/maven/org/openrewrite/recipe"
NAMESPACE = {"m": "http://maven.apache.org/POM/4.0.0"}


def curl(*arguments):
    # The credentials go through stdin so that they never appear in the process list. curl does not send them on to the storage host that Code Genome redirects to.
    config = 'user = "{}:{}"\n'.format(os.environ["CODEGENOME_USERNAME"], os.environ["CODEGENOME_TOKEN"])
    return subprocess.run(["curl", "--silent", "--show-error", "--config", "-", *arguments], input=config, capture_output=True, text=True, check=True).stdout


def recipe_dependencies(element):
    for dependency in element.findall("m:dependencies/m:dependency", NAMESPACE):
        scope = dependency.findtext("m:scope", namespaces=NAMESPACE)
        if dependency.findtext("m:groupId", namespaces=NAMESPACE) == "org.openrewrite.recipe" and scope in (None, "compile", "runtime"):
            yield dependency.findtext("m:artifactId", namespaces=NAMESPACE), dependency.findtext("m:version", namespaces=NAMESPACE)


def plugin_recipes(effective_pom):
    root = ElementTree.parse(effective_pom).getroot()
    for plugin in root.iter("{%s}plugin" % NAMESPACE["m"]):
        if plugin.findtext("m:artifactId", namespaces=NAMESPACE) == "rewrite-maven-plugin":
            yield from recipe_dependencies(plugin)


def main():
    ordered = []
    visited = set()

    def visit(module):
        if module in visited:
            return
        visited.add(module)
        artifact_id, version = module
        base = f"{CODEGENOME_URL}/{artifact_id}/{version}/{artifact_id}-{version}"
        pom = ElementTree.fromstring(curl("--fail", "--location", f"{base}.pom"))
        for dependency in recipe_dependencies(pom):
            visit(dependency)
        if curl("--output", "/dev/null", "--write-out", "%{http_code}", f"{base}.jar") == "403":
            ordered.append(module)

    for module in sorted({recipe for effective_pom in sys.argv[1:] for recipe in plugin_recipes(effective_pom)}):
        visit(module)
    for artifact_id, version in ordered:
        print(f"{artifact_id}:{version}")


main()
