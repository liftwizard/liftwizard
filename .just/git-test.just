# set up `git-test`
setup-git-test:
    # Combined
    git test add --forget --test default      'just --global-justfile _check-local-modifications && (should-skip-commit || just precommit) && just --global-justfile _check-local-modifications'
    git test add --forget --test no-tests     'MVN_FLAGS="--threads 2C -DskipTests" just clean _check-local-modifications mvn'
    git test add --forget --test module-build 'if grep -q liftwizard-generator-rewrite liftwizard-generator-plugins/pom.xml 2>/dev/null; then mvn clean install -pl liftwizard-generator-plugins/liftwizard-generator-rewrite,liftwizard-generator-plugins/liftwizard-generator-rewrite-plugin,liftwizard-utility/liftwizard-rewrite -am; else mvn clean install -pl liftwizard-utility/liftwizard-rewrite -am; fi'
    # Individual
    # git test add --forget --test compile                      'just clean _check-local-modifications compile'
    # git test add --forget --test test                         'just clean _check-local-modifications test'
    # git test add --forget --test enforcer                     'just clean _check-local-modifications enforcer'
    # git test add --forget --test dependency                   'just clean _check-local-modifications dependency'
    # git test add --forget --test checkstyle-semantics         'just clean _check-local-modifications checkstyle-semantics'
    # git test add --forget --test checkstyle-formatting        'just clean _check-local-modifications checkstyle-formatting'
    # git test add --forget --test checkstyle-semantics-strict  'just clean _check-local-modifications checkstyle-semantics-strict'
    # git test add --forget --test checkstyle-formatting-strict 'just clean _check-local-modifications checkstyle-formatting-strict'
    # git test add --forget --test javadoc                      'just clean _check-local-modifications javadoc'
    # git test add --forget --test reproducible                 'just clean _check-local-modifications reproducible'
    # git test add --forget --test formats                      'just clean _check-local-modifications spotless formats'
    # git test add --forget --test prettier                     'just clean _check-local-modifications spotless prettier-java'
    # git test add --forget --test gjf                          'just clean _check-local-modifications spotless google-java-format'
    # git test add --forget --test sort-imports                 'just clean _check-local-modifications spotless java-sort-imports'
    # git test add --forget --test prettier-sort-imports        'just clean _check-local-modifications spotless prettier-java-sort-imports'
    # git test add --forget --test unused-imports               'just clean _check-local-modifications spotless java-unused-imports'
    # git test add --forget --test cleanthat                    'just clean _check-local-modifications spotless java-cleanthat'
    # git test add --forget --test pom                          'just clean _check-local-modifications spotless pom'
    # git test add --forget --test markdown                     'just clean _check-local-modifications spotless markdown'
    # git test add --forget --test yaml                         'just clean _check-local-modifications spotless yaml'
    # git test add --forget --test sql                          'just clean _check-local-modifications spotless sql'

git_tests := env('GIT_TESTS', "default")
