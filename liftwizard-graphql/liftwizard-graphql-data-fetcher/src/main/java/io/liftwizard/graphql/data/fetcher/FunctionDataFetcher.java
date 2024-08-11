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
public class FunctionDataFetcher<Input, Output>
        implements DataFetcher<Output>
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
