set shell := ["bash", "-O", "globstar", "-c"]
set dotenv-filename := ".envrc"

group_id_with_slashes := "io/liftwizard"

import ".just/maven.just"
import ".just/git.just"
import ".just/git-rebase.just"
import ".just/git-test.just"

# Setup the project (mise) and run the default build (mvn)
default: mise mvn

# mise install
mise:
    mise plugin install maven
    mise plugin install mvnd https://github.com/joschi/asdf-mvnd
    mise install
    mise current

# clean (maven and git)
clean: _clean-git _clean-maven _clean-m2

# spotless
spotless NAME MVN=default_mvn: _check-local-modifications clean && _check-local-modifications
    {{MVN}} spotless:apply \
      --projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle' \
      --activate-profiles 'spotless-apply,spotless-{{NAME}}'

# spotless-all
spotless-all MVN=default_mvn: _check-local-modifications clean && _check-local-modifications
    {{MVN}} spotless:apply \
      --projects '!liftwizard-maven-build/liftwizard-minimal-parent,!liftwizard-utility/liftwizard-checkstyle' \
      --activate-profiles 'spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-pom,spotless-markdown,spotless-json,spotless-yaml'

markdownlint:
    npx markdownlint-cli --config .markdownlint.jsonc  --fix .

# mvn rewrite
rewrite-dry-run MVN=default_mvn:
    {{MVN}} --threads 1 install -DskipTests org.openrewrite.maven:rewrite-maven-plugin:dryRun --projects '!liftwizard-example' --activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun

# Count lines of code
scc:
    scc **/src/{main,test}

# Override this with a command called `woof` which notifies you in whatever ways you prefer.
# My `woof` command uses `echo`, `say`, and sends a Pushover notification.
echo_command := env('ECHO_COMMAND', "echo")

# mvn
mvn MVN=default_mvn TARGET=default_target PROFILES=default_profiles *FLAGS=default_flags:
    #!/usr/bin/env bash
    set -uo pipefail

    COMMIT_MESSAGE=$(git log --format=%B -n 1 HEAD)
    SKIPPABLE_WORDS=("skip" "pass" "stop" "fail")

    for word in "${SKIPPABLE_WORDS[@]}"; do
        if [[ $COMMIT_MESSAGE == *\[${word}\]* ]]; then
            echo "Skipping due to [${word}] in commit: '$COMMIT_MESSAGE'"
            exit 0
        fi
    done

    {{MVN}} {{FLAGS}} install --projects liftwizard-utility/liftwizard-checkstyle
    {{MVN}} {{FLAGS}} {{TARGET}} {{PROFILES}}

    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        exit 0
    fi

    DIRECTORY=$(basename $(pwd))

    MESSAGE="Failed in directory ${DIRECTORY} on commit: '${COMMIT_MESSAGE}' with exit code ${EXIT_CODE}"
    {{echo_command}} "$MESSAGE"
    exit $EXIT_CODE

# end-to-end test for git-test
test: _check-local-modifications clean mvn && _check-local-modifications

qodana:
    op run -- qodana scan \
        --apply-fixes \
        --linter jetbrains/qodana-jvm:2024.1
