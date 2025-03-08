default_mvn      := env('MVN_BINARY',   "mvnd")
default_target   := env('MVN_TARGET',   "verify")
default_flags    := env('MVN_FLAGS',    "--threads 2C")
skip_tests_flags := default_flags + " -DskipTests"

default_verify_profiles := env('MVN_VERIFY_PROFILES', "maven-enforcer-plugin,maven-dependency-plugin,maven-javadoc-plugin")
default_checkstyle_profiles := env('MVN_CHECKSTYLE_PROFILES', "checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict,checkstyle-formatting-strict")
default_spotless_profiles := env('MVN_SPOTLESS_PROFILES', "spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-sql,spotless-pom,spotless-markdown,spotless-json,spotless-yaml")
default_profiles := env('MVN_PROFILES', "--activate-profiles " + default_verify_profiles + "," + default_checkstyle_profiles + "," + default_spotless_profiles)

# Run `mvn` with configurable target, profiles, and flags
mvn MVN=default_mvn TARGET=default_target PROFILES=default_profiles *FLAGS=default_flags:
    #!/usr/bin/env bash
    set -uo pipefail

    COMMIT_MESSAGE=$(git log --format=%B -n 1 HEAD)
    SKIPPABLE_WORDS=("skip" "pass" "stop" "fail")

    for word in "${SKIPPABLE_WORDS[@]}"; do
        if [[ $COMMIT_MESSAGE == *\[${word}\]* ]]; then
            echo "Skipping due to [{{ANSI_YELLOW}}${word}{{ANSI_DEFAULT}}] in commit: '${COMMIT_MESSAGE}'"
            exit 0
        fi
    done

    # Set colors based on whether values match defaults
    if [ "{{MVN}}" = "{{default_mvn}}" ]; then MVN_COLOR="{{ANSI_GRAY}}"; else MVN_COLOR="{{ANSI_MAGENTA}}"; fi
    if [ "{{TARGET}}" = "{{default_target}}" ]; then TARGET_COLOR="{{ANSI_GRAY}}"; else TARGET_COLOR="{{ANSI_GREEN}}"; fi
    if [ "{{PROFILES}}" = "{{default_profiles}}" ]; then PROFILES_COLOR="{{ANSI_GRAY}}"; else PROFILES_COLOR="{{ANSI_BLUE}}"; fi
    if [ "{{FLAGS}}" = "{{default_flags}}" ]; then FLAGS_COLOR="{{ANSI_GRAY}}"; else FLAGS_COLOR="{{ANSI_MAGENTA}}"; fi

    just _run "${MVN_COLOR}{{MVN}}{{ANSI_DEFAULT}} ${FLAGS_COLOR}{{FLAGS}}{{ANSI_DEFAULT}} {{ANSI_GREEN}}install{{ANSI_DEFAULT}} --projects liftwizard-utility/liftwizard-checkstyle"

    just _run "${MVN_COLOR}{{MVN}}{{ANSI_DEFAULT}} ${TARGET_COLOR}{{TARGET}}{{ANSI_DEFAULT}} ${FLAGS_COLOR}{{FLAGS}}{{ANSI_DEFAULT}} ${PROFILES_COLOR}{{PROFILES}}{{ANSI_DEFAULT}}"

    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        exit 0
    fi

    DIRECTORY=$(basename $(pwd))

    MESSAGE="Failed in directory ${DIRECTORY} on commit: '${COMMIT_MESSAGE}' with exit code ${EXIT_CODE}"
    {{echo_command}} "$MESSAGE"
    exit $EXIT_CODE

# clean ~/.m2 with `rm -rf ~/.m2/repository/...`
_clean-m2:
    #!/usr/bin/env bash
    set -uo pipefail
    just _run "rm -rf ~/.m2/repository/{{ANSI_YELLOW}}{{group_id_with_slashes}}{{ANSI_DEFAULT}}/**/*-SNAPSHOT"
    exit 0

