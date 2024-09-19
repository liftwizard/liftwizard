set shell := ["bash", "-O", "globstar", "-c"]
set dotenv-filename := ".envrc"

group_id_with_slashes := "io/liftwizard"

# Setup the project (mise) and run the default build (mvn)
default: mise mvn

# set up git-test
setup-git-test:
    git test add --forget --test default        'just default'
    git test add --forget --test enforcer       'just enforcer'
    git test add --forget --test dependency     'just dependency'
    git test add --forget --test checkstyle     'just checkstyle'
    git test add --forget --test javadoc        'just javadoc'
    git test add --forget --test formats        'just spotless formats'
    git test add --forget --test prettier       'just spotless prettier-java'
    git test add --forget --test gjf            'just spotless google-java-format'
    git test add --forget --test sort-imports   'just spotless java-sort-imports'
    git test add --forget --test unused-imports 'just spotless java-unused-imports'
    git test add --forget --test cleanthat      'just spotless java-cleanthat'
    git test add --forget --test pom            'just spotless pom'
    git test add --forget --test markdown       'just spotless markdown'
    git test add --forget --test json           'just spotless json'
    git test add --forget --test yaml           'just spotless yaml'
    git test add --forget --test sql            'just spotless sql'

# Add git refspec to fetch GitHub PR refs from REMOTE
setup-github-refspec REMOTE:
    #!/usr/bin/env bash
    set -Eeuo pipefail

    if git remote get-url {{REMOTE}} &> /dev/null && git remote get-url {{REMOTE}} | grep -q 'github.com'; then
        git config --add remote.{{REMOTE}}.fetch '+refs/pull/*/head:refs/remotes/{{REMOTE}}/pr/*'
        git config --add remote.{{REMOTE}}.fetch '+refs/pull/*/merge:refs/remotes/{{REMOTE}}/pr/merge/*'
        echo "Added refspec to fetch GitHub PR refs for {{REMOTE}}"
    else
        echo "Remote {{REMOTE}} is not a GitHub remote"
    fi

# Add git refspec to fetch GitHub PR refs from origin and upstream
setup-github-refspecs: (setup-github-refspec "origin") (setup-github-refspec "upstream")

# mise install
mise:
    mise plugin install maven
    mise plugin install mvnd https://github.com/joschi/asdf-mvnd
    mise install
    mise current

# git clean
_clean-git:
    git clean -fdx release.properties **/pom.xml.releaseBackup **/target

# rm -rf ~/.m2/repository/...
_clean-m2:
    #!/usr/bin/env bash
    set -uo pipefail
    rm -rf ~/.m2/repository/{{group_id_with_slashes}}/**/*-SNAPSHOT
    exit 0

default_mvn      := env('MVN_BINARY',   "mvnd")
# clean (maven and git)
clean MVN=default_mvn: && _clean-git _clean-m2
    {{MVN}} clean

# mvn verify
verify MVN=default_mvn:
    {{MVN}} verify

# mvn install
install MVN=default_mvn:
    {{MVN}} install

default_target   := env('MVN_TARGET',   "verify")
default_flags    := env('MVN_FLAGS',    "--threads 2C")

skip_tests_flags := default_flags + " -DskipTests"

# mvn enforcer
enforcer MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "--activate-profiles maven-enforcer-plugin" skip_tests_flags) && _check-local-modifications

# mvn dependency
dependency MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "--activate-profiles maven-dependency-plugin" skip_tests_flags) && _check-local-modifications

# mvn javadoc
javadoc MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "--activate-profiles maven-dependency-plugin" skip_tests_flags) && _check-local-modifications

checkstyle-semantics MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics" default_flags) && _check-local-modifications
checkstyle-formatting MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting" default_flags) && _check-local-modifications
checkstyle-semantics-strict MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics-strict" default_flags) && _check-local-modifications
checkstyle-formatting-strict MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting-strict" default_flags) && _check-local-modifications
checkstyle: checkstyle-semantics checkstyle-formatting checkstyle-semantics-strict checkstyle-formatting-strict

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

# mvn reproducible
reproducible MVN=default_mvn:
    {{MVN}} verify -DskipTests artifact:check-buildplan

# mvn rewrite
rewrite-dry-run MVN=default_mvn:
    mvn --threads 1 install -DskipTests org.openrewrite.maven:rewrite-maven-plugin:dryRun --projects '!liftwizard-example' --activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun

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

upstream_remote := env('UPSTREAM_REMOTE', "upstream")
upstream_branch := env('UPSTREAM_BRANCH', "main")

# mvn release:prepare
release NEXT_VERSION: && _clean-git
    git checkout {{upstream_remote}}/{{upstream_branch}}
    mvn --batch-mode clean release:clean release:prepare -DdevelopmentVersion={{NEXT_VERSION}}

