upstream_remote := env('UPSTREAM_REMOTE', "upstream")
upstream_branch := env('UPSTREAM_BRANCH', "main")
offline := env_var_or_default('OFFLINE', 'false')

# Add git refspec to fetch GitHub PR refs from REMOTE
setup-github-refspec REMOTE:
    #!/usr/bin/env bash
    set -Eeuo pipefail

    if git remote get-url {{REMOTE}} &> /dev/null && git remote get-url {{REMOTE}} | grep -q 'github\.'; then
        git config --add remote.{{REMOTE}}.fetch '+refs/pull/*/head:refs/remotes/{{REMOTE}}/pr/*'
        git config --add remote.{{REMOTE}}.fetch '+refs/pull/*/merge:refs/remotes/{{REMOTE}}/pr/merge/*'
        echo "Added refspec to fetch GitHub PR refs for {{REMOTE}}"
    else
        echo "Remote {{REMOTE}} is not a GitHub remote"
    fi

# Add git refspec to fetch GitHub PR refs from origin and upstream
setup-github-refspecs: (setup-github-refspec "origin") (setup-github-refspec "upstream")

# git clean helper
_clean-git:
    git clean -fdx release.properties **/pom.xml.releaseBackup **/target

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

# git fetch configurable upstream
fetch:
    #!/usr/bin/env bash
    set -Eeuo pipefail
    if [ "{{offline}}" != "true" ]; then
        git fetch {{upstream_remote}} --tags --prune
    fi

# Create a branch if HEAD is detached
recover-detached-head:
    #!/usr/bin/env bash
    set -Eeuo pipefail

    if git symbolic-ref -q HEAD >/dev/null; then
        current_branch=$(git symbolic-ref --short HEAD)
        echo "Not in detached HEAD state. Current branch: $current_branch"
    else
        current_date=$(date +%Y-%m-%d)
        branch_name="branch-${current_date}"
        git checkout -b "$branch_name"
        echo "Created and checked out new branch: $branch_name"
    fi

# Generate a markdown list of commit messages
pull-request-description:
    git log {{upstream_remote}}/{{upstream_branch}}..HEAD --reverse --format='- %s'
