package io.liftwizard.graphql.scalar;

import java.util.function.Consumer;

import graphql.Scalars;
import graphql.schema.idl.RuntimeWiring.Builder;
import io.liftwizard.graphql.scalar.temporal.GraphQLLocalDateScalar;
import io.liftwizard.graphql.scalar.temporal.GraphQLTemporalScalar;

public class ScalarRuntimeWiringBuilder
        implements Consumer<Builder>
{
    @Override
    public void accept(Builder builder)
    {
        builder
                .scalar(new GraphQLTemporalScalar("Instant"))
                .scalar(new GraphQLTemporalScalar("TemporalInstant"))
                .scalar(new GraphQLTemporalScalar("TemporalRange"))
                .scalar(Scalars.GraphQLLong)
                .scalar(new GraphQLLocalDateScalar());
    }
}
