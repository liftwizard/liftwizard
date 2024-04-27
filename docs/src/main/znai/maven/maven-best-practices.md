There are a number of best practices that can be handled at once by inheriting from a parent pom that takes care of them all.

Liftwizard ships with several parent poms that form an inheritance hierarchy. 

- [`liftwizard-minimal-parent`](minimal-parent.md) is the most minimal parent pom. It is meant to contain uncontroversial best practices that are applicable to all projects.
- [`liftwizard-profile-parent`](profile-parent.md) is a parent pom that inherits from `liftwizard-minimal-parent` and enables several linters and validators in profiles that are off by default.
- [`liftwizard-bom`](bill-of-materials.md) is a [Bill of Materials (BOM)](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms) that exports all modules within Liftwizard.
- `liftwizard-parent` is a parent pom that inherits from `liftwizard-profile-parent`, selects versions of libraries related to Dropwizard applications, and includes opinionated configurations for plugins.
