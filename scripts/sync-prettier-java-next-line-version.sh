#!/usr/bin/env bash

set -Eeuo pipefail

# Copy the prettier-java-next-line version from .mise/config.toml (pre-commit) into liftwizard-profile-parent (spotless)

mise_config=.mise/config.toml
pom=liftwizard-maven-build/liftwizard-profile-parent/pom.xml

version=$(perl -ne 'print $1 if /^"npm:prettier-java-next-line" = \{ version = "([^"]+)"/' "$mise_config")

if [[ -z $version ]]; then
    echo "No npm:prettier-java-next-line version found in $mise_config" >&2
    exit 1
fi

VERSION=$version perl -pi -e 's|<prettier-java-next-line\.version>[^<]*</prettier-java-next-line\.version>|<prettier-java-next-line.version>$ENV{VERSION}</prettier-java-next-line.version>|' "$pom"
