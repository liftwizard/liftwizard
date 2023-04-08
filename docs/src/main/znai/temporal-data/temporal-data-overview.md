Liftwizard has built-in support for working with temporal data. In this section, we'll explore the various features of services that utilize this technology.

Note: This section is language and framework agnostic. If you’re interested in the underlying technology, Liftwizard's temporal support is built on [Reladomo](reladomo/reladomo-overview.md).

# Temporal features

In an application with temporal data storage, data is stored along with timestamps. Here are some key features of temporal support:

[Non-destructive edits](temporal-data/non-destructive-updates): updates and deletes **won't lose any information**. Old data is phased out with a timestamp, and new data is phased in at the same timestamp.

[As-of queries](temporal-data/as-of-queries): Retrieve data as it existed at a specific **point in time**.

[Versioning](temporal-data/versioning): **Numbered** versions of data can make working with timestamps easier. As-of queries can be performed by either timestamp or version number.

[Auditing](temporal-data/auditing): Keep track of **who** made each change, along with the data. With auditing enabled, each version has a user ID in addition to its timestamps.

[Optimistic locking](temporal-data/optimistic-locking): Prevent multiple users from **accidentally discarding** each other's work with this feature. APIs that perform edits require a version number as input, and will fail if the input version number and current version number don't match.

[Diff](temporal-data/diffs): See the differences between data at two version numbers.

[Maker/Checker workflows](temporal-data/maker-checker-workflows): **Make and review changes** before exposing them to all users. Most users view the latest *approved* version of the data, while makers/checkers see the *latest* version.

In the next section, we'll walk through a [running example](temporal-data/running-example) that showcases these features.