# Count lines of code
scc:
    scc **/src/{main,test}

# Override this with a command called `woof` which notifies you in whatever ways you prefer.
# My `woof` command uses `echo`, `say`, and sends a Pushover notification.
echo_command := env('ECHO_COMMAND', "echo")

# Fail if there are local modifications or untracked files
_check-local-modifications:
    #!/usr/bin/env bash
    set -uo pipefail

    git diff --quiet
    EXIT_CODE=$?
    if [ $EXIT_CODE -ne 0 ]; then
        {{echo_command}} "Locally modified files"
        exit $EXIT_CODE
    fi

    git status --porcelain | (! grep -q '^??')
    EXIT_CODE=$?
    if [ $EXIT_CODE -ne 0 ]; then
        {{echo_command}} "Untracked files"
        exit $EXIT_CODE
    fi

default_profiles := env('MVN_PROFILES', "--activate-profiles maven-enforcer-plugin,maven-dependency-plugin,checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict,spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-pom,spotless-markdown,spotless-json,spotless-yaml")

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

fail_fast := env('FAIL_FAST', "false")

# git-test on the range of commits between a configurable upstream/main and {{BRANCH}}
test-branch BRANCH="HEAD" TEST="default" *FLAGS="--retest":
    echo "Testing branch: {{BRANCH}}"
    git test run --test {{TEST}} {{FLAGS}} {{upstream_remote}}/{{upstream_branch}}..{{BRANCH}}

# `just test` all commits with configurable upstream/main as ancestor
test-all TEST="default" *FLAGS="--retest":
    #!/usr/bin/env bash
    set -uo pipefail

    if [ "{{fail_fast}}" ]; then
        set -Ee
    fi

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --contains {{upstream_remote}}/{{upstream_branch}}))

    for branch in "${branches[@]}"
    do
        just test-branch "${branch}" "{{TEST}}" {{FLAGS}}
    done

alias ta := test-all

# `just test results` all branches with configurable upstream/main as ancestor
test-results:
    #!/usr/bin/env bash
    set -uo pipefail

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --contains {{upstream_remote}}/{{upstream_branch}}))

    for branch in "${branches[@]}"
    do
        echo "Branch: $branch"
        git test results --color {{upstream_remote}}/{{upstream_branch}}..${branch}
    done

offline := env_var_or_default('OFFLINE', 'false')

# git fetch configurable upstream
fetch:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    if [ "{{offline}}" != "true" ]; then
        git fetch --all --prune --jobs=16
    fi

# Rebase all branches onto configurable upstream/main
rebase-all: _check-local-modifications fetch
    #!/usr/bin/env bash
    set -Eeuo pipefail

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --no-contains {{upstream_remote}}/{{upstream_branch}}))
    for branch in "${branches[@]}"
    do
        included_count=$(git branch --contains "$branch" | wc -l)
        if [ "$included_count" -gt 1 ]; then
            echo "Skipping branch $branch as it is included in other branches"
            continue
        fi

        echo "Rebasing branch: $branch"
        git checkout "$branch"
        git rebase {{upstream_remote}}/{{upstream_branch}}
    done

alias ra := rebase-all

# git absorb into configurable upstream/main
absorb:
    git absorb \
        --base {{upstream_remote}}/{{upstream_branch}} \
        --force

# git rebase onto configurable upstream/main
rebase: _check-local-modifications fetch
    git rebase --interactive --autosquash --rebase-merges {{upstream_remote}}/{{upstream_branch}}

# Delete local branches merged into configurable upstream/main
delete-local-merged: fetch
    git branch --merged remotes/{{upstream_remote}}/{{upstream_branch}} \
        | grep -v "^\*" \
        | xargs git branch -D

# Delete branches from origin merged into configurable upstream/main
delete-remote-merged: fetch
    #!/usr/bin/env bash
    set -Eeu
    if [ "{{offline}}" != "true" ]; then
        git branch --remote --list 'origin/*' --merged remotes/{{upstream_remote}}/{{upstream_branch}} \
            | grep --invert-match {{upstream_branch}} \
            | grep --invert-match HEAD \
            | grep "origin/" \
            | grep --invert-match "origin/pr/" \
            | cut -d "/" -f 2- \
            | xargs git push --delete origin
    else
        echo "Skipping delete-remote-merged in offline mode"
    fi

# Delete local and remote branches that are merged into configurable upstream/main
delete-merged: delete-local-merged delete-remote-merged

git: rebase-all delete-merged

qodana:
    op run -- qodana scan \
        --apply-fixes \
        --linter jetbrains/qodana-jvm:2024.1

pull-request-description:
    git log {{upstream_remote}}/{{upstream_branch}}..HEAD --reverse --format='- %s'
