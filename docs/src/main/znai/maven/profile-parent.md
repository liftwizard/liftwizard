# Liftwizard Profile Parent

`liftwizard-profile-parent` is a Maven parent POM that gives your project pre-configured profiles for code formatting, static analysis, refactoring, and more. By inheriting from it, you gain access to battle-tested CI workflows without configuring each plugin yourself.

## Quick Start

**Step 1:** Set `liftwizard-profile-parent` as your project's parent:

```xml
<parent>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-profile-parent</artifactId>
    <version>${liftwizard.version}</version>
</parent>
```

**Step 2:** Run profiles from the command line:

```bash
# Format Java code
mvn spotless:apply --activate-profiles spotless-apply,spotless-java,spotless-prettier-java-sort-imports

# Run static analysis
mvn verify --activate-profiles errorprone-strict
```

**Step 3:** Add GitHub Actions workflows to run these profiles in CI. Here are two patterns used in production.

### Pattern 1: Required Checks (merge-group)

Run all checks in parallel and require them to pass before merging:

```yaml
on:
  pull_request:
  merge_group:

jobs:
  maven-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: jdx/mise-action@v2
      - run: mvn verify

  maven-errorprone-strict:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: jdx/mise-action@v2
      - run: mvn verify --activate-profiles errorprone-strict -DskipTests

  checkstyle-semantics:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: jdx/mise-action@v2
      - run: mvn checkstyle:check --activate-profiles checkstyle-semantics

  spotless-java:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: jdx/mise-action@v2
      - run: mvn spotless:check --activate-profiles spotless-check,spotless-java,spotless-prettier-java-sort-imports

  all-checks:
    name: All checks
    if: ${{ !cancelled() }}
    needs: [maven-test, maven-errorprone-strict, checkstyle-semantics, spotless-java]
    runs-on: ubuntu-latest
    steps:
      - uses: re-actors/alls-green@release/v1
        with:
          jobs: ${{ toJSON(needs) }}
```

See [merge-group.yml](https://github.com/motlin/liftwizard/blob/main/.github/workflows/merge-group.yml) for all available profiles in use.

### Pattern 2: Auto-Fix (pull-request)

Automatically fix violations and push to a fix branch:

```yaml
on:
  pull_request:

jobs:
  spotless-java-fix:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ github.event.pull_request.head.ref }}
          token: ${{ secrets.GITHUB_TOKEN }}

      - uses: jdx/mise-action@v2

      - name: Configure Git
        run: |
          git config --global user.name "GitHub Actions"
          git config --global user.email "github-actions@github.com"

      - name: Run Spotless Apply and commit changes
        run: |
          mvn spotless:apply --activate-profiles spotless-apply,spotless-java,spotless-prettier-java-sort-imports

          if [[ -n $(git status --porcelain) ]]; then
            FIX_BRANCH="fix-${{ github.event.pull_request.number }}-spotless-java"
            git switch --create $FIX_BRANCH
            git add --all
            git commit --message "Auto-fix: Apply Spotless Java formatting"
            git push --force origin $FIX_BRANCH
            echo "Fixes pushed to $FIX_BRANCH branch."
            exit 1
          fi
```

See [pull-request.yml](https://github.com/motlin/liftwizard/blob/main/.github/workflows/pull-request.yml) for auto-fix jobs for spotless, prettier, errorprone, and OpenRewrite.

## Profile List

| Profile ID                            | Category              | Description                                  |
| ------------------------------------- | --------------------- | -------------------------------------------- |
| **Build & Packaging**                 |                       |                                              |
| `maven-javadoc-plugin`                | Build & Packaging     | Generate Javadoc                             |
| `maven-shade-plugin`                  | Build & Packaging     | Create uber-jars                             |
| `znai-maven-plugin`                   | Build & Packaging     | Generate Znai documentation                  |
| **Code Formatting**                   |                       |                                              |
| `prettier-apply`                      | Code Formatting       | Apply Prettier formatting                    |
| `prettier-check`                      | Code Formatting       | Check code formatting with Prettier          |
| `spotless-antlr`                      | Code Formatting       | ANTLR grammar formatting                     |
| `spotless-apply`                      | Code Formatting       | Apply Spotless formatting                    |
| `spotless-check`                      | Code Formatting       | Check code formatting with Spotless          |
| `spotless-formats`                    | Code Formatting       | Format .gitattributes and .gitignore files   |
| `spotless-google-java-format`         | Code Formatting       | Google Java style formatting                 |
| `spotless-java`                       | Code Formatting       | Basic Java formatting (whitespace, newlines) |
| `spotless-java-cleanthat`             | Code Formatting       | Code cleanup and refactoring                 |
| `spotless-java-sort-imports`          | Code Formatting       | Sort and organize Java imports               |
| `spotless-java-unused-imports`        | Code Formatting       | Remove unused Java imports                   |
| `spotless-json`                       | Code Formatting       | JSON/JSON5 formatting                        |
| `spotless-markdown`                   | Code Formatting       | Markdown formatting                          |
| `spotless-pom`                        | Code Formatting       | POM file formatting and sorting              |
| `spotless-prettier-java`              | Code Formatting       | Prettier formatting for Java                 |
| `spotless-prettier-java-sort-imports` | Code Formatting       | Prettier + import sorting                    |
| `spotless-sql`                        | Code Formatting       | SQL formatting with Prettier                 |
| `spotless-yaml`                       | Code Formatting       | YAML formatting                              |
| **Code Refactoring**                  |                       |                                              |
| `rewrite-maven-plugin`                | Code Refactoring      | Safe refactoring recipes                     |
| `rewrite-maven-plugin-dryRun`         | Code Refactoring      | Verify refactors without applying            |
| `rewrite-maven-plugin-one-off`        | Code Refactoring      | Potentially breaking refactors               |
| **Dependency Management**             |                       |                                              |
| `maven-dependency-plugin`             | Dependency Management | Analyze dependencies                         |
| `maven-enforcer-plugin`               | Dependency Management | Enforce dependency rules                     |
| **Static Analysis**                   |                       |                                              |
| `checkstyle-formatting`               | Static Analysis       | Code formatting checks                       |
| `checkstyle-formatting-strict`        | Static Analysis       | Strict formatting checks                     |
| `checkstyle-semantics`                | Static Analysis       | Semantic code style checks                   |
| `checkstyle-semantics-strict`         | Static Analysis       | Strict semantic checks                       |
| `errorprone`                          | Static Analysis       | Basic error-prone checks                     |
| `errorprone-patch`                    | Static Analysis       | Generate patches for error-prone fixes       |
| `errorprone-strict`                   | Static Analysis       | Strict error-prone checks                    |
| `spotbugs-maven-plugin`               | Static Analysis       | FindBugs successor for bug detection         |
| **Testing & Coverage**                |                       |                                              |
| `jacoco-maven-plugin`                 | Testing & Coverage    | Code coverage with JaCoCo                    |
| `rerecord`                            | Testing & Coverage    | Re-record test snapshots                     |
| **Utility**                           |                       |                                              |
| `deploy`                              | Utility               | Deploy artifacts with sources                |
| `spotless-preserve-cache`             | Utility               | Preserve Spotless cache during clean         |

For detailed profile configurations, see [liftwizard-profile-parent/pom.xml](https://github.com/motlin/liftwizard/blob/main/liftwizard-maven-build/liftwizard-profile-parent/pom.xml) on GitHub.
