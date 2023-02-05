Liftwizard has some support for working with temporal data. In this section, we cover features of services that are build on top of temporal data.

While Liftwizard's temporal support is built on [Reladomo](reladomo/reladomo-overview.md), this section is framework agnostic.

# Temporal features

Each page in this section covers one of the common features of services built on temporal data.

* Non-destructive edits
* As-of queries
* Versioning
* Auditing
* Optimistic locking
* Diffs
* Maker/Checker workflows

# Overview

In an application with temporal data storage, data is stored along with timestamps.

[Non-destructive edits](temporal-data/non-destructive-updates) support updates and deletes without losing any information. Old data is phased out with a timestamp, and new data is phased in at the same timestamp.

[As-of queries](temporal-data/as-of-queries) return the data as it existed at a specific point in time.

As-of queries can be difficult for people to understand, and [versioning](temporal-data/versioning) can be a user-friendly supplementary API. Versioning assigns an integer version number to each change, and allows users to view and access past versions of the data.

[Auditing](temporal-data/auditing) tracks changes made to the data, including who made the change. With auditing enabled, each version has a user id in addition to its timestamps.

[Optimistic locking](temporal-data/optimistic-locking) prevents multiple users from simultaneously editing the same data and accidentally discarding changes. APIs that allow editing require a version number, and the edit will fail if the version number doesn't match the current version.

[Diff](temporal-data/diffs) APIs take two version numbers and show the differences between them.

[Maker/Checker workflows](temporal-data/maker-checker-workflows) allow data changes to be made by one user (the maker) and then reviewed and approved by another user (the checker) before being exposed to all users. In these workflows, most users view the latest approved version of the data, and the makers/checker see the latest version of the data.

In the next section, kick off a [running example](temporal-data/running-example) that will walk through the features of Liftwizard's temporal support.
