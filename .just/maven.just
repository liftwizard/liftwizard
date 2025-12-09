default_mvn      := env('MVN_BINARY',   "mvn")
default_target   := env('MVN_TARGET',   "verify")
default_flags    := env('MVN_FLAGS',    "--threads 2C")
skip_tests_flags := default_flags + " -DskipTests"

default_verify_profiles := env('MVN_VERIFY_PROFILES', "spotless-preserve-cache,errorprone-strict,maven-enforcer-plugin,maven-dependency-plugin,maven-javadoc-plugin")
default_checkstyle_profiles := env('MVN_CHECKSTYLE_PROFILES', "checkstyle-semantics,checkstyle-semantics-strict")
default_spotless_profiles := env('MVN_SPOTLESS_PROFILES', "spotless-apply,spotless-formats,spotless-java,spotless-prettier-java-sort-imports,spotless-java-cleanthat,spotless-sql,spotless-pom")
default_profiles := env('MVN_PROFILES', "--activate-profiles " + default_verify_profiles + "," + default_checkstyle_profiles + "," + default_spotless_profiles)

# Run `mvn` with configurable target, profiles, and flags
mvn MVN=default_mvn TARGET=default_target PROFILES=default_profiles *FLAGS=default_flags:
    #!/usr/bin/env bash
    set -uo pipefail

    if command -v should-skip-commit >/dev/null 2>&1 && should-skip-commit HEAD; then
        exit 0
    fi

    # Set colors based on whether values match defaults
    if [ "{{MVN}}" = "{{default_mvn}}" ]; then MVN_COLOR="{{ANSI_GRAY}}"; else MVN_COLOR="{{ANSI_MAGENTA}}"; fi
    if [ "{{TARGET}}" = "{{default_target}}" ]; then TARGET_COLOR="{{ANSI_GRAY}}"; else TARGET_COLOR="{{ANSI_GREEN}}"; fi
    if [ "{{PROFILES}}" = "{{default_profiles}}" ]; then PROFILES_COLOR="{{ANSI_GRAY}}"; else PROFILES_COLOR="{{ANSI_BLUE}}"; fi
    if [ "{{FLAGS}}" = "{{default_flags}}" ]; then FLAGS_COLOR="{{ANSI_GRAY}}"; else FLAGS_COLOR="{{ANSI_MAGENTA}}"; fi

    just _run "${MVN_COLOR}{{MVN}}{{ANSI_DEFAULT}} ${FLAGS_COLOR}{{FLAGS}}{{ANSI_DEFAULT}} {{ANSI_GREEN}}install{{ANSI_DEFAULT}} --projects liftwizard-utility/liftwizard-checkstyle,liftwizard-utility/liftwizard-rewrite"

    just _run "${MVN_COLOR}{{MVN}}{{ANSI_DEFAULT}} ${TARGET_COLOR}{{TARGET}}{{ANSI_DEFAULT}} ${FLAGS_COLOR}{{FLAGS}}{{ANSI_DEFAULT}} ${PROFILES_COLOR}{{PROFILES}}{{ANSI_DEFAULT}}"

    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        exit 0
    fi

    DIRECTORY=$(basename $(pwd))
    COMMIT_MESSAGE=$(git log --format=%B -n 1 HEAD)

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
@_clean-maven MVN=default_mvn *FLAGS=default_flags: && (mvn MVN "clean" "--activate-profiles spotless-preserve-cache" FLAGS)
    just _run "git clean --quiet --force -dx release.properties **/pom.xml.releaseBackup"

# `mvn verify` without tests
compile MVN=default_mvn: (mvn MVN "verify" "" skip_tests_flags)

# `mvn verify` with tests
test MVN=default_mvn *FLAGS="": (mvn MVN "verify" "" (default_flags + " " + FLAGS))

# `mvn verify` with tests and rerecord profile
test-rerecord MVN=default_mvn: (mvn MVN "verify" "--activate-profiles rerecord" default_flags)

# `mvn install`
install MVN=default_mvn: (mvn MVN "install" "" skip_tests_flags)

# `mvn` with enforcer plugin
enforcer MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-enforcer-plugin" skip_tests_flags)

# `mvn` with dependency plugin
dependency MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-dependency-plugin" skip_tests_flags)

# `mvn` with javadoc plugin
javadoc MVN=default_mvn: (mvn MVN "verify" "--activate-profiles maven-javadoc-plugin" skip_tests_flags)

# `mvn` with jacoco plugin for code coverage
coverage MVN=default_mvn: (mvn MVN "verify" "--activate-profiles jacoco-maven-plugin" default_flags)

# `mvn` with errorprone
errorprone MVN=default_mvn: (mvn MVN "verify" "--activate-profiles errorprone" skip_tests_flags)

# `mvn` with errorprone-strict
errorprone-strict MVN=default_mvn: (mvn MVN "verify" "--activate-profiles errorprone-strict" skip_tests_flags)

# `mvn` with errorprone-patch to generate auto-fixes
errorprone-patch MVN=default_mvn: (mvn MVN "verify" "--activate-profiles errorprone-patch" skip_tests_flags)

# `mvn` with spotbugs
spotbugs MVN=default_mvn: (mvn MVN "verify" "--activate-profiles spotbugs-maven-plugin" skip_tests_flags)

# `mvn checkstyle:check` with the given profile
checkstyle-profile PROFILE FAIL_ON_VIOLATION="true" MVN="mvn": (mvn MVN "checkstyle:check" ("--activate-profiles " + PROFILE) (default_flags + " -Dcheckstyle.failOnViolation=" + FAIL_ON_VIOLATION))

