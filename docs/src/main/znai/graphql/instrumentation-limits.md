The limits instrumentations are implementations of `Instrumentation` from [GraphQL Java](https://www.graphql-java.com/) that bound the work a single GraphQL request can do. Each nested field in a query can fan out into more data fetches and a larger result, so an unbounded query can exhaust CPU and heap.

## Depth

`MaxDataQueryDepthInstrumentation` rejects queries that nest deeper than `maxDepth`. It's like GraphQL Java's `MaxQueryDepthInstrumentation`, except that it doesn't count introspection fields. Introspection reads only the static schema, yet the standard introspection query nests deeper than most data queries, so counting it would force a limit loose enough to allow long chains of to-many relationships.

## Complexity

GraphQL Java's `MaxQueryComplexityInstrumentation` rejects queries that select more than `maxComplexity` fields in total.

## Timeout

`GraphQLExecutionTimeoutInstrumentation` aborts an execution once it has run longer than `timeout`. The JVM cannot preempt a running thread, so the deadline is checked cooperatively before each field starts and after each data fetcher returns. Work inside a single data fetcher is not interrupted; the depth and complexity limits bound that work instead.

The timeout reads the time from an injected `Clock`. If your tests use an incrementing clock, each read advances it, so use a small `incrementAmount` in tests that run GraphQL queries.

## Configuration

`GraphQLLimitsFactory` holds the limits and creates the three instrumentations. Implement `GraphQLLimitsFactoryProvider` on your `Configuration` to make the limits configurable.

```json5
{
	graphQLLimits:
	{
		maxDepth: 8,
		maxComplexity: 200,
		timeout: "30 seconds",
	},
}
```

The values above are the defaults, so the whole `graphQLLimits` section is optional.

```java
GraphQLFactory factory = ...;

Clock clock = configuration.getClockFactory().createClock();

var metricsInstrumentation = new LiftwizardGraphQLMetricsInstrumentation(this.metricRegistry, clock);
var loggingInstrumentation = new LiftwizardGraphQLLoggingInstrumentation();

MutableList<Instrumentation> instrumentations = Lists.mutable.with(metricsInstrumentation, loggingInstrumentation);
instrumentations.addAllIterable(configuration.getGraphQLLimitsFactory().createInstrumentations(clock));
factory.setInstrumentations(instrumentations);
```

Add the limits after the logging and metrics instrumentations so that rejected and timed-out queries are still logged and measured.

The instrumentations live in the `liftwizard-graphql-instrumentation-limits` module, and the configuration lives in the `liftwizard-config-graphql-limits` module.

```xml
<dependency>
    <groupId>io.liftwizard</groupId>
    <artifactId>liftwizard-config-graphql-limits</artifactId>
</dependency>
```
