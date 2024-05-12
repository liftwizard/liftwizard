set shell := ["zsh", "--no-unset", "-c"]

# Setup the project (asdf) and run the default build (mvn)
default: asdf mvn

# asdf install
asdf:
    asdf plugin-add java
    asdf plugin-add maven
    asdf plugin-add mvnd https://github.com/joschi/asdf-mvnd
    asdf install
    asdf current

# git clean
_git-clean:
    git clean -dxf liftwizard-example/
    git clean -fdx release.properties '*/pom.xml.releaseBackup' pom.xml.releaseBackup

# clean (maven and git)
clean: _git-clean
    mvnd clean

# mvn verify
verify:
    mvnd verify

# mvn enforcer
enforcer:
    mvnd verify --activate-profiles maven-enforcer-plugin -DskipTests

# mvn dependency
analyze:
    mvnd verify --activate-profiles maven-dependency-plugin -DskipTests

# mvn javadoc
javadoc:
    mvnd verify --activate-profiles maven-javadoc-plugin -DskipTests

# mvn checkstyle
checkstyle:
    mvnd verify --activate-profiles checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict -DskipTests

# mvn reproducible
reproducible:
    mvnd verify artifact:check-buildplan -DskipTests

# mvn
mvn:
    mvnd verify --threads 2C --activate-profiles maven-enforcer-plugin,maven-dependency-plugin,checkstyle-semantics,checkstyle-formatting,checkstyle-semantics-strict

# mvn rewrite
rewrite:
    mvnd install org.openrewrite.maven:rewrite-maven-plugin:dryRun --projects '!liftwizard-example' --activate-profiles rewrite-maven-plugin,rewrite-maven-plugin-dryRun -DskipTests

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
release NEXT_VERSION: && _git-clean
    mvn clean release:clean release:prepare -DdevelopmentVersion=NEXT_VERSION

# Count lines of code
scc:
    scc **/src/{main,test}
