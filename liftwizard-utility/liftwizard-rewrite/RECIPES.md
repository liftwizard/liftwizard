# 📋 Liftwizard OpenRewrite Recipe Reference

This document provides a complete listing of all OpenRewrite recipes in the `liftwizard-rewrite` module with transformation examples.

For getting started instructions, see [README.md](README.md).

## Composite Recipes

Nine composite recipes are available:

| Composite Recipe                                                                             | Description                                                    |
| -------------------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| `io.liftwizard.rewrite.BestPractices`                                                        | General Java best practices for null-safety                    |
| `io.liftwizard.rewrite.logging.Log4j1ToSlf4j1`                                               | Migrate Log4j 1 to SLF4J, skipping object logging files        |
| `io.liftwizard.rewrite.LoggingBestPractices`                                                 | Transform logging to SLF4J parameterized format                |
| `io.liftwizard.rewrite.assertj.AssertJMigration`                                             | Migrate from Eclipse Collections testutils to AssertJ          |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsBestPractices`                  | Optimize existing Eclipse Collections usage                    |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption`                       | Migrate from Java Collections Framework to Eclipse Collections |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsRemoval`                        | Replace Eclipse Collections APIs with Java alternatives        |
| `io.liftwizard.rewrite.eclipse.collections.adoption.unsafe.EclipseCollectionsAdoptionUnsafe` | Adoption patterns that may change semantics with nulls         |
| `io.liftwizard.rewrite.dropwizard.testing.DropwizardTestingJUnit5Migration`                  | Migrate Dropwizard JUnit 4 testing rules to JUnit 5 extensions |

## Best Practices Recipes (General Java)

These recipes improve general Java code quality and are not specific to Eclipse Collections.

### NullSafeEquals

Replace complex null-safe equality patterns with Objects.equals():

- `left == null ? right == null : left.equals(right)` → `Objects.equals(left, right)`
- `left == null ? right != null : !left.equals(right)` → `!Objects.equals(left, right)`
- `left == right || left != null && left.equals(right)` → `Objects.equals(left, right)`

### NullSafeHashCode

Replace null-safe hashCode patterns with Objects.hashCode():

- `object == null ? 0 : object.hashCode()` → `Objects.hashCode(object)`

### ExplicitTypeToVar

Replace explicit type declarations with `var` when the variable is initialized with a constructor call of exactly the same type:

- `StringBuilder sb = new StringBuilder()` → `var sb = new StringBuilder()`
- `ArrayList<String> list = new ArrayList<>()` → `var list = new ArrayList<String>()`

**This recipe is conservative and does NOT transform:**

- Supertype: `Object x = new StringBuilder()`
- Interface vs implementation: `List<String> x = new ArrayList<>()`
- Non-constructor initializers: `ArrayList<String> x = this.getList()`

### HideUtilityClassConstructor

Fork of `org.openrewrite.staticanalysis.HideUtilityClassConstructor` that generates a private constructor throwing `AssertionError` to enforce noninstantiability.

For utility classes with no explicit constructor, adds:

```java
private ClassName() {
    throw new AssertionError("Suppress default constructor for noninstantiability");
}
```

For utility classes with public or package-private constructors, changes the access level to private.

## Log4j 1 to SLF4J Migration

The `io.liftwizard.rewrite.logging.Log4j1ToSlf4j1` composite recipe migrates Log4j 1.x to SLF4J 1.x, with a key safety improvement over the upstream recipe: it skips files that use Log4j 1's object logging pattern.

### Why a fork?

Log4j 1's `Category.info(Object)` accepts any Object, enabling structured logging where appenders can inspect the argument's type via `instanceof`. The upstream migration chain silently destroys this by transforming `LOGGER.info(myObject)` to `LOGGER.info("{}", myObject)`, which compiles but loses the object's type information.

Our fork adds a precondition: if a file contains any log call with a non-String argument (e.g., `LOGGER.info(myEvent)`), the entire file is skipped. It stays on Log4j 1 and must be migrated manually.

## Logging Best Practices Recipes

The `io.liftwizard.rewrite.LoggingBestPractices` composite recipe transforms eager logging patterns to use SLF4J parameterized logging.

### Recipes

#### StringFormatToParameterizedLogging

Converts `String.format()` calls in SLF4J logging statements to parameterized logging.

- `LOGGER.info(String.format("User %s logged in", username))` → `LOGGER.info("User {} logged in", username)`

See [StringFormatToParameterizedLogging documentation](https://docs.openrewrite.org/recipes/java/logging/slf4j/stringformattoparameterizedlogging).

#### MessageFormatToParameterizedLogging

Converts `MessageFormat.format()` calls in SLF4J logging statements to parameterized logging.

- `LOGGER.info(MessageFormat.format("User {0} logged in", username))` → `LOGGER.info("User {} logged in", username)`

See [MessageFormatToParameterizedLogging documentation](https://docs.openrewrite.org/recipes/java/logging/slf4j/messageformattoparameterizedlogging).

#### ParameterizedLogging (Liftwizard fork)

Converts string concatenation in logging statements to parameterized logging. Forked from the [upstream OpenRewrite recipe](https://docs.openrewrite.org/recipes/java/logging/slf4j/parameterizedlogging) to exclude the incorrect transformation of simple object arguments.

- `LOGGER.info("User " + username + " logged in")` -> `LOGGER.info("User {} logged in", username)`
- `LOGGER.info(myObject)` -> unchanged (upstream would incorrectly transform to `LOGGER.info("{}", myObject)`)

#### [StripToStringFromArguments](https://docs.openrewrite.org/recipes/java/logging/slf4j/striptostringfromarguments)

- `LOGGER.debug("Value: {}", obj.toString())` → `LOGGER.debug("Value: {}", obj)`

See [StripToStringFromArguments documentation](https://docs.openrewrite.org/recipes/java/logging/slf4j/striptostringfromarguments).

#### RemoveUnnecessaryLogLevelGuards

Removes redundant if-statement guards around SLF4J logging calls when all arguments are safe (no expensive computation).

- `if (LOGGER.isDebugEnabled()) { LOGGER.debug("Value: {}", name); }` → `LOGGER.debug("Value: {}", name);`

See [RemoveUnnecessaryLogLevelGuards documentation](https://docs.openrewrite.org/recipes/java/logging/slf4j/removeunnecessaryloglevelguards).

## AssertJ Migration Recipes

These recipes migrate from `eclipse-collections-testutils` libraries to AssertJ.

### VerifyAssertEmptyToAssertJ

Replace Eclipse Collections Verify.assertEmpty() with AssertJ:

- `Verify.assertEmpty(message, collection)` → `assertThat(collection).as(message).isEmpty()`
- `Verify.assertEmpty(collection)` → `assertThat(collection).isEmpty()`

### VerifyAssertNotEmptyToAssertJ

Replace Eclipse Collections Verify.assertNotEmpty() with AssertJ:

- `Verify.assertNotEmpty(message, collection)` → `assertThat(collection).as(message).isNotEmpty()`
- `Verify.assertNotEmpty(collection)` → `assertThat(collection).isNotEmpty()`

### VerifyAssertSizeToAssertJ

Replace Eclipse Collections Verify.assertSize() with AssertJ:

- `Verify.assertSize(message, expectedSize, iterable)` → `assertThat(iterable).as(message).hasSize(expectedSize)`
- `Verify.assertSize(expectedSize, iterable)` → `assertThat(iterable).hasSize(expectedSize)`
- `Verify.assertSize(arrayName, expectedSize, array)` → `assertThat(array).as(arrayName).hasSize(expectedSize)`
- `Verify.assertSize(expectedSize, array)` → `assertThat(array).hasSize(expectedSize)`

### VerifyAssertThrowsToAssertJ

Replace Eclipse Collections Verify.assertThrows() with AssertJ:

- `Verify.assertThrows(exceptionClass, () -> { ... })` → `assertThatThrownBy(() -> { ... }).isInstanceOf(exceptionClass)`
- `Verify.assertThrows(exceptionClass, callable)` → `assertThatThrownBy(() -> callable.call()).isInstanceOf(exceptionClass)`

This recipe handles both lambda expressions and callable variables. Lambda expressions are passed directly to assertThatThrownBy, while callable variables are wrapped in a lambda that calls the .call() method.

### VerifyAssertCountToAssertJ

Replace Eclipse Collections Verify.assertCount() with AssertJ using filteredOn() for better error messages:

- `Verify.assertCount(message, expectedCount, iterable, predicate)` → `assertThat(iterable).as(message).filteredOn(predicate).hasSize(expectedCount)`
- `Verify.assertCount(expectedCount, iterable, predicate)` → `assertThat(iterable).filteredOn(predicate).hasSize(expectedCount)`

This recipe provides better error messages when assertions fail by showing the actual filtered elements. It only applies when the predicate is a lambda expression or method reference, as it converts Eclipse Collections Predicate to Java Predicate automatically.

### AssertionsStaticImport

Convert qualified AssertJ assertions to static imports:

- `Assertions.assertThat(value)` → `assertThat(value)` with `import static org.assertj.core.api.Assertions.assertThat`

This recipe converts non-static imports of `org.assertj.core.api.Assertions` to static imports, removing the `Assertions.` prefix from method calls. This is the conventional way to use AssertJ assertions.

## Eclipse Collections Best Practices Recipes

### Empty/Size Checks

#### ECSizeToEmpty

Replace size comparisons with isEmpty/notEmpty on Eclipse Collections types:

- `collection.size() == 0` → `collection.isEmpty()`
- `collection.size() > 0` → `collection.notEmpty()`
- `collection.size() != 0` → `collection.notEmpty()`
- `collection.size() >= 1` → `collection.notEmpty()`
- `collection.size() < 1` → `collection.isEmpty()`
- `collection.size() <= 0` → `collection.isEmpty()`

#### ECSimplifyNegatedEmptyChecks

Simplify negated empty checks:

- `!list.isEmpty()` → `list.notEmpty()`
- `!list.notEmpty()` → `list.isEmpty()`

### Satisfies Patterns

#### ECCountToSatisfies

Replace count comparisons with anySatisfy/noneSatisfy:

- `list.count(predicate) == 0` → `list.noneSatisfy(predicate)`
- `list.count(predicate) > 0` → `list.anySatisfy(predicate)`
- `list.count(predicate) != 0` → `list.anySatisfy(predicate)`
- `list.count(predicate) <= 0` → `list.noneSatisfy(predicate)`
- `list.count(predicate) >= 1` → `list.anySatisfy(predicate)`

#### ECCountEqualsSize

Replace count(predicate) == size() with allSatisfy(predicate):

- `list.count(predicate) == list.size()` → `list.allSatisfy(predicate)`
- `list.size() == list.count(predicate)` → `list.allSatisfy(predicate)`

The allSatisfy() can short-circuit on the first non-matching element, while count() == size() must scan the entire collection.

#### ECDetectOptionalToSatisfies

Replace detectOptional().isPresent() with anySatisfy/noneSatisfy:

- `list.detectOptional(predicate).isPresent()` → `list.anySatisfy(predicate)`
- `!list.detectOptional(predicate).isPresent()` → `list.noneSatisfy(predicate)`

#### ECSimplifyNegatedSatisfies

Simplify negated satisfies calls:

- `!list.anySatisfy(predicate)` → `list.noneSatisfy(predicate)`
- `!list.noneSatisfy(predicate)` → `list.anySatisfy(predicate)`

#### ECSimplifyNegatedIterateSatisfies

Simplify negated satisfies calls on Iterate utility:

- `!Iterate.anySatisfy(iterable, predicate)` → `Iterate.noneSatisfy(iterable, predicate)`
- `!Iterate.noneSatisfy(iterable, predicate)` → `Iterate.anySatisfy(iterable, predicate)`

### Select/Reject Simplifications

#### ECSimplifyNegatedSelectReject

Flip select() and reject() when the lambda contains a negation pattern:

- `list.select(x -> !pred(x))` → `list.reject(x -> pred(x))`
- `list.select(x -> x != value)` → `list.reject(x -> x == value)`
- `list.reject(x -> !pred(x))` → `list.select(x -> pred(x))`
- `list.reject(x -> x != value)` → `list.select(x -> x == value)`

This eliminates double negation patterns and improves readability by using the more appropriate method for the predicate logic.

### Method Reference Simplifications

#### ECSimplifyMethodReferences

Simplify redundant functional method references by removing unnecessary method calls:

- `list.select(predicate::accept)` → `list.select(predicate)`
- `list.select(predicate::test)` → `list.select(predicate)`
- `list.collect(function::valueOf)` → `list.collect(function)`
- `list.collect(function::apply)` → `list.collect(function)`
- `list.forEach(procedure::value)` → `list.forEach(procedure)`
- `list.forEach(consumer::accept)` → `list.forEach(consumer)`

When a variable is already the correct functional type, calling its single abstract method via a method reference is redundant. This applies to both Eclipse Collections functional types (Predicate, Function, Procedure) and JDK functional types (java.util.function.Predicate, Function, Consumer).

### Primitive Sum Optimizations

#### ECCollectIntSum and ECCollectLongSum

Replace `collect<primitive>().sum()` with `sumOf<primitive>()` to avoid intermediate primitive collection allocation:

- `iterable.collectInt(function).sum()` → `iterable.sumOfInt(function)`
- `iterable.collectLong(function).sum()` → `iterable.sumOfLong(function)`

### Stream to Native Method Conversions

These recipes replace Java Stream operations with native Eclipse Collections methods for better performance and readability.

#### ECStreamCountToCount

Replace stream().filter().count() with count() on Eclipse Collections types:

- `collection.stream().filter(predicate).count()` -> `collection.count(predicate)`

This transformation eliminates the unnecessary Stream intermediary since Eclipse Collections has the count method directly on RichIterable.

**Note**: Eclipse Collections `count()` returns `int`, while `stream().filter().count()` returns `long`. This transformation is safe when the count value fits in an int (up to 2^31-1 elements).

#### ECStreamMatchToSatisfy

Replace stream match operations with Eclipse Collections satisfy operations:

- `collection.stream().anyMatch(pred)` -> `collection.anySatisfy(pred)`
- `collection.stream().allMatch(pred)` -> `collection.allSatisfy(pred)`
- `collection.stream().noneMatch(pred)` -> `collection.noneSatisfy(pred)`

#### ECStreamForEach

Replace stream().forEach() with forEach() on Eclipse Collections types:

- `collection.stream().forEach(action)` -> `collection.forEach(action)`

#### ECStreamFindFirstToDetectOptional

Replace stream().filter().findFirst() with detectOptional() on Eclipse Collections types:

- `collection.stream().filter(predicate).findFirst()` -> `collection.detectOptional(predicate)`

#### ECSelectFirstToDetect

Replace select().getFirstOptional()/getFirst() chains with detectOptional()/detect():

- `collection.select(predicate).getFirstOptional()` -> `collection.detectOptional(predicate)`
- `collection.select(predicate).getFirst()` -> `collection.detect(predicate)`
- `ArrayIterate.select(array, predicate).getFirstOptional()` -> `ArrayIterate.detectOptional(array, predicate)`
- `ArrayIterate.select(array, predicate).getFirst()` -> `ArrayIterate.detect(array, predicate)`
- `ListIterate.select(list, predicate).getFirstOptional()` -> `ListIterate.detectOptional(list, predicate)`
- `ListIterate.select(list, predicate).getFirst()` -> `ListIterate.detect(list, predicate)`

#### ECSelectNotEmptyToAnySatisfy

Replace select().notEmpty() chains with anySatisfy():

- `collection.select(predicate).notEmpty()` -> `collection.anySatisfy(predicate)`
- `ArrayIterate.select(array, predicate).notEmpty()` -> `ArrayIterate.anySatisfy(array, predicate)`
- `ListIterate.select(list, predicate).notEmpty()` -> `ListIterate.anySatisfy(list, predicate)`

#### ECSelectIsEmptyToNoneSatisfy

Replace select().isEmpty() chains with noneSatisfy():

- `collection.select(predicate).isEmpty()` -> `collection.noneSatisfy(predicate)`
- `ArrayIterate.select(array, predicate).isEmpty()` -> `ArrayIterate.noneSatisfy(array, predicate)`
- `ListIterate.select(list, predicate).isEmpty()` -> `ListIterate.noneSatisfy(list, predicate)`

#### ECSelectSizeToCount

Replace select().size() chains with count():

- `collection.select(predicate).size()` -> `collection.count(predicate)`
- `ArrayIterate.select(array, predicate).size()` -> `ArrayIterate.count(array, predicate)`
- `ListIterate.select(list, predicate).size()` -> `ListIterate.count(list, predicate)`

The count() method is more efficient as it avoids creating an intermediate collection.

#### ECStreamReduceToInjectInto

Replace stream().reduce() with injectInto() on Eclipse Collections types:

- `collection.stream().reduce(identity, accumulator)` -> `collection.injectInto(identity, accumulator)`

#### ECStreamMinMaxToMinMax

Replace stream min/max operations with Eclipse Collections min/max:

- `collection.stream().min(comparator)` -> `collection.minOptional(comparator)`
- `collection.stream().max(comparator)` -> `collection.maxOptional(comparator)`

#### ECStreamAnyMatchToContains

Replace stream().anyMatch() with contains() when checking for value equality:

- `collection.stream().anyMatch(value::equals)` -> `collection.contains(value)`

#### ECStreamFlatMapCollectToFlatCollect

Replace stream().flatMap(fn).collect() with flatCollect(fn) on Eclipse Collections types:

- `collection.stream().flatMap(x -> x.items().stream()).collect(Collectors.toList())` -> `collection.flatCollect(x -> x.items())`
- `collection.stream().flatMap(x -> x.items().stream()).collect(Collectors.toSet())` -> `collection.flatCollect(x -> x.items()).toSet()`

The lambda body must end with a `.stream()` call, which is stripped since `flatCollect` expects an `Iterable` rather than a `Stream`.

#### ECStreamCollectToMapToGroupByUniqueKey

Replace stream().collect(Collectors.toMap(keyFn, Function.identity())) with groupByUniqueKey(keyFn) on Eclipse Collections types:

- `collection.stream().collect(Collectors.toMap(Donut::code, Function.identity()))` -> `collection.groupByUniqueKey(Donut::code)`

This recipe only matches the two-argument form of `Collectors.toMap` where the value mapper is `Function.identity()`. The `groupByUniqueKey` method returns a `MutableMap<K, V>` directly, eliminating stream and collector boilerplate.

#### ECStreamCollectGroupingByToGroupBy

Replace stream().collect(Collectors.groupingBy(...)) with groupBy() on Eclipse Collections types:

- `collection.stream().collect(Collectors.groupingBy(fn))` -> `collection.groupBy(fn)`
- `collection.stream().collect(Collectors.groupingBy(fn, Collectors.toList()))` -> `collection.groupBy(fn)`
- `collection.stream().collect(Collectors.groupingBy(fn, Collectors.toSet()))` -> `collection.groupBy(fn)`

The `groupBy` method returns a `Multimap<K, V>` which is richer than `Map<K, Collection<V>>`. This recipe only matches the one-argument form and the two-argument form with `Collectors.toList()` or `Collectors.toSet()` as the downstream collector. The three-argument form with a map factory is not transformed.

#### ECStreamCollectGroupingByIdentityCountingToToBag

Replace stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())) with toBag() on Eclipse Collections types:

- `collection.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))` -> `collection.toBag()`

The `toBag()` method returns a `Bag<T>` which provides `occurrencesOf()` and `topOccurrences()` out of the box, replacing manual Map-based word counting patterns. This recipe only matches the two-argument form of `Collectors.groupingBy` with `Function.identity()` as the classifier and `Collectors.counting()` as the downstream collector.

#### ECStreamCollectGroupingByCountingToCountBy

Replace stream().collect(Collectors.groupingBy(fn, Collectors.counting())) with countBy(fn) on Eclipse Collections types:

- `collection.stream().collect(Collectors.groupingBy(fn, Collectors.counting()))` -> `collection.countBy(fn)`

The `countBy` method returns a `Bag<K>` which provides `occurrencesOf()` and `topOccurrences()` instead of `Map<K, Long>`. This recipe only matches the two-argument form of `Collectors.groupingBy` with `Collectors.counting()` as the downstream collector.

#### ECStreamCollectPartitioningByToPartition

Replace stream().collect(Collectors.partitioningBy(pred)) with partition(pred) on Eclipse Collections types:

- `collection.stream().collect(Collectors.partitioningBy(pred))` -> `collection.partition(pred)`

The `partition` method returns a `PartitionIterable` with `getSelected()` and `getRejected()` instead of `Map<Boolean, List<T>>` with `get(true)`/`get(false)`. This recipe only matches the one-argument form of `Collectors.partitioningBy`. The two-argument form with a downstream collector is not transformed.

#### ECStreamCollectSummarizingToCollectPrimitive

Replace stream().collect(Collectors.summarizing*(fn)) with collect*(fn).summaryStatistics() on Eclipse Collections types:

- `collection.stream().collect(Collectors.summarizingDouble(fn))` -> `collection.collectDouble(fn).summaryStatistics()`
- `collection.stream().collect(Collectors.summarizingInt(fn))` -> `collection.collectInt(fn).summaryStatistics()`
- `collection.stream().collect(Collectors.summarizingLong(fn))` -> `collection.collectLong(fn).summaryStatistics()`

This eliminates the unnecessary Stream intermediary since Eclipse Collections has the collectDouble/collectInt/collectLong methods directly on RichIterable, and the resulting primitive collections have summaryStatistics().

#### ECStreamMapToPrimitiveSumToCollectPrimitiveSum

Replace stream().mapToDouble/mapToInt/mapToLong(fn).sum() with collectDouble/collectInt/collectLong(fn).sum() on Eclipse Collections types:

- `collection.stream().mapToDouble(fn).sum()` -> `collection.collectDouble(fn).sum()`
- `collection.stream().mapToInt(fn).sum()` -> `collection.collectInt(fn).sum()`
- `collection.stream().mapToLong(fn).sum()` -> `collection.collectLong(fn).sum()`

This eliminates the unnecessary Stream intermediary since Eclipse Collections has the collectDouble/collectInt/collectLong methods directly on RichIterable, and the resulting primitive collections have sum().

#### ECStreamSortedCollectToSortedListBy

Replace stream().sorted(Comparator.comparing(fn)).collect(Collectors.toList()) with toSortedListBy(fn) on Eclipse Collections types:

- `collection.stream().sorted(Comparator.comparing(fn)).collect(Collectors.toList())` -> `collection.toSortedListBy(fn)`

This eliminates the unnecessary Stream intermediary since Eclipse Collections has the toSortedListBy method directly on RichIterable. The recipe only matches when Comparator.comparing() is used as the sort key extractor.

#### ECStreamCollectToUnmodifiableToToImmutable

Replace stream().collect(Collectors.toUnmodifiableList/Set()) with toImmutableList/Set() on Eclipse Collections types:

- `collection.stream().collect(Collectors.toUnmodifiableList())` -> `collection.toImmutableList()`
- `collection.stream().collect(Collectors.toUnmodifiableSet())` -> `collection.toImmutableSet()`

This eliminates the unnecessary Stream intermediary since Eclipse Collections has the toImmutableList and toImmutableSet methods directly on RichIterable. Eclipse Collections immutable types provide a richer API than JDK unmodifiable wrappers.

### Map Method Conversions

#### ECMapGetOrDefaultToGetIfAbsentValue

Replace JDK `Map.getOrDefault()` with Eclipse Collections `MapIterable.getIfAbsentValue()`:

- `map.getOrDefault(key, defaultValue)` -> `map.getIfAbsentValue(key, defaultValue)`

This replaces the JDK Map method with the idiomatic Eclipse Collections equivalent. Only applies to Eclipse Collections map types (MutableMap, etc.), not plain JDK maps.

### Operation Ordering Optimizations

#### ECSelectBeforeSortThis

Reorder sortThis() and select()/reject() operations to filter before sorting for better performance:

- `list.sortThis().select(predicate)` -> `list.select(predicate).sortThis()`
- `list.sortThis(comparator).select(predicate)` -> `list.select(predicate).sortThis(comparator)`
- `list.sortThis().reject(predicate)` -> `list.reject(predicate).sortThis()`
- `list.sortThis(comparator).reject(predicate)` -> `list.reject(predicate).sortThis(comparator)`

### Constructor to Factory

#### ECListConstructorToFactory

Replace Eclipse Collections list constructors with factory methods:

- `new FastList<>()` → `Lists.mutable.empty()`
- `new FastList<String>()` → `Lists.mutable.<String>empty()`

#### ECSetConstructorToFactory

Replace Eclipse Collections set constructors with factory methods:

- `new UnifiedSet<>()` → `Sets.mutable.empty()`
- `new UnifiedSet<String>()` → `Sets.mutable.<String>empty()`

#### ECMapConstructorToFactory

Replace Eclipse Collections map constructors with factory methods:

- `new UnifiedMap<>()` → `Maps.mutable.empty()`
- `new UnifiedMap<String, Integer>()` → `Maps.mutable.<String, Integer>empty()`

#### ECIntStreamRangeClosedToIntInterval

Replace `IntStream.rangeClosed()` with Eclipse Collections `IntInterval.fromTo()`:

- `IntStream.rangeClosed(from, to)` -> `IntInterval.fromTo(from, to)`

#### ECStreamGatherWindowFixedToChunk

Replace Java 24+ `Gatherers.windowFixed(n)` with Eclipse Collections `chunk(n)`:

- `collection.stream().gather(Gatherers.windowFixed(n)).collect(Collectors.toList())` -> `collection.chunk(n)`

This recipe targets Java 24+ code using the Gatherers API (JEP 485). Eclipse Collections' `chunk(n)` method on `RichIterable` returns a `LazyIterable<RichIterable<T>>` which provides a rich collection API without the Stream intermediary.

#### ECStreamGatherFoldToInjectInto

Replace Java 24+ `Gatherers.fold()` with Eclipse Collections `injectInto()`:

- `collection.stream().gather(Gatherers.fold(() -> init, folder)).findFirst().orElseThrow()` -> `collection.injectInto(init, folder)`

This recipe targets Java 24+ code using the Gatherers API (JEP 485). `Gatherers.fold()` returns a single-element stream requiring `findFirst()` to extract the value. Eclipse Collections' `injectInto()` method on `RichIterable` is the native fold operation and eliminates the Stream intermediary.

#### ECArraysAsListToWith

Replace verbose collection creation patterns with Eclipse Collections factory methods:

- `FastList.newList(Arrays.asList(a, b, c))` → `Lists.mutable.with(a, b, c)`
- `UnifiedSet.newSet(Arrays.asList(a, b, c))` → `Sets.mutable.with(a, b, c)`
- `HashBag.newBag(Arrays.asList(a, b, c))` → `Bags.mutable.with(a, b, c)`

#### ECArraysStreamToArrayAdapter

Replace `Arrays.stream(array)` with Eclipse Collections `ArrayAdapter.adapt(array)`:

- `Arrays.stream(values)` -> `ArrayAdapter.adapt(values)`

`ArrayAdapter` wraps an array as a `RichIterable`, providing `select`, `collect`, `groupBy`, and other Eclipse Collections operations without copying the array. Only matches `Object[]` arrays, not primitive arrays (`int[]`, `long[]`, `double[]`).

#### ArrayIterateEmpty

Replace manual array null and length checks with ArrayIterate utility methods:

- `array == null || array.length == 0` → `ArrayIterate.isEmpty(array)`
- `array == null || array.length <= 0` → `ArrayIterate.isEmpty(array)`
- `array == null || array.length < 1` → `ArrayIterate.isEmpty(array)`
- `array != null && array.length > 0` → `ArrayIterate.notEmpty(array)`
- `array != null && array.length != 0` → `ArrayIterate.notEmpty(array)`
- `array != null && array.length >= 1` → `ArrayIterate.notEmpty(array)`
- `!ArrayIterate.isEmpty(array)` → `ArrayIterate.notEmpty(array)`
- `!ArrayIterate.notEmpty(array)` → `ArrayIterate.isEmpty(array)`

#### MapIterateEmpty

Replace manual map null and isEmpty checks with MapIterate utility methods:

- `map == null || map.isEmpty()` → `MapIterate.isEmpty(map)`
- `map != null && !map.isEmpty()` → `MapIterate.notEmpty(map)`
- `!MapIterate.isEmpty(map)` → `MapIterate.notEmpty(map)`
- `!MapIterate.notEmpty(map)` → `MapIterate.isEmpty(map)`

#### IterateEmpty

Replace manual collection null and isEmpty checks with Iterate utility methods:

- `collection == null || collection.isEmpty()` → `Iterate.isEmpty(collection)`
- `collection != null && !collection.isEmpty()` → `Iterate.notEmpty(collection)`
- `!Iterate.isEmpty(iterable)` → `Iterate.notEmpty(iterable)`
- `!Iterate.notEmpty(iterable)` → `Iterate.isEmpty(iterable)`

#### IterateToArrayIterate

Replace Iterate method calls on Arrays.asList() with ArrayIterate for better performance:

- `Iterate.anySatisfy(Arrays.asList(array), predicate)` → `ArrayIterate.anySatisfy(array, predicate)`
- `Iterate.allSatisfy(Arrays.asList(array), predicate)` → `ArrayIterate.allSatisfy(array, predicate)`
- `Iterate.noneSatisfy(Arrays.asList(array), predicate)` → `ArrayIterate.noneSatisfy(array, predicate)`
- `Iterate.detect(Arrays.asList(array), predicate)` → `ArrayIterate.detect(array, predicate)`
- `Iterate.count(Arrays.asList(array), predicate)` → `ArrayIterate.count(array, predicate)`
- `Iterate.collect(Arrays.asList(array), function)` → `ArrayIterate.collect(array, function)`
- `Iterate.forEach(Arrays.asList(array), procedure)` → `ArrayIterate.forEach(array, procedure)`
- `Iterate.getFirst(Arrays.asList(array))` → `ArrayIterate.getFirst(array)`
- `Iterate.getLast(Arrays.asList(array))` → `ArrayIterate.getLast(array)`

### Type Declaration

#### JCFListToMutableList

Replace java.util.List declarations with MutableList when initialized with Eclipse Collections:

- `List<String> list = Lists.mutable.empty()` → `MutableList<String> list = Lists.mutable.empty()`
- `List list = Lists.mutable.empty()` → `MutableList list = Lists.mutable.empty()`

#### JCFSetToMutableSet

Replace java.util.Set declarations with MutableSet when initialized with Eclipse Collections:

- `Set<String> set = Sets.mutable.empty()` → `MutableSet<String> set = Sets.mutable.empty()`
- `Set set = Sets.mutable.empty()` → `MutableSet set = Sets.mutable.empty()`

#### JCFMapToMutableMap

Replace java.util.Map declarations with MutableMap when initialized with Eclipse Collections:

- `Map<String, Integer> map = Maps.mutable.empty()` → `MutableMap<String, Integer> map = Maps.mutable.empty()`
- `Map map = Maps.mutable.empty()` → `MutableMap map = Maps.mutable.empty()`

## Adoption Recipes

### JCFListConstructorToFactory

Replace ArrayList constructor calls with Eclipse Collections factory methods:

- `new ArrayList<>()` → `Lists.mutable.empty()`
- `new ArrayList<String>()` → `Lists.mutable.<String>empty()`

### JCFMapConstructorToFactory

Replace map constructor calls with Eclipse Collections factory methods:

- `new HashMap<>()` → `Maps.mutable.empty()`
- `new HashMap<String, Integer>()` → `Maps.mutable.<String, Integer>empty()`
- `new TreeMap<>()` → `SortedMaps.mutable.empty()`
- `new TreeMap<String, Integer>()` → `SortedMaps.mutable.<String, Integer>empty()`

### JCFSetConstructorToFactory

Replace set constructor calls with Eclipse Collections factory methods:

- `new HashSet<>()` → `Sets.mutable.empty()`
- `new HashSet<String>()` → `Sets.mutable.<String>empty()`
- `new TreeSet<>()` → `SortedSets.mutable.empty()`
- `new TreeSet<String>()` → `SortedSets.mutable.<String>empty()`

### JCFCollectionsToFactories

Replace Collections utility methods with Eclipse Collections factories:

- `Collections.emptyList()` → `Lists.fixedSize.empty()`
- `Collections.emptyMap()` → `Maps.fixedSize.empty()`
- `Collections.emptySet()` → `Sets.fixedSize.empty()`
- `Collections.singletonList(element)` → `Lists.fixedSize.of(element)`
- `Collections.singleton(element)` → `Sets.fixedSize.of(element)`
- `Collections.singletonMap(key, value)` → `Maps.fixedSize.of(key, value)`

### CollectionsSynchronizedToAsSynchronized

Replace Collections.synchronized\*() methods with Eclipse Collections asSynchronized():

- `Collections.synchronizedCollection(collection)` → `collection.asSynchronized()`
- `Collections.synchronizedList(list)` → `list.asSynchronized()`
- `Collections.synchronizedMap(map)` → `map.asSynchronized()`
- `Collections.synchronizedSet(set)` → `set.asSynchronized()`

### CollectionsUnmodifiableToAsUnmodifiable

Replace Collections.unmodifiable\*() methods with Eclipse Collections asUnmodifiable():

- `Collections.unmodifiableCollection(collection)` → `collection.asUnmodifiable()`
- `Collections.unmodifiableList(list)` → `list.asUnmodifiable()`
- `Collections.unmodifiableMap(map)` → `map.asUnmodifiable()`
- `Collections.unmodifiableSet(set)` → `set.asUnmodifiable()`

### IterateGetFirst

Replace iterator().next() and listIterator().next() calls with Iterate.getFirst() for safer first element access:

- `collection.iterator().next()` → `Iterate.getFirst(collection)`
- `list.listIterator().next()` → `Iterate.getFirst(list)`

## Eclipse Collections Removal Recipes

These recipes replace Eclipse Collections APIs with standard Java alternatives where appropriate.

### ComparatorsNullSafeEqualsToObjectsEquals

Replace Comparators.nullSafeEquals() with Objects.equals():

- `Comparators.nullSafeEquals(a, b)` → `Objects.equals(a, b)`

### CollectionAddProcedureOnToMethodReference

Replace CollectionAddProcedure usage with method reference:

- `CollectionAddProcedure.on(collection)` → `collection::add`
- `new CollectionAddProcedure<>(collection)` → `collection::add`

### CollectionRemoveProcedureOnToMethodReference

Replace CollectionRemoveProcedure usage with method reference:

- `CollectionRemoveProcedure.on(collection)` → `collection::remove`
- `new CollectionRemoveProcedure<>(collection)` → `collection::remove`

## ⚠️ Unsafe Adoption Patterns

These recipes help adopt Eclipse Collections patterns but may change program semantics in some circumstances. Apply with careful review.

### ECDetectToSatisfies

Replace detect() != null patterns with anySatisfy():

- `list.detect(predicate) != null` → `list.anySatisfy(predicate)`
- `list.detect(predicate) == null` → `list.noneSatisfy(predicate)`

**Warning**: This transformation changes semantics when the collection contains null values. The original pattern distinguishes between "found null" and "not found", while anySatisfy/noneSatisfy only check predicate satisfaction.

## Dropwizard Testing Migration Recipes

The `io.liftwizard.rewrite.dropwizard.testing.DropwizardTestingJUnit5Migration` composite recipe migrates Dropwizard JUnit 4 testing rules to their JUnit 5 extension equivalents. Each sub-recipe changes the type, replaces `@ClassRule`/`@Rule` with `@RegisterExtension`, and adds `@ExtendWith(DropwizardExtensionsSupport.class)` to the test class.

### DropwizardAppRuleToLiftwizardAppExtension

Replace `DropwizardAppRule` with Liftwizard's `LiftwizardAppExtension`:

```java
// Before
@ClassRule
public static DropwizardAppRule<MyConfig> RULE =
        new DropwizardAppRule<>(MyApp.class, "config.yml");

// After
@ExtendWith(DropwizardExtensionsSupport.class)
class MyTest {
    @RegisterExtension
    public static LiftwizardAppExtension<MyConfig> RULE =
            new LiftwizardAppExtension<>(MyApp.class, "config.yml");
}
```

### DropwizardClientRuleToExtension

Replace `DropwizardClientRule` with `DropwizardClientExtension`:

```java
// Before
@ClassRule
public static DropwizardClientRule RULE =
        new DropwizardClientRule(new MyResource());

// After
@ExtendWith(DropwizardExtensionsSupport.class)
class MyTest {
    @RegisterExtension
    public static DropwizardClientExtension RULE =
            new DropwizardClientExtension(new MyResource());
}
```

### ResourceTestRuleToExtension

Replace `ResourceTestRule` with `ResourceExtension`:

```java
// Before
@ClassRule
public static ResourceTestRule RESOURCES = ResourceTestRule.builder()
        .addResource(new MyResource())
        .build();

// After
@ExtendWith(DropwizardExtensionsSupport.class)
class MyTest {
    @RegisterExtension
    public static ResourceExtension RESOURCES = ResourceExtension.builder()
            .addResource(new MyResource())
            .build();
}
```
