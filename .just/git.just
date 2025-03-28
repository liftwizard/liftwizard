upstream_remote := env('UPSTREAM_REMOTE', "upstream")
upstream_branch := env('UPSTREAM_BRANCH', "main")

# git clean helper
_clean-git:

# Fail if there are local modifications or untracked files
_check-local-modifications:
    #!/usr/bin/env bash
    set -uo pipefail

    ERRORS=""

    git diff --quiet || ERRORS+="- Working tree has uncommitted changes\n"
    git diff --staged --quiet || ERRORS+="- Index has staged changes\n"
    git status --porcelain | grep -q '^??' && ERRORS+="- Untracked files exist\n"

    if [ -n "$ERRORS" ]; then
        {{echo_command}} "Local modifications"
        echo -e "$ERRORS"
        just _run "git status"
        exit 1
    fi
