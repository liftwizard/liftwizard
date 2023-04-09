To confirm that we have not lost any data, we can perform an as-of query. We want to query the state of the blueprint at time 3 (`2001-01-03`), before the non-destructive update.

We `GET` from `/api/blueprint/{blueprintKey}?asOf={asOf}`.

We created a blueprint with key `6ed1f638-a63c-3a54-af67-ba494f27bff2` at time 3 (`2001-01-03`) and edited it at time 4 (`2001-01-04`). We can query as-of any time in the range `[2001-01-03, 2001-01-04)`. We'll use the beginning of the range: `2001-01-03`.

Plugging these values into the template, we GET `/api/blueprint/6ed1f638-a63c-3a54-af67-ba494f27bff2?asOf=2001-01-03T23:59:59Z`

:include-file: as-of-queries-2-getBlueprintAsOf.diff {title: "GET /api/blueprint/{blueprintKey}?asOf=2001-01-03T23:59:59Z", commentsType: "inline"}

The response we get from `/api/blueprint/{blueprintKey}?asOf=2001-01-03T23:59:59Z` is nearly identical to the response we would have got from `/api/blueprint/{blueprintKey}` had we run the query at time 3: `2001-01-03`. This makes sense!

There's a small difference in the data. Some of the `systemTo` values that used to be `null` are now time 4: `2001-01-04`. This illustrates an important rule of temporal data.

**All writes into the data store are immutable and append-only, except for the `systemTo` value.**

# Temporal Schema

Next we'll focus on the data store. In this example, we're using a relational database, but these concepts apply to any data store.

The schema maps closely to the json examples above, so if you're comfortable with the data, feel free to skip ahead to the queries.

## BLUEPRINT after create

:include-table: as-of-queries-1-Blueprint.csv {wide: true, systemFrom: {width: 215, align: "right"}, systemTo: {width: 215, align: "right"}}

## BLUEPRINT after update

:include-table: as-of-queries-3-Blueprint.csv {wide: true, systemFrom: {width: 215, align: "right"}, systemTo: {width: 215, align: "right"}}

## BLUEPRINT_VERSION after create

:include-table: as-of-queries-2-BlueprintVersion.csv {wide: true, title: "BlueprintVersion", systemFrom: {width: 215, align: "right"}, systemTo: {width: 215, align: "right"}}

## BLUEPRINT_VERSION after update

:include-table: as-of-queries-4-BlueprintVersion.csv {wide: true, title: "BlueprintVersion", systemFrom: {width: 215, align: "right"}, systemTo: {width: 215, align: "right"}}

## Temporal Schema patterns

* All tables have `systemFrom` and `systemTo` columns.
* Old data is phased out by setting `systemTo` to now.
* New data is phased in by setting `systemFrom` to now.
* The new row's `systemFrom` and the old row's `systemTo` are set to the same value, forming a contiguous timeline.
* When several tables are edited within a transaction, the `systemFrom` and `systemTo` values are set to the same value across all tables.
* Unchanged data is copied from the old row to the new row. For very wide columns that don't change frequently, it may be more efficient to split out a separate table.
* The `systemTo` value of the new row is set to `9999-12-01 23:59:00.00` to indicate that the row is still active. In json, we had used `null` to represent the infinity date.

# Temporal queries in SQL

As-of queries are implemented in SQL by adding temporal criteria to our `WHERE` clause.

```sql
select *
from BLUEPRINT t0
where t0.key = '6ed1f638-a63c-3a54-af67-ba494f27bff2'
  and t0.system_from <= '2001-01-03 23:59:59.000'
  and t0.system_to > '2001-01-03 23:59:59.000'
```

Now we can see why the infinity date is represented as `9999-12-01 23:59:00.00`. If we instead used `null` we'd need to add additional criteria to our WHERE clauses.

Joins that are one hop away from our main table are similar.

```sql
select *
from BLUEPRINT_TAG t0
where t0.blueprint_key = '6ed1f638-a63c-3a54-af67-ba494f27bff2'
  and t0.system_from <= '2001-01-03 23:59:59.000'
  and t0.system_to > '2001-01-03 23:59:59.000'
```

Joins that are two hops away from our main table are more complicated. We'll see examples of these later.

## Temporal query patterns

* We perform asOf queries by adding `where system_from <= {asOf} and system_to > {asOf}` to our `WHERE` clause.
* We add this exact came criteria to every query.
* We always `SELECT` all columns from the table. In the examples above we used `SELECT *`. In production usage, it's common to list the columns explicitly.
* We never `SELECT` columns from two tables in the same query. Even in the upcoming examples of joins, we always `SELECT` from one table at a time.

In the next section, we'll learn about adding versions and querying "as of" a version number.
