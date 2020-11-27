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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class MeteredDataFetcher<T>
        implements DataFetcher<T>
{
    @Nonnull
    private final DataFetcher<T>  dataFetcher;
    @Nonnull
    private final Optional<Timer> timerSync;
    @Nonnull
    private final Optional<Timer> timerAsync;
    @Nonnull
    private final Optional<Meter> meter;
    @Nonnull
    private final Optional<Meter> exceptionMeter;

    public MeteredDataFetcher(
            @Nonnull DataFetcher<T> dataFetcher,
            @Nonnull MetricRegistry metricRegistry)
    {
        this.dataFetcher = Objects.requireNonNull(dataFetcher);

        Class<? extends DataFetcher> dataFetcherClass = dataFetcher.getClass();

        Timed timedAnnotation = MeteredDataFetcher.getAnnotation(dataFetcherClass, Timed.class);
        this.timerSync = MeteredDataFetcher.getTimer(metricRegistry, dataFetcherClass, timedAnnotation, "sync");
        this.timerAsync = MeteredDataFetcher.getTimer(metricRegistry, dataFetcherClass, timedAnnotation, "async");

        Metered meteredAnnotation = MeteredDataFetcher.getAnnotation(dataFetcherClass, Metered.class);
        this.meter = MeteredDataFetcher.getMeter(metricRegistry, dataFetcherClass, meteredAnnotation);

        ExceptionMetered exceptionMeteredAnnotation = MeteredDataFetcher.getAnnotation(dataFetcherClass, ExceptionMetered.class);
        this.exceptionMeter = MeteredDataFetcher.getExceptionsMeter(metricRegistry, dataFetcherClass, exceptionMeteredAnnotation);
    }

    @Nullable
    private static <A extends Annotation> A getAnnotation(
            Class<? extends DataFetcher> dataFetcherClass,
            Class<A> annotationClass)
    {
        try
        {
            Method getMethod        = dataFetcherClass.getMethod("get", DataFetchingEnvironment.class);
            A      methodAnnotation = getMethod.getAnnotation(annotationClass);
            if (methodAnnotation != null)
            {
                return methodAnnotation;
            }
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }

        return dataFetcherClass.getAnnotation(annotationClass);
    }

    @Nonnull
    private static Optional<Timer> getTimer(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull Class<? extends DataFetcher> dataFetcherClass,
            @Nullable Timed annotation,
            String suffix)
    {
        if (annotation == null)
        {
            return Optional.empty();
        }

        String name = MeteredDataFetcher.chooseName(annotation.name(), annotation.absolute(), dataFetcherClass, suffix);

        return Optional.of(metricRegistry.timer(name));
    }

    @Nonnull
    private static Optional<Meter> getMeter(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull Class<? extends DataFetcher> dataFetcherClass,
            @Nullable Metered annotation)
    {
        if (annotation == null)
        {
            return Optional.empty();
        }

        String name = MeteredDataFetcher.chooseName(annotation.name(), annotation.absolute(), dataFetcherClass);

        return Optional.of(metricRegistry.meter(name));
    }

    @Nonnull
    public static Optional<Meter> getExceptionsMeter(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull Class<? extends DataFetcher> dataFetcherClass,
            @Nullable ExceptionMetered annotation)
    {
        if (annotation == null)
        {
            return Optional.empty();
        }

        String name = MeteredDataFetcher.chooseName(
                annotation.name(),
                annotation.absolute(),
                dataFetcherClass,
                ExceptionMetered.DEFAULT_NAME_SUFFIX);

        return Optional.of(metricRegistry.meter(name));
    }

    @Override
    public T get(DataFetchingEnvironment environment) throws Exception
    {
        Optional<Context> syncClock  = this.timerSync.map(Timer::time);
        Optional<Context> asyncClock = this.timerAsync.map(Timer::time);
        this.meter.ifPresent(Meter::mark);

        try
        {
            T result = this.dataFetcher.get(environment);
            if (result instanceof CompletionStage)
            {
                // If a fetcher never returns CompletionStage, we'll never record async timings
                CompletionStage<?> completionStage = (CompletionStage<?>) result;
                completionStage.whenComplete((ignored, throwable) ->
                {
                    asyncClock.ifPresent(Context::stop);
                    if (throwable != null)
                    {
                        this.exceptionMeter.ifPresent(Meter::mark);
                    }
                });
            }
            return result;
        }
        catch (Exception e)
        {
            this.exceptionMeter.ifPresent(Meter::mark);
            throw e;
        }
        finally
        {
            syncClock.ifPresent(Context::stop);
        }
    }

    private static String chooseName(
            String explicitName,
            boolean absolute,
            Class<?> aClass,
            String... suffixes)
    {
        String metricName = MeteredDataFetcher.getMetricName(explicitName, absolute, aClass);
        return MetricRegistry.name(metricName, suffixes);
    }

    private static String getMetricName(
            String explicitName,
            boolean absolute,
            Class<?> aClass)
    {
        if (explicitName == null || explicitName.isEmpty())
        {
            return MetricRegistry.name(aClass, "get");
        }

        if (absolute)
        {
            return explicitName;
        }

        return MetricRegistry.name(aClass, explicitName);
    }
}
