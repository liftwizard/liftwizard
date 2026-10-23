#!/usr/bin/env bash

set -Eeuo pipefail

# Build the source-available OpenRewrite recipe modules from their release tags and install them into ~/.m2. With --list, only print the modules and versions that it would build.
#
# Recipe releases newer than the last ones on Maven Central are only published to the Code Genome Project, and their jars are only downloadable with a Moderne subscription. Their POMs are downloadable with any Code Genome token, so each module is compiled against the dependency versions in its official POM and installed with that POM.
#
# The versions come from the rewrite-maven-plugin dependencies in the effective POM of the project in the working directory, so the POMs are the only place to bump them.
#
# Needs Java, Maven, Python, and mise on the PATH. Credentials come from CODEGENOME_USERNAME and CODEGENOME_TOKEN, or else from the codegenome server in ~/.m2/settings.xml.

CODEGENOME_URL=https://artifacts.codegenomeproject.org/maven/org/openrewrite/recipe
LOCAL_REPOSITORY="$HOME/.m2/repository/org/openrewrite/recipe"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Relative to the working directory rather than this script, because other repositories run it from the action's checkout.
WORK_DIR="$PWD/target/openrewrite-recipes"

if [[ -z "${CODEGENOME_USERNAME:-}" || -z "${CODEGENOME_TOKEN:-}" ]]; then
    eval "$(python3 -I -c '
import os, shlex, xml.etree.ElementTree as ElementTree
root = ElementTree.parse(os.path.expanduser("~/.m2/settings.xml")).getroot()
for server in root.iter():
    if server.tag.endswith("server") and any(e.tag.endswith("id") and e.text == "codegenome" for e in server):
        fields = {e.tag.split("}")[-1]: e.text for e in server}
        print("export CODEGENOME_USERNAME=" + shlex.quote(fields["username"]))
        print("export CODEGENOME_TOKEN=" + shlex.quote(fields["password"]))
')"
fi

if [[ -z "${CODEGENOME_USERNAME:-}" || -z "${CODEGENOME_TOKEN:-}" ]]; then
    echo "Set CODEGENOME_USERNAME and CODEGENOME_TOKEN, or add a codegenome server to ~/.m2/settings.xml" >&2
    exit 1
fi

mkdir -p "$WORK_DIR"

# The rewrite-maven-plugin configuration lives in profiles. Each profile gets its own effective POM because activating them together merges their plugin dependencies, hiding some of them. Maven only warns about a profile that does not exist, and the optional ?profile form does not activate profiles inherited from a parent POM. Selecting the root project with --projects rather than --non-recursive keeps the other modules in the reactor, so a parent can import a BOM that is built in the same reactor. In Liftwizard itself, -DskipLiftwizardRewriteModule leaves out liftwizard-rewrite, which the rewrite profiles add as a plugin dependency of its own parent.
effective_poms=()
for profile in rewrite-maven-plugin rewrite-maven-plugin-dryRun rewrite-maven-plugin-one-off; do
    mvn --batch-mode --quiet --projects . -DskipLiftwizardRewriteModule help:effective-pom \
        --activate-profiles "$profile" \
        -Doutput="$WORK_DIR/effective-pom-$profile.xml"
    effective_poms+=("$WORK_DIR/effective-pom-$profile.xml")
done
resolved="$(python3 -I "$SCRIPT_DIR/resolve-openrewrite-recipes.py" "${effective_poms[@]}")"
RECIPES=()
if [[ -n "$resolved" ]]; then
    mapfile -t RECIPES <<< "$resolved"
fi

if [[ "${1:-}" == "--list" ]]; then
    printf '%s\n' "${RECIPES[@]}"
    exit 0
fi

# Some recipe modules build with a Java 25 toolchain. Installing it with mise does not change any mise configuration.
TOOLCHAIN_JDK=temurin-25.0.4+101.0.LTS
mise install --quiet "java@$TOOLCHAIN_JDK"
TOOLCHAIN_JDK_HOME="$(mise where "java@$TOOLCHAIN_JDK")"

# The recipe modules compile against these language modules, whose newer jars are only downloadable with a Moderne subscription. The Gradle builds only look for them in ~/.m2, so put the last versions on Maven Central there. The version must match subscriptionOnlyVersion in pin-official-pom.init.gradle.kts.
for language in rewrite-javascript rewrite-csharp rewrite-scala rewrite-python rewrite-go; do
    mvn --batch-mode --quiet dependency:get -Dtransitive=false -Dartifact="org.openrewrite:$language:8.90.4"
done

for recipe in "${RECIPES[@]}"; do
    module="${recipe%%:*}"
    version="${recipe##*:}"

    if [[ -f "$LOCAL_REPOSITORY/$module/$version/$module-$version.jar" ]]; then
        echo "$module $version is already installed"
        continue
    fi

    echo "Building $module $version"

    pom="$WORK_DIR/$module-$version.pom"
    # The credentials go through stdin so that they never appear in the process list.
    printf 'user = "%s:%s"\n' "$CODEGENOME_USERNAME" "$CODEGENOME_TOKEN" \
        | curl --silent --show-error --fail --location --config - \
            --output "$pom" "$CODEGENOME_URL/$module/$version/$module-$version.pom"

    checkout="$WORK_DIR/$module-$version"
    if [[ ! -d "$checkout" ]]; then
        git clone --quiet --depth 1 --branch "v$version" "https://github.com/openrewrite/$module.git" "$checkout"
    fi

    # The rewrite-build Gradle plugin only adds the Code Genome repository when these Gradle properties are set.
    (
        cd "$checkout"
        ORG_GRADLE_PROJECT_codegenomeUsername="$CODEGENOME_USERNAME" \
            ORG_GRADLE_PROJECT_codegenomePassword="$CODEGENOME_TOKEN" \
            ./gradlew --no-daemon --quiet \
                --init-script "$SCRIPT_DIR/pin-official-pom.init.gradle.kts" \
                -PofficialPom="$pom" \
                -Prelease.useLastTag=true \
                -Porg.gradle.java.installations.paths="$TOOLCHAIN_JDK_HOME" \
                clean jar
    )

    mvn --batch-mode --quiet install:install-file \
        -Dfile="$checkout/build/libs/$module-$version.jar" \
        -DpomFile="$pom"
done
