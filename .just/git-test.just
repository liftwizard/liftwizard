# set up git-test
setup-git-test:
    git test add --forget --test default        'just test'
    git test add --forget --test verify         'just verify'
    git test add --forget --test enforcer       'just enforcer'
    git test add --forget --test dependency     'just dependency'
    git test add --forget --test checkstyle     'just checkstyle'
    git test add --forget --test javadoc        'just javadoc'
    git test add --forget --test reproducible   'just reproducible'
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

fail_fast := env('FAIL_FAST', "false")

# git-test on the range of commits between a configurable upstream/main and {{BRANCH}}
test-branch BRANCH="HEAD" TEST="default" *FLAGS="--retest":
    echo "Testing branch: {{BRANCH}}"
    echo "{{BRANCH}}" > JUSTFILE_BRANCH
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

# Fix the current commit and rebase
test-fix:
    git add --update
    git commit --fixup HEAD
    just _check-local-modifications
    cat JUSTFILE_BRANCH
    git rebase --onto HEAD HEAD^ $(cat JUSTFILE_BRANCH)
    just rebase
    cat JUSTFILE_BRANCH
    rm JUSTFILE_BRANCH
