set shell := ["zsh", "--no-unset", "-c"]
set dotenv-filename := ".envrc"

group_id_with_slashes := "io/liftwizard"

# Setup the project (asdf) and run the default build (mvn)
default: asdf mvn

# asdf install
asdf:
    asdf plugin-add java
    asdf plugin-add maven
    asdf plugin-add mvnd https://github.com/joschi/asdf-mvnd
    asdf install
    asdf current

# git clean
_git-clean:
    git clean -fdx release.properties '*/pom.xml.releaseBackup' pom.xml.releaseBackup target '*/target' '*/*/target'

# rm -rf ~/.m2/repository/...
_clean-m2:
    #!/usr/bin/env zsh
    set -uo pipefail
    rm -rf ~/.m2/repository/{{group_id_with_slashes}}/**/*-SNAPSHOT
    exit 0

# clean (maven and git)
clean MVN="mvnd": && _git-clean _clean-m2
    {{MVN}} clean

# mvn verify
verify MVN="mvnd":
    {{MVN}} verify

# mvn enforcer
enforcer MVN="mvnd":
    {{MVN}} verify -DskipTests --activate-profiles maven-enforcer-plugin

# mvn dependency
analyze MVN="mvnd":
    {{MVN}} verify -DskipTests --activate-profiles maven-dependency-plugin

# mvn javadoc
javadoc MVN="mvnd":
    {{MVN}} verify -DskipTests --activate-profiles maven-javadoc-plugin

# mvn checkstyle
checkstyle MVN="mvnd":
    {{MVN}} verify -DskipTests --activate-profiles checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict

# mvn reproducible
reproducible MVN="mvnd":
    {{MVN}} verify -DskipTests artifact:check-buildplan

# mvn rewrite
rewrite-dry-run MVN="mvnd":
    {{MVN}} install -DskipTests org.openrewrite.maven:rewrite-maven-plugin:dryRun --projects '!liftwizard-example' --activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun

# mvn rewrite
rewrite RECIPE:
    mvn --threads 1 -U -DskipTests org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.activeRecipes={{RECIPE}}

# mvn display updates (dependencies, plugins, properties)
display-updates:
    mvn --threads 1 versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates

# mvn dependency:tree
dependency-tree:
    mvn --threads 1 dependency:tree

# mvn buildplan-list
buildplan-list:
    mvn --threads 1 buildplan:list

# mvn buildplan-list-phase
buildplan-list-phase:
    mvn --threads 1 buildplan:list-phase

# mvn wrapper:wrapper
wrapper VERSION:
    mvn --threads 1 wrapper:wrapper -Dmaven=VERSION

# mvn release:prepare
release NEXT_VERSION: && _git-clean
    mvn --batch-mode clean release:clean release:prepare -DdevelopmentVersion={{NEXT_VERSION}}

# Count lines of code
scc:
    scc **/src/{main,test}

echo_command := env('ECHO_COMMAND', "echo")

# Fail if there are local modifications or untracked files
_check-local-modifications:
    #!/usr/bin/env zsh
    set -uo pipefail

    git diff --quiet
    EXIT_CODE=$?
    if [ $EXIT_CODE -ne 0 ]; then
        {{echo_command}} "Locally modified files" &
        exit $EXIT_CODE
    fi

    git status --porcelain | (! grep -q '^??')
    EXIT_CODE=$?
    if [ $EXIT_CODE -ne 0 ]; then
        {{echo_command}} "Untracked files" &
        exit $EXIT_CODE
    fi

default_target := env('MVN_TARGET', "verify")
default_profiles := env('MVN_PROFILES', "--activate-profiles maven-enforcer-plugin,maven-dependency-plugin,checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict")

# mvn
mvn MVN="mvnd" TARGET=default_target PROFILES=default_profiles *FLAGS="--threads 2C":
    #!/usr/bin/env zsh
    set -uo pipefail

    COMMIT_MESSAGE=$(git log --format=%B -n 1 HEAD)
    SKIPPABLE_WORDS=("skip" "pass" "stop" "fail")

    for word in "${SKIPPABLE_WORDS[@]}"; do
        if [[ $COMMIT_MESSAGE == *\[${word}\]* ]]; then
            echo "Skipping due to [${word}] in commit: '$COMMIT_MESSAGE'"
            exit 0
        fi
    done

    {{MVN}} {{FLAGS}} {{TARGET}} {{PROFILES}}

    EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        exit 0
    fi

    MESSAGE="Failed on commit: '$COMMIT_MESSAGE' with exit code $EXIT_CODE"
    {{echo_command}} "$MESSAGE" &
    exit $EXIT_CODE

# end-to-end test for git-test
test: _check-local-modifications clean mvn

upstream_remote := env('UPSTREAM_REMOTE', "upstream")
upstream_branch := env('UPSTREAM_BRANCH', "main")
fail_fast := env('FAIL_FAST', "false")

# `just test` all commits with configurable upstream/main as ancestor
test-all *FLAGS="--retest":
    #!/usr/bin/env zsh
    set -uo pipefail

    if [ "{{fail_fast}}" ]; then
        set -Ee
    fi

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --contains {{upstream_remote}}/{{upstream_branch}}))

    for branch in "${branches[@]}"
    do
        echo "Testing branch: $branch"
        git test run {{FLAGS}} {{upstream_remote}}/{{upstream_branch}}..${branch}
    done

# `just test results` all branches with configurable upstream/main as ancestor
test-results:
    #!/usr/bin/env zsh
    set -uo pipefail

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --contains {{upstream_remote}}/{{upstream_branch}}))

    for branch in "${branches[@]}"
    do
        echo "Branch: $branch"
        git test results {{upstream_remote}}/{{upstream_branch}}..${branch}
    done

# Rebase all branches onto configurable upstream/main
rebase-all:
    #!/usr/bin/env zsh
    set -Eeuo pipefail

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --no-contains {{upstream_remote}}/{{upstream_branch}}))
    for branch in "${branches[@]}"
    do
        echo "Rebasing branch: $branch"
        git checkout "$branch"
        git pull {{upstream_remote}} {{upstream_branch}} --rebase
    done

# git absorb into configurable upstream/main
absorb:
    git absorb --base {{upstream_remote}}/{{upstream_branch}} --force

# git rebase onto configurable upstream/main
rebase:
    git fetch {{upstream_remote}} {{upstream_branch}}
    git rebase --interactive --autosquash {{upstream_remote}}/{{upstream_branch}}

# Delete branches from origin merged into configurable upstream/main
delete-merged:
    git branch --all --merged remotes/{{upstream_remote}}/{{upstream_branch}} | grep --invert-match {{upstream_branch}} | grep --invert-match HEAD | grep "remotes/origin/" | cut -d "/" -f 3- | xargs git push --delete origin
