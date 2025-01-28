package io.liftwizard.graphql.scalar;

import java.util.function.Consumer;

import graphql.scalars.java.JavaPrimitives;
import graphql.schema.idl.RuntimeWiring;
import io.liftwizard.graphql.scalar.temporal.GraphQLLocalDateScalar;
import io.liftwizard.graphql.scalar.temporal.GraphQLTemporalScalar;

public class ScalarRuntimeWiringBuilder
        implements Consumer<RuntimeWiring.Builder>
{
    @Override
    public void accept(RuntimeWiring.Builder builder)
    {
        builder
                .scalar(GraphQLTemporalScalar.INSTANT_INSTANCE)
                .scalar(GraphQLTemporalScalar.TEMPORAL_INSTANT_INSTANCE)
                .scalar(GraphQLTemporalScalar.TEMPORAL_RANGE_INSTANCE)
                .scalar(JavaPrimitives.GraphQLLong)
                .scalar(GraphQLLocalDateScalar.INSTANCE);
    }
}
