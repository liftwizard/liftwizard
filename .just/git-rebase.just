# git rebase onto configurable upstream/main
@rebase: _check-local-modifications fetch
    just _run "git rebase --interactive --autosquash --rebase-merges --update-refs {{ANSI_RED}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_DEFAULT}}"

# Rebase all branches onto configurable upstream/main or matching up-to-date origin branch
rebase-all: _check-local-modifications fetch
    #!/usr/bin/env bash
    set -Eeuo pipefail

    branches=($(git for-each-ref --format='%(refname:short)' refs/heads/ --sort -committerdate --no-contains {{upstream_remote}}/{{upstream_branch}}))
    total=${#branches[@]}
    current=0

    for branch in "${branches[@]}"
    do
        current=$((current + 1))
        included_count=$(git branch --contains "$branch" | wc -l)
        if [ "$included_count" -gt 1 ]; then
            echo "[{{ANSI_YELLOW}}${current}{{ANSI_DEFAULT}}/${total}] Skipping branch {{ANSI_GREEN}}$branch{{ANSI_DEFAULT}} as it is included in other branches{{ANSI_NORMAL}}"
            continue
        fi

        echo "[{{ANSI_YELLOW}}${current}{{ANSI_DEFAULT}}/${total}] Processing branch: {{ANSI_GREEN}}$branch{{ANSI_NORMAL}}"
        git checkout --quiet "$branch"

        # Check if origin/$branch exists
        if git rev-parse --verify origin/$branch &>/dev/null; then
            # Check if upstream/master is an ancestor of origin/$branch
            if git merge-base --is-ancestor {{upstream_remote}}/{{upstream_branch}} origin/$branch; then
                echo "Rebasing onto {{ANSI_RED}}origin/$branch{{ANSI_DEFAULT}} (up-to-date with or ahead of {{ANSI_RED}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_DEFAULT}}){{ANSI_NORMAL}}"
                git rebase --rebase-merges --update-refs origin/$branch
            else
                echo "Rebasing onto {{ANSI_RED}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_DEFAULT}} ({{ANSI_RED}}origin/$branch{{ANSI_DEFAULT}} is behind){{ANSI_NORMAL}}"
                git rebase --rebase-merges --update-refs {{upstream_remote}}/{{upstream_branch}}
            fi
        else
            echo "Rebasing onto {{ANSI_RED}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_DEFAULT}} (no matching origin branch){{ANSI_NORMAL}}"
            git rebase --rebase-merges --update-refs {{upstream_remote}}/{{upstream_branch}}
        fi
    done

alias ra := rebase-all

# git absorb into configurable upstream/main
@absorb:
    just _run "git absorb --base {{ANSI_RED}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_DEFAULT}} --force"

# Delete local branches merged into configurable upstream/main
delete-merged-local: fetch
    git branch --merged remotes/{{upstream_remote}}/{{upstream_branch}} \
        | grep -v "^\*" \
        | xargs --no-run-if-empty git branch -D

# Delete branches from origin merged into configurable upstream/main
delete-merged-remote: fetch
    #!/usr/bin/env bash
    set -Eeu
    if [ "{{offline}}" != "true" ]; then
        git branch --remote --list 'origin/*' --merged remotes/{{upstream_remote}}/{{upstream_branch}} \
            | grep --invert-match {{upstream_branch}} \
            | grep --invert-match HEAD \
            | grep "origin/" \
            | grep --invert-match "origin/pr/" \
            | cut -d "/" -f 2- \
            | xargs --no-run-if-empty git push --delete origin
    else
        echo "Skipping delete-remote-merged in offline mode"
    fi

# Delete local and remote branches that are merged into configurable upstream/main
delete-merged: delete-merged-local delete-merged-remote

git-all: rebase-all delete-merged
