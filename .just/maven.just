default_mvn      := env('MVN_BINARY',   "mvnd")
default_target   := env('MVN_TARGET',   "verify")
default_flags    := env('MVN_FLAGS',    "--threads 2C")
skip_tests_flags := default_flags + " -DskipTests"
default_profiles := env('MVN_PROFILES', "--activate-profiles maven-enforcer-plugin,maven-dependency-plugin,maven-javadoc-plugin,checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict,checkstyle-formatting-strict,spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-sql,spotless-pom,spotless-markdown,spotless-json,spotless-yaml")

# clean ~/.m2 with `rm -rf ~/.m2/repository/...`
_clean-m2:
    #!/usr/bin/env bash
    set -uo pipefail
    just _run "rm -rf ~/.m2/repository/{{ANSI_YELLOW}}{{group_id_with_slashes}}{{ANSI_DEFAULT}}/**/*-SNAPSHOT"
    exit 0

# mvn clean
@_clean-maven MVN=default_mvn *FLAGS=default_flags: && (mvn MVN "clean" "" FLAGS)
    just _run "git clean --quiet --force -dx release.properties **/pom.xml.releaseBackup **/target"

# mvn verify
compile MVN=default_mvn: (mvn MVN "verify" "" skip_tests_flags) && _check-local-modifications

# mvn verify
test MVN=default_mvn: (mvn MVN "verify" "" default_flags) && _check-local-modifications

# mvn install
install MVN=default_mvn: (mvn MVN "install" "" skip_tests_flags) && _check-local-modifications

# mvn enforcer
enforcer MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-enforcer-plugin" skip_tests_flags) && _check-local-modifications

# mvn dependency
dependency MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-dependency-plugin" skip_tests_flags) && _check-local-modifications

# mvn javadoc
javadoc MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-javadoc-plugin" skip_tests_flags) && _check-local-modifications

checkstyle-semantics MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics" default_flags) && _check-local-modifications
checkstyle-formatting MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting" default_flags) && _check-local-modifications
checkstyle-semantics-strict MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics-strict" default_flags) && _check-local-modifications
checkstyle-formatting-strict MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting-strict" default_flags) && _check-local-modifications
checkstyle: checkstyle-semantics checkstyle-formatting checkstyle-semantics-strict checkstyle-formatting-strict

# mvn reproducible
reproducible MVN=default_mvn: (mvn MVN "verify artifact:check-buildplan" "" skip_tests_flags) && _check-local-modifications

# mvn rewrite
@rewrite RECIPE: && _check-local-modifications
    just _run "mvn {{ANSI_MAGENTA}}--threads 1 -U -DskipTests{{ANSI_DEFAULT}} org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes={{ANSI_YELLOW}}{{RECIPE}}{{ANSI_DEFAULT}}"

# mvn display updates (dependencies, plugins, properties)
@display-updates:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates"

# mvn dependency:tree
@dependency-tree:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} dependency:tree"

# mvn buildplan-list
@buildplan-list:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} buildplan:list"

# mvn buildplan-list-phase
@buildplan-list-phase:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} buildplan:list-phase"

# mvn wrapper:wrapper
@wrapper VERSION:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} wrapper:wrapper -Dmaven={{ANSI_YELLOW}}{{VERSION}}{{ANSI_DEFAULT}}"

# mvn release:prepare
release: clean _check-local-modifications && _clean-git _check-local-modifications
    #!/usr/bin/env bash
    set -Eeuo pipefail

    CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

    if [[ $CURRENT_VERSION =~ ([0-9]+)\.([0-9]+)\.([0-9]+)-SNAPSHOT ]]; then
        MAJOR="${BASH_REMATCH[1]}"
        MINOR="${BASH_REMATCH[2]}"
        PATCH="${BASH_REMATCH[3]}"
        NEXT_VERSION="$MAJOR.$((MINOR + 1)).0-SNAPSHOT"
    else
        NEXT_VERSION="$CURRENT_VERSION"
    fi

    echo "Current version is: {{ANSI_YELLOW}}$CURRENT_VERSION{{ANSI_DEFAULT}}"

    read -p "Enter next version (default: {{ANSI_YELLOW}}$NEXT_VERSION{{ANSI_DEFAULT}}): " INPUT_VERSION
    NEXT_VERSION=${INPUT_VERSION:-$NEXT_VERSION}

    just _run "git checkout {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_NORMAL}}"

    just _run "mvn {{ANSI_MAGENTA}}--batch-mode{{ANSI_DEFAULT}} clean release:clean release:prepare -DdevelopmentVersion={{ANSI_YELLOW}}$NEXT_VERSION{{ANSI_DEFAULT}}"
