/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.graphql.scalar.temporal;

import graphql.Internal;
import graphql.schema.GraphQLScalarType;

/**
 * Derived from <a href="https://github.com/graphql-java/graphql-java-extended-scalars">graphql-java-extended-scalars</a> but for Instant instead of OffsetDateTime.
 */
@Internal
public final class GraphQLTemporalScalar
{
    public static final GraphQLScalarType INSTANT_INSTANCE          = getGraphQLScalarType("Instant");
    public static final GraphQLScalarType TEMPORAL_INSTANT_INSTANCE = getGraphQLScalarType("TemporalInstant");
    public static final GraphQLScalarType TEMPORAL_RANGE_INSTANCE   = getGraphQLScalarType("TemporalRange");

    private GraphQLTemporalScalar()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    private static GraphQLScalarType getGraphQLScalarType(String name)
    {
        return GraphQLScalarType.newScalar()
                .name(name)
                .description("A slightly refined version of RFC-3339 compliant " + name + " Scalar")
                .coercing(InstantCoercing.INSTANCE)
                .build();
    }
}
