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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.StringAttribute;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.impl.list.mutable.ListAdapter;

public class GraphQLQueryToOperationConverter
{
    private final MutableStack<String> context = Stacks.mutable.empty();

    public Operation convert(RelatedFinder finder, Map<?, ?> inputOperation)
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

    private Operation convert(RelatedFinder finder, String key, Object graphQlOperation)
    {
        if (key.equals("AND"))
        {
            return this.convertConjunction(finder, graphQlOperation, "AND", Operation::and);
        }

        if (key.equals("OR"))
        {
            return this.convertConjunction(finder, graphQlOperation, "OR", Operation::or);
        }

        return this.convertField(finder, key, graphQlOperation);
    }

    private Operation convertConjunction(
            RelatedFinder finder,
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

            RelatedFinder relationshipFinderByName = finder.getRelationshipFinderByName(key);
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
        Class aClass = attribute.valueType();
        if (String.class == aClass)
        {
            return this.convertStringAttribute(finderInstance, (StringAttribute) attribute, graphQlOperation);
        }
        throw new AssertionError(aClass);
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
            case "toLowerCase":
            {
                return this.convertStringAttribute(
                        finderInstance,
                        attribute.toLowerCase(),
                        (Map<String, ?>) operationParameter);
            }
            case "wildCardEq":
            {
                return attribute.wildCardEq((String) operationParameter);
            }
            case "wildCardIn":
            {
                return attribute.wildCardIn(new LinkedHashSet<>((Collection<? extends String>) operationParameter));
            }
            case "wildCardNotEq":
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

    private Operation convertRelationship(
            RelatedFinder relatedFinder,
            Map<String, ?> graphQlOperation)
    {
        return this.convert(relatedFinder, graphQlOperation);
    }

    private ImmutableList<String> getContext()
    {
        return this.context.toList().toReversed().toImmutable();
    }
}
