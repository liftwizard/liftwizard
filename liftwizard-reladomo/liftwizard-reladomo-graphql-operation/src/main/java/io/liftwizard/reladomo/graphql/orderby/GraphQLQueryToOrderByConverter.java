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
import java.util.Optional;

import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.finder.orderby.OrderBy;
import io.liftwizard.reladomo.graphql.operation.LiftwizardGraphQLContextException;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.stack.MutableStack;

public class GraphQLQueryToOrderByConverter
{
    private final MutableStack<String> context = Stacks.mutable.empty();

    private final MutableList<OrderBy> result = Lists.mutable.empty();

    public static Optional<OrderBy> convertOrderByList(RelatedFinder finder, List<Map<String, ?>> inputOrderBy)
    {
        return inputOrderBy
                .stream()
                .map(map -> GraphQLQueryToOrderByConverter.convertOrderBy(finder, map))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(OrderBy::and);
    }

    private static Optional<OrderBy> convertOrderBy(RelatedFinder finder, Map<String, ?> map)
    {
        GraphQLQueryToOrderByConverter converter = new GraphQLQueryToOrderByConverter();
        Map<String, ?> attribute = (Map<String, ?>) map.get("attribute");
        String         direction = (String) map.get("direction");

        if (attribute == null)
        {
            throw new LiftwizardGraphQLContextException("Missing attribute in orderBy", converter.context.toImmutableList());
        }

        converter.convertAttribute(finder, attribute, direction);
        return converter.getResult();
    }

    private void convertAttribute(RelatedFinder finder, Map<String, ?> attribute, String direction)
    {
        attribute.forEach((key, value) ->
        {
            this.context.push(key);

            try
            {
                this.convertOneAttribute(finder, direction, key, value);
            }
            finally
            {
                this.context.pop();
            }
        });
    }

    private void convertOneAttribute(RelatedFinder finder, String direction, String key, Object value)
    {
        if (value.equals(Maps.immutable.empty()))
        {
            Attribute attributeByName = finder.getAttributeByName(key);
            if (direction == null || direction.equals("ASCENDING"))
            {
                this.result.add(attributeByName.ascendingOrderBy());
            }
            else if (direction.equals("DESCENDING"))
            {
                this.result.add(attributeByName.descendingOrderBy());
            }
            else
            {
                throw new LiftwizardGraphQLContextException(
                        "Invalid direction: " + direction,
                        this.getContext());
            }
            return;
        }

        RelatedFinder                  relatedFinder = finder.getRelationshipFinderByName(key);
        GraphQLQueryToOrderByConverter converter     = new GraphQLQueryToOrderByConverter();
        converter.convertAttribute(relatedFinder, (Map<String, ?>) value, direction);
        Optional<OrderBy> nestedResult = converter.getResult().stream().reduce(OrderBy::and);
        nestedResult.ifPresent(this.result::add);
    }

    public Optional<OrderBy> getResult()
    {
        return this.result.stream().reduce(OrderBy::and);
    }

    private ImmutableList<String> getContext()
    {
        return this.context.toList().toReversed().toImmutable();
    }
}
