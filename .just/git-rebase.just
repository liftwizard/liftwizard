# git rebase onto configurable upstream/main
@rebase: _check-local-modifications fetch
    just _run "git rebase --interactive --autosquash --rebase-merges --update-refs {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_NORMAL}}"

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
            echo "[{{ANSI_YELLOW}}${current}{{ANSI_DEFAULT}}/${total}] Skipping branch {{ANSI_BRIGHT_GREEN}}{{ANSI_BOLD}}$branch{{ANSI_NORMAL}} as it is included in other branches"
            continue
        fi

        git checkout --quiet "$branch"
        rebase_message="[{{ANSI_YELLOW}}${current}{{ANSI_DEFAULT}}/${total}] Rebasing branch {{ANSI_BRIGHT_GREEN}}{{ANSI_BOLD}}$branch{{ANSI_NORMAL}} onto"

        # Check if origin/$branch exists
        if git rev-parse --verify origin/$branch &>/dev/null; then
            # Check if upstream/master is an ancestor of origin/$branch
            if git merge-base --is-ancestor {{upstream_remote}}/{{upstream_branch}} origin/$branch; then
                echo "$rebase_message {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}origin/$branch{{ANSI_NORMAL}} (because origin/$branch exists and is up-to-date with {{upstream_remote}}/{{upstream_branch}})"
                git rebase --rebase-merges --update-refs --quiet origin/$branch
            else
                echo "$rebase_message {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_NORMAL}} (because origin/$branch exists but is not an ancestor)"
                git rebase --rebase-merges --update-refs --quiet {{upstream_remote}}/{{upstream_branch}}
            fi
        else
            echo "$rebase_message {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_NORMAL}} (because origin/$branch does not exist)"
            git rebase --rebase-merges --update-refs --quiet {{upstream_remote}}/{{upstream_branch}}
        fi
    done

alias ra := rebase-all

# git absorb into configurable upstream/main
@absorb *FLAGS:
    just _run "git absorb --base {{ANSI_BRIGHT_RED}}{{ANSI_BOLD}}{{upstream_remote}}/{{upstream_branch}}{{ANSI_NORMAL}} --force {{FLAGS}}"

setup-git-absorb:
    #!/usr/bin/env bash
    set -Eeuo pipefail

    # Define arrays for configuration keys, desired values, and descriptions
    keys=("absorb.maxStack" "absorb.oneFixupPerCommit" "absorb.autoStageIfNothingStaged" "absorb.fixupTargetAlwaysSHA")
    values=("50" "true" "true" "false")
    descriptions=("Maximum number of commits to search for absorption candidates" \
                  "Create only one fixup commit per target commit" \
                  "Stage all changes if nothing is staged" \
                  "Use commit message for fixup when possible, fall back to SHA")

    echo "Git absorb configuration"
    echo

    printf "%-31s %-12s %-19s %-19s\n" "Key" "Action" "Value Before" "Value After"
    printf "%-31s %-12s %-19s %-19s\n" "-------------------------------" "------------" "-------------------" "-------------------"

    changes=0

    # Iterate over each configuration setting and print a table row
    for i in "${!keys[@]}"; do
        key="${keys[$i]}"
        desired_value="${values[$i]}"
        current_value=$(git config --global --get "${key}" || echo "(not set)")

        if [ "${current_value}" = "${desired_value}" ]; then
            action="No action"
        else
            action="Set"
            ((changes++))
        fi

        printf "%-31s %-12s %-19s %-19s\n" "${key}" "${action}" "${current_value}" "${desired_value}"
    done

    echo

    if [ "${changes}" -eq 0 ]; then
        echo "All values are already set correctly."
        exit 0
    fi

    read -p "Proceed with these changes? [y/N] " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        for i in "${!keys[@]}"; do
            key="${keys[$i]}"
            desired_value="${values[$i]}"
            current_value=$(git config --global --get "${key}" || echo "(not set)")
            if [ "${current_value}" != "${desired_value}" ]; then
                git config --global "${key}" "${desired_value}"
            fi
        done
        echo "Values updated successfully."
    else
        echo "Operation cancelled."
        exit 1
    fi

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
alias g := git-all
