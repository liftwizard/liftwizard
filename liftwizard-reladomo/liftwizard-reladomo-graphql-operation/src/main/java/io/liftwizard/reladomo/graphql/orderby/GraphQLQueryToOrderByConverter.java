/*
 * Copyright 2023 Craig Motlin
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

package io.liftwizard.reladomo.graphql.orderby;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.finder.orderby.OrderBy;
import io.liftwizard.reladomo.graphql.operation.LiftwizardGraphQLContextException;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.stack.MutableStack;

public class GraphQLQueryToOrderByConverter
{
    private final MutableStack<String> context = Stacks.mutable.empty();

    public Optional<OrderBy> convert(RelatedFinder finder, Map<String, ?> inputAttributes, String inputDirection)
    {
        Objects.requireNonNull(inputDirection);
        List<OrderBy> nestedOrderBys = inputAttributes
                .entrySet()
                .stream()
                .map(each -> this.convert(
                        finder,
                        each.getKey(),
                        each.getValue(),
                        inputDirection))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return nestedOrderBys.stream().reduce(OrderBy::and);
    }

    private Optional<OrderBy> convert(RelatedFinder finder, String key, Object graphQlOrderBy, String inputDirection)
    {
        Objects.requireNonNull(inputDirection);
        this.context.push(key);

        try
        {
            Attribute attributeByName = finder.getAttributeByName(key);
            if (attributeByName != null)
            {
                return this.convertAttribute(finder, attributeByName, (Map<String, ?>) graphQlOrderBy, inputDirection);
            }

            RelatedFinder relationshipFinderByName = finder.getRelationshipFinderByName(key);
            if (relationshipFinderByName != null)
            {
                return this.convertRelationship(
                        relationshipFinderByName,
                        (Map<String, ?>) graphQlOrderBy,
                        inputDirection);
            }

            throw new LiftwizardGraphQLContextException("Could not find field " + key, this.getContext());
        }
        finally
        {
            this.context.pop();
        }
    }

    private Optional<OrderBy> convertAttribute(
            RelatedFinder finderInstance,
            Attribute attribute,
            Map<String, ?> graphQlOrderBy,
            String inputDirection)
    {
        if (graphQlOrderBy.isEmpty())
        {
            return switch (inputDirection)
            {
                case "ASCENDING" -> Optional.ofNullable(attribute.ascendingOrderBy());
                case "DESCENDING" -> Optional.ofNullable(attribute.descendingOrderBy());
                default -> throw new LiftwizardGraphQLContextException(
                        "Invalid direction: " + inputDirection,
                        this.getContext());
            };
        }

        return graphQlOrderBy
                .entrySet()
                .stream()
                .map(each -> this.convert(
                        (AbstractRelatedFinder) finderInstance,
                        each.getKey(),
                        each.getValue(),
                        inputDirection))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(OrderBy::and);
    }

    private Optional<OrderBy> convertRelationship(
            RelatedFinder relatedFinder,
            Map<String, ?> graphQlOrderBy,
            String inputDirection)
    {
        return this.convert(relatedFinder, graphQlOrderBy, inputDirection);
    }

    private ImmutableList<String> getContext()
    {
        return this.context.toList().toReversed().toImmutable();
    }
}
