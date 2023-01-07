/*
 * Copyright 2020 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.reladomo.graphql.operation;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.BooleanAttribute;
import com.gs.fw.common.mithra.attribute.DateAttribute;
import com.gs.fw.common.mithra.attribute.DoubleAttribute;
import com.gs.fw.common.mithra.attribute.FloatAttribute;
import com.gs.fw.common.mithra.attribute.IntegerAttribute;
import com.gs.fw.common.mithra.attribute.LongAttribute;
import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.impl.factory.primitive.BooleanSets;
import org.eclipse.collections.impl.factory.primitive.DoubleSets;
import org.eclipse.collections.impl.factory.primitive.FloatSets;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.collections.impl.utility.Iterate;

public class GraphQLQueryToOperationConverter
{
    private final MutableStack<String> context = Stacks.mutable.empty();

    public Operation convert(AbstractRelatedFinder finder, Map<?, ?> inputOperation)
    {
        if (inputOperation.size() > 1 && (inputOperation.containsKey("AND") || inputOperation.containsKey("OR")))
        {
            throw new LiftwizardGraphQLContextException(
                    "AND and OR cannot be combined with other criteria. Did you mean to nest the other criteria inside?",
                    this.getContext());
        }

        List<Operation> nestedOperations = inputOperation
                .entrySet()
                .stream()
                .map(each -> this.convert(
                        finder,
                        (String) each.getKey(),
                        each.getValue()))
                .collect(Collectors.toList());

        return nestedOperations.stream().reduce(finder.all(), Operation::and);
    }

    private Operation convert(AbstractRelatedFinder finder, String key, Object graphQlOperation)
    {
        if (key.equals("AND"))
        {
            return this.convertConjunction(finder, graphQlOperation, "AND", Operation::and);
        }

        if (key.equals("OR"))
        {
            return this.convertConjunction(finder, graphQlOperation, "OR", Operation::or);
        }

        if (key.equals("exists"))
        {
            if (!graphQlOperation.equals(Maps.immutable.empty()))
            {
                throw new LiftwizardGraphQLContextException("Expected empty criteria node for exists but found " + graphQlOperation, this.getContext());
            }

            return finder.exists();
        }

        if (key.equals("notExists"))
        {
            if (!graphQlOperation.equals(Maps.immutable.empty()))
            {
                var converter = new GraphQLQueryToOperationConverter();
                Operation nestedOperation = converter.convert(finder.zWithoutParent(), (Map<?, ?>) graphQlOperation);
                return finder.notExists(nestedOperation);
            }

            return finder.notExists();
        }

        if (key.equals("recursiveNotExists"))
        {
            if (!graphQlOperation.equals(Maps.immutable.empty()))
            {
                var converter = new GraphQLQueryToOperationConverter();
                Operation nestedOperation = converter.convert(finder.zWithoutParent(), (Map<?, ?>) graphQlOperation);
                return finder.recursiveNotExists(nestedOperation);
            }

            return finder.recursiveNotExists();
        }

        return this.convertField(finder, key, graphQlOperation);
    }

    private Operation convertConjunction(
            AbstractRelatedFinder finder,
            Object graphQlOperation,
            String conjunctionName,
            BinaryOperator<Operation> conjunctionFunction)
    {
        if (graphQlOperation.equals(Lists.immutable.empty()))
        {
            this.context.push(conjunctionName);
            try
            {
                throw new LiftwizardGraphQLContextException("Empty criteria node.", this.getContext());
            }
            finally
            {
                this.context.pop();
            }
        }

        List<?> nestedGraphQlOperations = (List<?>) graphQlOperation;
        MutableList<Operation> nestedOperations =
                ListAdapter.adapt(nestedGraphQlOperations).collectWithIndex((nestedGraphQlOperation, index) ->
                {
                    this.context.push(conjunctionName + "[" + index + "]");
                    try
                    {
                        return this.convert(finder, (Map<?, ?>) nestedGraphQlOperation);
                    }
                    finally
                    {
                        this.context.pop();
                    }
                });
        return nestedOperations.reduce(conjunctionFunction).get();
    }

    private Operation convertField(RelatedFinder finder, String key, Object graphQlOperation)
    {
        this.context.push(key);

        try
        {
            if (graphQlOperation.equals(Lists.immutable.empty()) || graphQlOperation.equals(Maps.immutable.empty()))
            {
                throw new LiftwizardGraphQLContextException("Empty criteria node.", this.getContext());
            }

            Attribute attributeByName = finder.getAttributeByName(key);
            if (attributeByName != null)
            {
                return this.convertAttribute(finder, attributeByName, (Map<String, ?>) graphQlOperation);
            }

            AbstractRelatedFinder relationshipFinderByName = (AbstractRelatedFinder) finder.getRelationshipFinderByName(key);
            if (relationshipFinderByName != null)
            {
                return this.convertRelationship(
                        relationshipFinderByName,
                        (Map<String, ?>) graphQlOperation);
            }

            throw new LiftwizardGraphQLContextException("Could not find field " + key, this.getContext());
        }
        finally
        {
            this.context.pop();
        }
    }

    private Operation convertAttribute(
            RelatedFinder finderInstance,
            Attribute attribute,
            Map<String, ?> graphQlOperation)
    {
        if (attribute instanceof StringAttribute)
        {
            return this.convertStringAttribute(finderInstance, (StringAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof BooleanAttribute)
        {
            return this.convertBooleanAttribute(finderInstance, (BooleanAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof IntegerAttribute)
        {
            return this.convertIntegerAttribute(finderInstance, (IntegerAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof LongAttribute)
        {
            return this.convertLongAttribute(finderInstance, (LongAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof DoubleAttribute)
        {
            return this.convertDoubleAttribute(finderInstance, (DoubleAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof FloatAttribute)
        {
            return this.convertFloatAttribute(finderInstance, (FloatAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof DateAttribute)
        {
            return this.convertDateAttribute(finderInstance, (DateAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof TimestampAttribute)
        {
            return this.convertTimestampAttribute(finderInstance, (TimestampAttribute) attribute, graphQlOperation);
        }
        if (attribute instanceof AsOfAttribute)
        {
            return this.convertAsOfAttribute(finderInstance, (AsOfAttribute) attribute, graphQlOperation);
        }
        throw new AssertionError(attribute.getClass().getSuperclass().getCanonicalName());
    }

    private Operation convertStringAttribute(
            RelatedFinder finderInstance,
            StringAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertStringAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertStringAttribute(
            RelatedFinder finderInstance,
            StringAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                return attribute.eq((String) operationParameter);
            }
            case "notEq":
            {
                return attribute.notEq((String) operationParameter);
            }
            case "in":
            {
                return attribute.in(new LinkedHashSet<String>((Collection<? extends String>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(new LinkedHashSet<String>((Collection<? extends String>) operationParameter));
            }
            case "greaterThan":
            {
                return attribute.greaterThan((String) operationParameter);
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals((String) operationParameter);
            }
            case "lessThan":
            {
                return attribute.lessThan((String) operationParameter);
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals((String) operationParameter);
            }
            case "startsWith":
            {
                return attribute.startsWith((String) operationParameter);
            }
            case "notStartsWith":
            {
                return attribute.notStartsWith((String) operationParameter);
            }
            case "endsWith":
            {
                return attribute.endsWith((String) operationParameter);
            }
            case "notEndsWith":
            {
                return attribute.notEndsWith((String) operationParameter);
            }
            case "contains":
            {
                return attribute.contains((String) operationParameter);
            }
            case "notContains":
            {
                return attribute.notContains((String) operationParameter);
            }
            case "lower":
            case "toLowerCase":
            {
                return this.convertStringAttribute(
                        finderInstance,
                        attribute.toLowerCase(),
                        (Map<String, ?>) operationParameter);
            }
            case "wildCardEq":
            case "wildCardEquals":
            {
                return attribute.wildCardEq((String) operationParameter);
            }
            case "wildCardIn":
            {
                return attribute.wildCardIn(new LinkedHashSet<>((Collection<? extends String>) operationParameter));
            }
            case "wildCardNotEq":
            case "wildCardNotEquals":
            {
                return attribute.wildCardNotEq((String) operationParameter);
            }
            case "subString":
            {
                return this.convertStringAttribute(
                        finderInstance,
                        attribute.substring(0, 0),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on StringAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertBooleanAttribute(
            RelatedFinder finderInstance,
            BooleanAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertBooleanAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertBooleanAttribute(
            RelatedFinder finderInstance,
            BooleanAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq((Boolean) operationParameter);
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq((Boolean) operationParameter);
            }
            case "in":
            {
                return attribute.in(BooleanSets.immutable.withAll((Collection<Boolean>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(BooleanSets.immutable.withAll((Collection<Boolean>) operationParameter));
            }
            default:
            {
                var message = "Unknown operation on StringAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertIntegerAttribute(
            RelatedFinder finderInstance,
            IntegerAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertIntegerAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertIntegerAttribute(
            RelatedFinder finderInstance,
            IntegerAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq((Integer) operationParameter);
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq((Integer) operationParameter);
            }
            case "in":
            {
                return attribute.in(IntSets.immutable.withAll((Collection<Integer>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(IntSets.immutable.withAll((Collection<Integer>) operationParameter));
            }
            case "greaterThan":
            {
                return attribute.greaterThan((Integer) operationParameter);
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals((Integer) operationParameter);
            }
            case "lessThan":
            {
                return attribute.lessThan((Integer) operationParameter);
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals((Integer) operationParameter);
            }
            case "abs":
            case "absoluteValue":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.absoluteValue(),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on IntegerAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertLongAttribute(
            RelatedFinder finderInstance,
            LongAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertLongAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertLongAttribute(
            RelatedFinder finderInstance,
            LongAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq((Long) operationParameter);
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq((Long) operationParameter);
            }
            case "in":
            {
                return attribute.in(LongSets.immutable.withAll((Collection<Long>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(LongSets.immutable.withAll((Collection<Long>) operationParameter));
            }
            case "greaterThan":
            {
                return attribute.greaterThan((Long) operationParameter);
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals((Long) operationParameter);
            }
            case "lessThan":
            {
                return attribute.lessThan((Long) operationParameter);
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals((Long) operationParameter);
            }
            case "absoluteValue":
            {
                return this.convertLongAttribute(
                        finderInstance,
                        attribute.absoluteValue(),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on LongAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertDoubleAttribute(
            RelatedFinder finderInstance,
            DoubleAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertDoubleAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertDoubleAttribute(
            RelatedFinder finderInstance,
            DoubleAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq((Double) operationParameter);
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq((Double) operationParameter);
            }
            case "in":
            {
                return attribute.in(DoubleSets.immutable.withAll((Collection<Double>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(DoubleSets.immutable.withAll((Collection<Double>) operationParameter));
            }
            case "greaterThan":
            {
                return attribute.greaterThan((Double) operationParameter);
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals((Double) operationParameter);
            }
            case "lessThan":
            {
                return attribute.lessThan((Double) operationParameter);
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals((Double) operationParameter);
            }
            case "absoluteValue":
            {
                return this.convertDoubleAttribute(
                        finderInstance,
                        attribute.absoluteValue(),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on DoubleAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertFloatAttribute(
            RelatedFinder finderInstance,
            FloatAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertFloatAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertFloatAttribute(
            RelatedFinder finderInstance,
            FloatAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq(((Double) operationParameter).floatValue());
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq(((Double) operationParameter).floatValue());
            }
            case "in":
            {
                return attribute.in(FloatSets.immutable.withAll(Iterate.collectFloat((Collection<Double>) operationParameter, Double::floatValue)));
            }
            case "notIn":
            {
                return attribute.notIn(FloatSets.immutable.withAll(Iterate.collectFloat((Collection<Double>) operationParameter, Double::floatValue)));
            }
            case "greaterThan":
            {
                return attribute.greaterThan(((Double) operationParameter).floatValue());
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals(((Double) operationParameter).floatValue());
            }
            case "lessThan":
            {
                return attribute.lessThan(((Double) operationParameter).floatValue());
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals(((Double) operationParameter).floatValue());
            }
            case "absoluteValue":
            {
                return this.convertFloatAttribute(
                        finderInstance,
                        attribute.absoluteValue(),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on FloatAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertDateAttribute(
            RelatedFinder finderInstance,
            DateAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertDateAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertDateAttribute(
            RelatedFinder finderInstance,
            DateAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq(GraphQLQueryToOperationConverter.getDate((String) operationParameter));
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq(GraphQLQueryToOperationConverter.getDate((String) operationParameter));
            }
            case "in":
            {
                return attribute.in(new LinkedHashSet<Timestamp>((Collection<? extends Timestamp>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(new LinkedHashSet<Timestamp>((Collection<? extends Timestamp>) operationParameter));
            }
            case "greaterThan":
            {
                return attribute.greaterThan(GraphQLQueryToOperationConverter.getDate((String) operationParameter));
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals(GraphQLQueryToOperationConverter.getDate((String) operationParameter));
            }
            case "lessThan":
            {
                return attribute.lessThan(GraphQLQueryToOperationConverter.getDate((String) operationParameter));
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals(GraphQLQueryToOperationConverter.getDate((String) operationParameter));
            }
            case "year":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.year(),
                        (Map<String, ?>) operationParameter);
            }
            case "month":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.month(),
                        (Map<String, ?>) operationParameter);
            }
            case "dayOfMonth":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.dayOfMonth(),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on IntegerAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertTimestampAttribute(
            RelatedFinder finderInstance,
            TimestampAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertTimestampAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertTimestampAttribute(
            RelatedFinder finderInstance,
            TimestampAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                return attribute.notEq(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "in":
            {
                return attribute.in(new LinkedHashSet<Timestamp>((Collection<? extends Timestamp>) operationParameter));
            }
            case "notIn":
            {
                return attribute.notIn(new LinkedHashSet<Timestamp>((Collection<? extends Timestamp>) operationParameter));
            }
            case "greaterThan":
            {
                return attribute.greaterThan(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "greaterThanEquals":
            {
                return attribute.greaterThanEquals(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "lessThan":
            {
                return attribute.lessThan(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "lessThanEquals":
            {
                return attribute.lessThanEquals(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "year":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.year(),
                        (Map<String, ?>) operationParameter);
            }
            case "month":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.month(),
                        (Map<String, ?>) operationParameter);
            }
            case "dayOfMonth":
            {
                return this.convertIntegerAttribute(
                        finderInstance,
                        attribute.dayOfMonth(),
                        (Map<String, ?>) operationParameter);
            }
            default:
            {
                var message = "Unknown operation on IntegerAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertAsOfAttribute(
            RelatedFinder finderInstance,
            AsOfAttribute attribute,
            Map<String, ?> graphQlOperation)
    {
        List<Operation> nestedOperations = graphQlOperation
                .entrySet()
                .stream()
                .map(entry -> this.convertAsOfAttribute(
                        finderInstance,
                        attribute,
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.toList());
        return nestedOperations.stream().reduce(finderInstance.all(), Operation::and);
    }

    private Operation convertAsOfAttribute(
            RelatedFinder finderInstance,
            AsOfAttribute attribute,
            String operationName,
            Object operationParameter)
    {
        switch (operationName)
        {
            case "eq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNull();
                }
                return attribute.eq(GraphQLQueryToOperationConverter.getTimestamp((String) operationParameter));
            }
            case "notEq":
            {
                if (operationParameter == null)
                {
                    return attribute.isNotNull();
                }
                var message = "notEq operation on AsOfAttribute only supported with null but found: "
                        + operationParameter;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
            case "equalsEdgePoint":
            {
                if (!operationParameter.equals(Maps.immutable.empty()))
                {
                    var message = "equalsEdgePoint takes an empty object as its argument but found: "
                            + operationParameter;
                    throw new LiftwizardGraphQLContextException(message, this.getContext());
                }
                return attribute.equalsEdgePoint();
            }
            default:
            {
                var message = "Unknown operation on AsOfAttribute: " + operationName;
                throw new LiftwizardGraphQLContextException(message, this.getContext());
            }
        }
    }

    private Operation convertRelationship(
            AbstractRelatedFinder relatedFinder,
            Map<String, ?> graphQlOperation)
    {
        return this.convert(relatedFinder, graphQlOperation);
    }

    private ImmutableList<String> getContext()
    {
        return this.context.toList().toReversed().toImmutable();
    }

    private static Timestamp getDate(String operationParameter)
    {
        LocalDate localDate = LocalDate.parse(operationParameter);
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    private static Timestamp getTimestamp(String operationParameter)
    {
        Instant instant = Instant.parse(operationParameter);
        return Timestamp.from(instant);
    }
}
