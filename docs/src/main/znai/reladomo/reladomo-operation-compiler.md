# ReladomoOperationCompiler

The `ReladomoOperationCompiler` can take a String and compile it into a Reladomo [Operation](https://www.mvndoc.com/c/com.goldmansachs.reladomo/reladomo/com/gs/fw/finder/Operation.html). This can be used for dynamic ad-hoc queries, and combines well with [Liftwizard's GraphQL features](graphql/bundle.md).

```java
RelatedFinder finder        = ...;
String        operationText = ...;
var           compiler      = new ReladomoOperationCompiler();
Operation     operation     = compiler.compile(finder, operationText);
MithraList<?> mithraList    = finder.findMany(operation);
```

## Compiling toString() representation

The syntax closely matches the `toString()` representation of Reladomo's Operations, with a little added flexibility. In general, you can call `toString()` and compile the output and get back an equivalent Operation.

```java
Operation operation     = ...;
String    operationText = operation.toString();
Operation recompiled    = compiler.compile(finder, operationText);
assertThat(recompiled, is(operation));
```

## Flexible syntax

The compiler allows some flexibility in the syntax.

:include-table: flexible-syntax.json

## Complete examples

```
# Attribute types
this.booleanProperty = true
this.integerProperty = 4
this.longProperty = 5
this.floatProperty = 6.6
this.doubleProperty = 7.7
this.dateProperty = "2010-12-31"
this.timeProperty = "2010-12-31T23:59:00.0Z"
this.stringProperty = "Value"
this.system = "2010-12-31T23:59:00.0Z"

# Conjunctions
this.booleanProperty = true & this.integerProperty = 4
this.booleanProperty = true && this.integerProperty = 4
this.booleanProperty = true and this.integerProperty = 4
this.booleanProperty = true | this.integerProperty = 4
this.booleanProperty = true || this.integerProperty = 4
this.booleanProperty = true or this.integerProperty = 4

# Equality operators
this.stringProperty = "Value"
this.stringProperty != "Value"
this.stringProperty is null
this.stringProperty == null
this.stringProperty is not null
this.stringProperty != null
this.stringProperty in ["Value", "Value2", null]
this.stringProperty not in ["Value", "Value2", null]

# String operators
this.stringProperty endsWith "Value"
this.stringProperty contains "Value"
this.stringProperty startsWith "Value"
this.stringProperty wildCardEquals "Value?"
this.stringProperty not endsWith "Value"
this.stringProperty not contains "Value"
this.stringProperty not startsWith "Value"
this.stringProperty not wildCardEquals "Value?"

# Numeric operators
this.stringProperty > "Value"
this.stringProperty >= "Value"
this.stringProperty < "Value"
this.stringProperty <= "Value"

# Functions / derived attributes
toLowerCase(this.stringProperty) = "value"
substring(this.stringProperty, 2, 3) = "value"
substring(toLowerCase(this.stringProperty), 2, 3) = "value"

# Flexible number literals
this.floatProperty = 42.0f
this.floatProperty = 42.0d
this.floatProperty = 42
this.doubleProperty = 42.0f
this.doubleProperty = 42.0d
this.doubleProperty = 42
this.longProperty = 10_000_000_000
this.integerProperty = 1_000_000_000

# Number / date functions / derived attributes
abs(this.integerProperty) = 1

year(this.timeProperty) = 1999
month(this.timeProperty) = 12
dayOfMonth(this.timeProperty) = 31
year(this.dateProperty) = 1999
month(this.dateProperty) = 12
dayOfMonth(this.dateProperty) = 31

# Relationships
this.target.value = "value"
this.target exists
this.target not exists
this.target { RelatedType.source.value = "value" } not exists

# Edge points
this.system equalsEdgePoint
```
