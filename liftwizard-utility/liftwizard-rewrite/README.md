# Liftwizard OpenRewrite Recipes

This module provides OpenRewrite recipes for Eclipse Collections code transformations.

## Recipe Collections

Three recipe collections:

- **Eclipse Collections Best Practices**: Optimize existing Eclipse Collections usage
- **Eclipse Collections Adoption**: Migrate from Java Collections Framework to Eclipse Collections
- **Eclipse Collections Risky Adoption Patterns**: Adoption patterns that may change code semantics when collections contain nulls

## Best Practices Recipes

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

#### ECUtilityNegatedEmpty

Simplify negated empty/notEmpty calls on Eclipse Collections utility classes:

- `!Iterate.isEmpty(iterable)` → `Iterate.notEmpty(iterable)`
- `!Iterate.notEmpty(iterable)` → `Iterate.isEmpty(iterable)`
- `!MapIterate.isEmpty(map)` → `MapIterate.notEmpty(map)`
- `!MapIterate.notEmpty(map)` → `MapIterate.isEmpty(map)`
- `!ArrayIterate.isEmpty(array)` → `ArrayIterate.notEmpty(array)`
- `!ArrayIterate.notEmpty(array)` → `ArrayIterate.isEmpty(array)`

### Satisfies Patterns

#### ECCountToSatisfies

Replace count comparisons with anySatisfy/noneSatisfy:

- `list.count(predicate) == 0` → `list.noneSatisfy(predicate)`
- `list.count(predicate) > 0` → `list.anySatisfy(predicate)`
- `list.count(predicate) != 0` → `list.anySatisfy(predicate)`
- `list.count(predicate) <= 0` → `list.noneSatisfy(predicate)`
- `list.count(predicate) >= 1` → `list.anySatisfy(predicate)`

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

### Other Patterns

#### ECNullSafeEquals

Replace complex null-safe equality patterns with Comparators.nullSafeEquals():

- `left == null ? right == null : left.equals(right)` → `Comparators.nullSafeEquals(left, right)`
- `left == null ? right != null : !left.equals(right)` → `!Comparators.nullSafeEquals(left, right)`
- `left == right || left != null && left.equals(right)` → `Comparators.nullSafeEquals(left, right)`

#### ECCollectionAddProcedureToFactory

Replace CollectionAddProcedure constructor calls with factory method:

- `new CollectionAddProcedure<>(collection)` → `CollectionAddProcedure.on(collection)`

#### ECCollectionRemoveProcedureToFactory

Replace CollectionRemoveProcedure constructor calls with factory method:

- `new CollectionRemoveProcedure<>(collection)` → `CollectionRemoveProcedure.on(collection)`

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

## Risky Adoption Patterns

These recipes help adopt Eclipse Collections patterns but may change program semantics in some circumstances. Apply with careful review.

### ECDetectToSatisfies

Replace detect() != null patterns with anySatisfy():

- `list.detect(predicate) != null` → `list.anySatisfy(predicate)`
- `list.detect(predicate) == null` → `list.noneSatisfy(predicate)`

**Warning**: This transformation changes semantics when the collection contains null values. The original pattern distinguishes between "found null" and "not found", while anySatisfy/noneSatisfy only check predicate satisfaction.
