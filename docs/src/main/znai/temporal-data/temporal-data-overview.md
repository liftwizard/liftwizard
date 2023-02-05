Liftwizard has support for working with **temporal data**.

In this section, we cover features of services that are build on top of temporal data.

Note: This section is agnostic of language or framework. If you are interested in the underlying technology, Liftwizard's temporal support is built on [Reladomo](reladomo/reladomo-overview.md).

Each page in this section covers one of the common features of services built on temporal data.

# Temporal features

In an application with temporal data storage, data is stored along with timestamps.

[Non-destructive edits](temporal-data/non-destructive-updates) are updates and deletes that **don't lose any information**. Old data is phased out with a timestamp, and new data is phased in at the same timestamp.

[As-of queries](temporal-data/as-of-queries) return the data as it existed at a **point in time**.

Working with timestamps can be difficult. [Versioning](temporal-data/versioning) can be a user-friendly supplementary API. Versions of data get **numbered**, starting with 1. As-of queries can be performed by timestamp or version number.

[Auditing](temporal-data/auditing) tracks **who** made each change, along with the data. With auditing enabled, each version has a user id in addition to its timestamps.

[Optimistic locking](temporal-data/optimistic-locking) prevents users from **simultaneously editing** the same data and accidentally discarding each other's work. APIs that perform edits require a version number as input, and will fail if the input version number and current version number don't match.

[Diff](temporal-data/diffs) APIs take two version numbers and show the differences between them.

[Maker/Checker workflows](temporal-data/maker-checker-workflows) allow data changes to be made by one user (the maker) and then reviewed and approved by another user (the checker) before being exposed to all users. In these workflows, most users view the **latest approved version** of the data, and the makers/checker see the latest version of the data.

In the next section, we kick off a [running example](temporal-data/running-example) that we'll use to walk through these features.
