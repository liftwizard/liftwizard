# 📋 Liftwizard OpenRewrite Recipe Reference

This document provides a complete listing of all OpenRewrite recipes in the `liftwizard-rewrite` module with transformation examples.

For getting started instructions, see [README.md](README.md).

## Composite Recipes

Six composite recipes are available:

| Composite Recipe                                                                             | Description                                                    |
| -------------------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| `io.liftwizard.rewrite.BestPractices`                                                        | General Java best practices for null-safety                    |
| `io.liftwizard.rewrite.assertj.AssertJMigration`                                             | Migrate from Eclipse Collections testutils to AssertJ          |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsBestPractices`                  | Optimize existing Eclipse Collections usage                    |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsAdoption`                       | Migrate from Java Collections Framework to Eclipse Collections |
| `io.liftwizard.rewrite.eclipse.collections.EclipseCollectionsRemoval`                        | Replace Eclipse Collections APIs with Java alternatives        |
| `io.liftwizard.rewrite.eclipse.collections.adoption.unsafe.EclipseCollectionsAdoptionUnsafe` | Adoption patterns that may change semantics with nulls         |

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

#### ECArraysAsListToWith

Replace verbose collection creation patterns with Eclipse Collections factory methods:

- `FastList.newList(Arrays.asList(a, b, c))` → `Lists.mutable.with(a, b, c)`
- `UnifiedSet.newSet(Arrays.asList(a, b, c))` → `Sets.mutable.with(a, b, c)`
- `HashBag.newBag(Arrays.asList(a, b, c))` → `Bags.mutable.with(a, b, c)`

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
