group_id_with_slashes := "io/liftwizard"

import ".just/console.just"
import ".just/maven.just"
import ".just/git.just"
import ".just/git-test.just"

# `just --list --unsorted`
[group('default')]
default:
    @just --list --unsorted

# Run build and auto-formatters
verify: mise test-problem-matchers mvn
    @echo "Verify checks completed"

# Deprecated alias for `verify`
precommit: verify

# `mise install`
mise:
    mise install --quiet
    mise current

# clean (maven and git)
@clean: _clean-git _clean-maven _clean-m2

# `node --test` for GitHub problem matchers
test-problem-matchers:
    node --test .github/*.test.mjs

markdownlint:
    markdownlint --config .markdownlint.jsonc  --fix .

# Override this with a command called `woof` which notifies you in whatever ways you prefer.
# My `woof` command uses `echo`, `say`, and sends a Pushover notification.
echo_command := env('ECHO_COMMAND', "echo")
