set shell := ["bash", "-O", "globstar", "-c"]
set dotenv-filename := ".envrc"

group_id_with_slashes := "io/liftwizard"

import ".just/console.just"
import ".just/maven.just"
import ".just/git.just"
import ".just/git-rebase.just"
import ".just/git-test.just"

# Setup the project (mise) and run the default build (mvn)
default: mise mvn

# `mise install`
mise:
    mise install
    mise current

# clean (maven and git)
@clean: _clean-git _clean-maven _clean-m2

# spotless
spotless NAME MVN=default_mvn: _check-local-modifications clean (mvn MVN "spotless:apply" ("--projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle' --activate-profiles spotless-apply,spotless-" + NAME) default_flags) && _check-local-modifications

# spotless-all
spotless-all MVN=default_mvn: _check-local-modifications clean (mvn MVN "spotless:apply" "--projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle' --activate-profiles spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-sql,spotless-pom,spotless-markdown,spotless-json,spotless-yaml" default_flags) && _check-local-modifications

markdownlint:
    npx markdownlint-cli --config .markdownlint.jsonc  --fix .

# mvn rewrite
@rewrite-dry-run MVN=default_mvn:
    just _run "{{MVN}} {{ANSI_GREEN}}install{{ANSI_DEFAULT}} -DskipTests org.openrewrite.maven:rewrite-maven-plugin:dryRun --projects '!liftwizard-example' {{ANSI_BLUE}}--activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun"

# Count lines of code
scc:
    scc **/src/{main,test}

# Override this with a command called `woof` which notifies you in whatever ways you prefer.
# My `woof` command uses `echo`, `say`, and sends a Pushover notification.
echo_command := env('ECHO_COMMAND', "echo")

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

qodana:
    op run -- qodana scan \
        --apply-fixes \
        --linter jetbrains/qodana-jvm:2024.1