# Run checkstyle with all profiles
checkstyle FAIL_ON_VIOLATION="true" MVN="mvn": \
    (checkstyle-profile "checkstyle-semantics" FAIL_ON_VIOLATION MVN) \
    (checkstyle-profile "checkstyle-formatting" FAIL_ON_VIOLATION MVN) \
    (checkstyle-profile "checkstyle-semantics-strict" FAIL_ON_VIOLATION MVN) \
    (checkstyle-profile "checkstyle-formatting-strict" FAIL_ON_VIOLATION MVN)

# spotless
spotless NAME MVN=default_mvn: (mvn MVN "spotless:apply" ("--projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle,!liftwizard-utility/liftwizard-rewrite' --activate-profiles spotless-apply,spotless-" + NAME) default_flags)

# spotless-all
spotless-all MVN=default_mvn: (mvn MVN "spotless:apply" ("--projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle,!liftwizard-utility/liftwizard-rewrite' --activate-profiles " + default_spotless_profiles) default_flags)

# `mvn` with reproducible build check
reproducible MVN=default_mvn: (mvn MVN "verify artifact:check-buildplan" "" skip_tests_flags)

# `mvn` rewrite dry run
rewrite-dry-run MVN=default_mvn: (mvn MVN "install org.openrewrite.maven:rewrite-maven-plugin:dryRun" "--activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun" skip_tests_flags)

# `mvn` rewrite run
rewrite-run MVN=default_mvn: clean (mvn MVN "install org.openrewrite.maven:rewrite-maven-plugin:run" "--activate-profiles rewrite-maven-plugin" skip_tests_flags)

# `mvn` with rewrite-maven-plugin for single recipe
rewrite RECIPE MVN=default_mvn: (mvn MVN "install org.openrewrite.maven:rewrite-maven-plugin:run --projects '!liftwizard-example'" ("--activate-profiles rewrite-maven-plugin -Drewrite.activeRecipes=" + RECIPE) skip_tests_flags)

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

# Trigger GitHub Actions release workflow
[group('release')]
release:
    #!/usr/bin/env bash
    set -Eeuo pipefail

    just _run "git checkout {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_NORMAL}}"

    current_branch=$(git rev-parse --abbrev-ref HEAD)
    if [ "$current_branch" = "HEAD" ]; then
        current_ref="detached at $(git rev-parse --short HEAD)"
    else
        current_ref="$current_branch"
    fi
    if [ "$current_branch" != "{{upstream_branch}}" ]; then
        echo "⚠️  Not on {{ANSI_YELLOW}}{{upstream_branch}}{{ANSI_DEFAULT}} branch (currently on {{ANSI_RED}}${current_ref}{{ANSI_DEFAULT}})"
        read -p "Continue anyway? [y/N] " -n 1 -r
        echo
        [[ $REPLY =~ ^[Yy]$ ]] || exit 1
    fi
    local_sha=$(git rev-parse HEAD)
    remote_sha=$(git rev-parse {{upstream_remote}}/{{upstream_branch}})
    if [ "$local_sha" != "$remote_sha" ]; then
        echo "⚠️  Local {{ANSI_YELLOW}}{{upstream_branch}}{{ANSI_DEFAULT}} is not up to date with {{ANSI_YELLOW}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_DEFAULT}}"
        echo "   Local:  ${local_sha}"
        echo "   Remote: ${remote_sha}"
        read -p "Continue anyway? [y/N] " -n 1 -r
        echo
        [[ $REPLY =~ ^[Yy]$ ]] || exit 1
    fi

    CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

    if [[ $CURRENT_VERSION =~ ([0-9]+)\.([0-9]+)\.([0-9]+)-SNAPSHOT ]]; then
        MAJOR="${BASH_REMATCH[1]}"
        MINOR="${BASH_REMATCH[2]}"
        PATCH="${BASH_REMATCH[3]}"
        RELEASE_VERSION="$MAJOR.$MINOR.$PATCH"
        NEXT_VERSION="$MAJOR.$MINOR.$((PATCH + 1))-SNAPSHOT"
    else
        echo "❌ Could not parse version: ${CURRENT_VERSION}"
        exit 1
    fi

    echo "📦 Current version: {{ANSI_YELLOW}}${CURRENT_VERSION}{{ANSI_DEFAULT}}"
    echo "🏷️  Release version: {{ANSI_GREEN}}${RELEASE_VERSION}{{ANSI_DEFAULT}}"

    read -p "Enter next version (default: {{ANSI_YELLOW}}$NEXT_VERSION{{ANSI_DEFAULT}}): " INPUT_VERSION
    NEXT_VERSION=${INPUT_VERSION:-$NEXT_VERSION}

    just _run "gh workflow run {{ANSI_YELLOW}}release.yml{{ANSI_DEFAULT}} --field next-version={{ANSI_YELLOW}}${NEXT_VERSION}{{ANSI_DEFAULT}}"
    just _run "sleep {{ANSI_YELLOW}}2{{ANSI_DEFAULT}}"
    run_id=$(gh run list --workflow=release.yml --limit 1 --json databaseId --jq '.[0].databaseId')
    just _run "gh run watch {{ANSI_YELLOW}}${run_id}{{ANSI_DEFAULT}}"

# `mvn deploy`
deploy MVN="mvn": (mvn MVN "deploy" "--activate-profiles maven-central-deploy,deploy,maven-javadoc-plugin" "")
