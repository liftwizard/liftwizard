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
    mise install --quiet
    mise current

# clean (maven and git)
@clean: _clean-git _clean-maven _clean-m2

markdownlint:
    markdownlint --config .markdownlint.jsonc  --fix .

# Count lines of code
scc:
    scc **/src/{main,test}

# Override this with a command called `woof` which notifies you in whatever ways you prefer.
# My `woof` command uses `echo`, `say`, and sends a Pushover notification.
echo_command := env('ECHO_COMMAND', "echo")

qodana:
    op run -- qodana scan \
        --apply-fixes \
        --linter jetbrains/qodana-jvm:2024.1
