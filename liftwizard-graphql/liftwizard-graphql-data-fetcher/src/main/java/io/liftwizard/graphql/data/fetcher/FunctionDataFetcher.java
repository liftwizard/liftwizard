package io.liftwizard.graphql.data.fetcher;

import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * A DataFetcher backed by a Function.
 * Similar to {@link graphql.schema.PropertyDataFetcher} but without any reflection.
 *
 * @see graphql.schema.PropertyDataFetcher
 */
public class FunctionDataFetcher<Input, Output> implements DataFetcher<Output>
{
    @Nonnull
    private final Function<Input, Output> function;

    public FunctionDataFetcher(@Nonnull Function<Input, Output> function)
    {
        this.function = Objects.requireNonNull(function);
    }

    @Nullable
    @Override
    public Output get(@Nonnull DataFetchingEnvironment environment)
    {
        Input source = environment.getSource();
        if (source == null)
        {
            return null;
        }

        return this.function.apply(source);
    }
}
