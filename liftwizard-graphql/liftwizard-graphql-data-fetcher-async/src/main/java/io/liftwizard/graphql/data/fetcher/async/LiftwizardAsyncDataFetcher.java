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

package io.liftwizard.graphql.data.fetcher.async;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import graphql.schema.AsyncDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.MDC;

/**
 * An enhanced alternative to {@link AsyncDataFetcher}.
 *
 * <p>
 * Both have the ability to wrap a synchronous {@link DataFetcher} together with an {@link Executor}, and return CompleteableFutures that execute on the Executor. LiftwizardAsyncDataFetcher also copies slf4j's {@link MDC} to the background tasks, and restores the MDC when each task completes.
 *
 * @see <a href="https://liftwizard.io/docs/graphql/data-fetcher-async">https://liftwizard.io/docs/graphql/data-fetcher-async</a>
 */
public class LiftwizardAsyncDataFetcher<T>
        implements DataFetcher<CompletableFuture<T>>
{
    private final DataFetcher<T> wrappedDataFetcher;
    private final Executor executor;

    public LiftwizardAsyncDataFetcher(DataFetcher<T> wrappedDataFetcher, Executor executor)
    {
        this.wrappedDataFetcher = Objects.requireNonNull(wrappedDataFetcher);
        this.executor = Objects.requireNonNull(executor);
    }

    public static <T> LiftwizardAsyncDataFetcher<T> async(DataFetcher<T> wrappedDataFetcher, Executor executor)
    {
        return new LiftwizardAsyncDataFetcher<>(wrappedDataFetcher, executor);
    }

    public DataFetcher<T> getWrappedDataFetcher()
    {
        return this.wrappedDataFetcher;
    }

    public Executor getExecutor()
    {
        return this.executor;
    }

    @Override
    public CompletableFuture<T> get(DataFetchingEnvironment environment)
    {
        return CompletableFuture.supplyAsync(
                new AsyncDataSupplier<>(this.wrappedDataFetcher, environment),
                this.executor);
    }
}
