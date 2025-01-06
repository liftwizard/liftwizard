default_mvn      := env('MVN_BINARY',   "mvnd")
default_target   := env('MVN_TARGET',   "verify")
default_flags    := env('MVN_FLAGS',    "--threads 2C")
skip_tests_flags := default_flags + " -DskipTests"
default_profiles := env('MVN_PROFILES', "--activate-profiles maven-enforcer-plugin,maven-dependency-plugin,maven-javadoc-plugin,checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict,spotless-apply,spotless-formats,spotless-java-sort-imports,spotless-java-unused-imports,spotless-java-cleanthat,spotless-pom,spotless-markdown,spotless-json,spotless-yaml")

# clean ~/.m2 with `rm -rf ~/.m2/repository/...`
_clean-m2:
    #!/usr/bin/env bash
    set -uo pipefail
    rm -rf ~/.m2/repository/{{group_id_with_slashes}}/**/*-SNAPSHOT
    exit 0

# mvn clean
_clean-maven MVN=default_mvn *FLAGS=default_flags: (mvn MVN "clean" "" FLAGS)

# mvn verify
verify MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "" skip_tests_flags) && _check-local-modifications

# mvn enforcer
enforcer MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "--activate-profiles maven-enforcer-plugin" skip_tests_flags) && _check-local-modifications

# mvn dependency
dependency MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "--activate-profiles maven-dependency-plugin" skip_tests_flags) && _check-local-modifications

# mvn javadoc
javadoc MVN=default_mvn: _check-local-modifications clean (mvn MVN default_target "--activate-profiles maven-javadoc-plugin" skip_tests_flags) && _check-local-modifications

checkstyle-semantics MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics" default_flags) && _check-local-modifications
checkstyle-formatting MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting" default_flags) && _check-local-modifications
checkstyle-semantics-strict MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-semantics-strict" default_flags) && _check-local-modifications
checkstyle-formatting-strict MVN="mvn": _check-local-modifications clean (mvn MVN "checkstyle:check" "--activate-profiles checkstyle-formatting-strict" default_flags) && _check-local-modifications
checkstyle: checkstyle-semantics checkstyle-formatting checkstyle-semantics-strict checkstyle-formatting-strict

# mvn reproducible
reproducible MVN=default_mvn: (mvn MVN "verify artifact:check-buildplan" "" skip_tests_flags) && _check-local-modifications

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
release NEXT_VERSION: && _clean-git
    git checkout {{upstream_remote}}/{{upstream_branch}}
    mvn --batch-mode clean release:clean release:prepare -DdevelopmentVersion={{NEXT_VERSION}}

# mvn archetype
archetype MVN=default_mvn:
    {{MVN}} install --also-make --projects klass-maven-archetype