# `mvn clean`
@_clean-maven MVN=default_mvn *FLAGS=default_flags: && (mvn MVN "clean" "" FLAGS)
    just _run "git clean --quiet --force -dx release.properties **/pom.xml.releaseBackup **/target"

# `mvn verify` without tests
compile MVN=default_mvn: (mvn MVN "verify" "" skip_tests_flags) && _check-local-modifications

# `mvn verify` with tests
test MVN=default_mvn: (mvn MVN "verify" "" default_flags) && _check-local-modifications

# `mvn install`
install MVN=default_mvn: (mvn MVN "install" "" skip_tests_flags) && _check-local-modifications

# `mvn` with enforcer plugin
enforcer MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-enforcer-plugin" skip_tests_flags) && _check-local-modifications

# `mvn` with dependency plugin
dependency MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-dependency-plugin" skip_tests_flags) && _check-local-modifications

# `mvn` with javadoc plugin
javadoc MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-javadoc-plugin" skip_tests_flags) && _check-local-modifications

# `mvn` with errorprone
errorprone MVN=default_mvn: (mvn MVN "verify" "--activate-profiles errorprone" skip_tests_flags) && _check-local-modifications

# checksyle with semantics profile
checkstyle-semantics MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics" default_flags) && _check-local-modifications

# checksyle with formatting profile
checkstyle-formatting MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting" default_flags) && _check-local-modifications

# checksyle with semantics-strict profile
checkstyle-semantics-strict MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics-strict" default_flags) && _check-local-modifications

# checksyle with formatting-strict profile
checkstyle-formatting-strict MVN="mvn": (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting-strict" default_flags) && _check-local-modifications

# checksyle with all profiles
checkstyle: checkstyle-semantics checkstyle-formatting checkstyle-semantics-strict checkstyle-formatting-strict

# spotless
spotless NAME MVN=default_mvn: _check-local-modifications clean (mvn MVN "spotless:apply" ("--projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle' --activate-profiles spotless-apply,spotless-" + NAME) default_flags) && _check-local-modifications

# spotless-all
spotless-all MVN=default_mvn: _check-local-modifications clean (mvn MVN "spotless:apply" "--projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle' --activate-profiles spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-sql,spotless-pom,spotless-markdown,spotless-json,spotless-yaml" default_flags) && _check-local-modifications

# `mvn` with reproducible build check
reproducible MVN=default_mvn: (mvn MVN "verify artifact:check-buildplan" "" skip_tests_flags) && _check-local-modifications

# `mvn` rewrite
rewrite-dry-run MVN=default_mvn: _check-local-modifications clean (mvn MVN "install org.openrewrite.maven:rewrite-maven-plugin:dryRun --projects '!liftwizard-example'" "--activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun" skip_tests_flags) && _check-local-modifications

# `mvn` with rewrite-maven-plugin for single recipe
rewrite RECIPE MVN=default_mvn: _check-local-modifications clean (mvn MVN "install org.openrewrite.maven:rewrite-maven-plugin:run --projects '!liftwizard-example'" ("--activate-profiles rewrite-maven-plugin -Drewrite.activeRecipes=" + RECIPE) skip_tests_flags) && _check-local-modifications

# display available updates (dependencies, plugins, properties)
@display-updates:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates"

# `mvn dependency:tree`
@dependency-tree:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} dependency:tree"

# `mvn buildplan-list`
@buildplan-list:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} buildplan:list"

# `mvn buildplan-list-phase`
@buildplan-list-phase:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} buildplan:list-phase"

# `mvn wrapper:wrapper`
@wrapper VERSION:
    just _run "mvn {{ANSI_MAGENTA}}--threads 1{{ANSI_DEFAULT}} wrapper:wrapper -Dmaven={{ANSI_YELLOW}}{{VERSION}}{{ANSI_DEFAULT}}"

# `mvn release:prepare`
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
