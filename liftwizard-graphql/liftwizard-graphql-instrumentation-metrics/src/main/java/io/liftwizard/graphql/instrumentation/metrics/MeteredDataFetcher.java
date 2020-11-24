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

package io.liftwizard.graphql.instrumentation.metrics;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nonnull;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class MeteredDataFetcher<T>
        implements DataFetcher<T>
{
    @Nonnull
    private final DataFetcher<T> dataFetcher;
    @Nonnull
    private final Timer          timerSync;
    @Nonnull
    private final Timer          timerAsync;
    @Nonnull
    private final Meter          exceptionMeter;

    public MeteredDataFetcher(
            @Nonnull DataFetcher<T> dataFetcher,
            @Nonnull MetricRegistry metricRegistry)
    {
        this.dataFetcher = Objects.requireNonNull(dataFetcher);

        this.timerSync      = metricRegistry.timer(MetricRegistry.name(dataFetcher.getClass(), "sync"));
        this.timerAsync     = metricRegistry.timer(MetricRegistry.name(dataFetcher.getClass(), "async"));
        this.exceptionMeter = metricRegistry.meter(MetricRegistry.name(dataFetcher.getClass(), "exceptions"));
    }

    @Override
    public T get(DataFetchingEnvironment environment) throws Exception
    {
        try (var sync = this.timerSync.time())
        {
            // Deliberately not using try-with-resources for the second timer
            // If a fetcher never returns CompletionStage, we'll never record async timings
            Context asyncTime = this.timerAsync.time();
            T       result    = this.dataFetcher.get(environment);
            if (result instanceof CompletionStage)
            {
                ((CompletionStage<?>) result).thenRun(asyncTime::close);
            }
            return result;
        }
        catch (Exception e)
        {
            this.exceptionMeter.mark();
            throw e;
        }
    }
}
