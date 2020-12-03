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
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

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
import io.liftwizard.graphql.data.fetcher.async.LiftwizardAsyncDataFetcher;

public class InstrumentedDataFetcher<T>
        implements DataFetcher<T>
{
    @Nonnull
    private final MetricRegistry metricRegistry;
    @Nonnull
    private final Clock          clock;
    @Nonnull
    private final DataFetcher<T> dataFetcher;
    @Nonnull
    private final String         typeName;
    @Nonnull
    private final String         fieldName;
    @Nonnull
    private final String         path;

    @Nullable
    private final Timed            timedAnnotation;
    @Nullable
    private final Metered          meteredAnnotation;
    @Nullable
    private final ExceptionMetered exceptionMeteredAnnotation;

    @Nonnull
    private final Optional<Timer> timerFetcherSync;
    @Nonnull
    private final Optional<Timer> timerFieldSync;
    @Nonnull
    private final Optional<Timer> timerPathSync;
    @Nonnull
    private final Optional<Meter> meterFetcher;
    @Nonnull
    private final Optional<Meter> meterField;
    @Nonnull
    private final Optional<Meter> meterPath;
    @Nonnull
    private final Optional<Meter> exceptionMeterFetcher;
    @Nonnull
    private final Optional<Meter> exceptionMeterField;
    @Nonnull
    private final Optional<Meter> exceptionMeterPath;

    public InstrumentedDataFetcher(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull Clock clock,
            @Nonnull DataFetcher<T> dataFetcher,
            @Nonnull String fieldName,
            @Nonnull String typeName,
            @Nonnull String path)
    {
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
        this.clock          = Objects.requireNonNull(clock);
        this.dataFetcher    = Objects.requireNonNull(dataFetcher);
        this.typeName       = Objects.requireNonNull(typeName);
        this.fieldName      = Objects.requireNonNull(fieldName);
        this.path           = Objects.requireNonNull(path);

        this.timedAnnotation            = this.getAnnotation(Timed.class);
        this.meteredAnnotation          = this.getAnnotation(Metered.class);
        this.exceptionMeteredAnnotation = this.getAnnotation(ExceptionMetered.class);

        this.timerFetcherSync = this.getTimer("sync");
        this.timerFieldSync   = this.getFieldTimer("sync");
        this.timerPathSync    = this.getPathTimer("sync");

        this.meterFetcher = this.getMeter();
        this.meterField   = this.getFieldMeter();
        this.meterPath    = this.getPathMeter();

        this.exceptionMeterFetcher = this.getExceptionsMeter();
        this.exceptionMeterField   = this.getFieldExceptionsMeter();
        this.exceptionMeterPath    = this.getPathExceptionsMeter();
    }

    private DataFetcher<?> getAnnotatedDataFetcher()
    {
        return this.dataFetcher instanceof LiftwizardAsyncDataFetcher
                ? ((LiftwizardAsyncDataFetcher<?>) this.dataFetcher).getWrappedDataFetcher()
                : this.dataFetcher;
    }

    @Nullable
    private <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
        DataFetcher<?> annotatedDataFetcher = this.getAnnotatedDataFetcher();
        try
        {
            Method getMethod        = annotatedDataFetcher.getClass().getMethod("get", DataFetchingEnvironment.class);
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

        return annotatedDataFetcher.getClass().getAnnotation(annotationClass);
    }

    @Nonnull
    private Optional<Timer> getTimer(String suffix)
    {
        if (this.timedAnnotation == null)
        {
            return Optional.empty();
        }

        String name = InstrumentedDataFetcher.chooseName(
                this.timedAnnotation.name(),
                this.timedAnnotation.absolute(),
                this.getAnnotatedDataFetcher().getClass(),
                suffix);

        return Optional.of(this.metricRegistry.timer(name));
    }

    @Nonnull
    private Optional<Timer> getFieldTimer(String suffix)
    {
        if (this.timedAnnotation == null)
        {
            return Optional.empty();
        }

        String name  = MetricRegistry.name("liftwizard", "graphql", "field", this.typeName, this.fieldName, suffix);
        Timer  timer = this.metricRegistry.timer(name);
        return Optional.of(timer);
    }

    @Nonnull
    private Optional<Timer> getPathTimer(String suffix)
    {
        if (this.timedAnnotation == null)
        {
            return Optional.empty();
        }

        String name  = MetricRegistry.name("liftwizard", "graphql", "path", this.path, suffix);
        Timer  timer = this.metricRegistry.timer(name);
        return Optional.of(timer);
    }

    @Nonnull
    private Optional<Meter> getMeter()
    {
        if (this.meteredAnnotation == null)
        {
            return Optional.empty();
        }

        String name = InstrumentedDataFetcher.chooseName(
                this.meteredAnnotation.name(),
                this.meteredAnnotation.absolute(),
                this.getAnnotatedDataFetcher().getClass());

        return Optional.of(this.metricRegistry.meter(name));
    }

    @Nonnull
    private Optional<Meter> getFieldMeter()
    {
        if (this.meteredAnnotation == null)
        {
            return Optional.empty();
        }

        String name  = MetricRegistry.name("liftwizard", "graphql", "field", this.typeName, this.fieldName);
        Meter  meter = this.metricRegistry.meter(name);
        return Optional.of(meter);
    }

    @Nonnull
    private Optional<Meter> getPathMeter()
    {
        if (this.meteredAnnotation == null)
        {
            return Optional.empty();
        }

        String name  = MetricRegistry.name("liftwizard", "graphql", "path", this.path);
        Meter  meter = this.metricRegistry.meter(name);
        return Optional.of(meter);
    }

    @Nonnull
    private Optional<Meter> getExceptionsMeter()
    {
        if (this.exceptionMeteredAnnotation == null)
        {
            return Optional.empty();
        }

        String name = InstrumentedDataFetcher.chooseName(
                this.exceptionMeteredAnnotation.name(),
                this.exceptionMeteredAnnotation.absolute(),
                this.getAnnotatedDataFetcher().getClass(),
                ExceptionMetered.DEFAULT_NAME_SUFFIX);

        return Optional.of(this.metricRegistry.meter(name));
    }

    @Nonnull
    private Optional<Meter> getFieldExceptionsMeter()
    {
        if (this.exceptionMeteredAnnotation == null)
        {
            return Optional.empty();
        }

        String name = MetricRegistry.name(
                "liftwizard",
                "graphql",
                "field",
                this.typeName,
                this.fieldName,
                ExceptionMetered.DEFAULT_NAME_SUFFIX);
        Meter meter = this.metricRegistry.meter(name);
        return Optional.of(meter);
    }

    @Nonnull
    private Optional<Meter> getPathExceptionsMeter()
    {
        if (this.exceptionMeteredAnnotation == null)
        {
            return Optional.empty();
        }

        String name = MetricRegistry.name(
                "liftwizard",
                "graphql",
                "path",
                this.path,
                ExceptionMetered.DEFAULT_NAME_SUFFIX);
        Meter meter = this.metricRegistry.meter(name);
        return Optional.of(meter);
    }

    @Override
    public T get(DataFetchingEnvironment environment) throws Exception
    {
        Instant           startTime        = Instant.now();
        Optional<Context> fetcherSyncClock = this.timerFetcherSync.map(Timer::time);
        Optional<Context> fieldSyncClock   = this.timerFieldSync.map(Timer::time);
        Optional<Context> pathSyncClock    = this.timerPathSync.map(Timer::time);

        try
        {
            T result = this.dataFetcher.get(environment);
            if (result instanceof CompletionStage)
            {
                // If a fetcher never returns CompletionStage, we'll never record async timings
                CompletionStage<?> completionStage = (CompletionStage<?>) result;
                completionStage.whenComplete((success, throwable) ->
                {
                    Optional<Timer> timerFetcherAsync = this.getTimer("async");
                    Optional<Timer> timerFieldAsync   = this.getFieldTimer("async");
                    Optional<Timer> timerPathAsync    = this.getPathTimer("async");

                    if (this.timedAnnotation != null)
                    {
                        Instant  stopTime = this.clock.instant();
                        Duration duration = Duration.between(startTime, stopTime);
                        timerFetcherAsync.orElseThrow().update(duration.toNanos(), TimeUnit.NANOSECONDS);
                        timerFieldAsync.orElseThrow().update(duration.toNanos(), TimeUnit.NANOSECONDS);
                        timerPathAsync.orElseThrow().update(duration.toNanos(), TimeUnit.NANOSECONDS);
                    }

                    if (throwable != null)
                    {
                        this.exceptionMeterFetcher.ifPresent(Meter::mark);
                        this.exceptionMeterField.ifPresent(Meter::mark);
                        this.exceptionMeterPath.ifPresent(Meter::mark);
                    }

                    int size = success instanceof Collection ? ((Collection<?>) success).size() : 1;
                    this.meterFetcher.ifPresent(meter -> meter.mark(size));
                    this.meterField.ifPresent(meter -> meter.mark(size));
                    this.meterPath.ifPresent(meter -> meter.mark(size));
                });
            }
            else
            {
                int size = result instanceof Collection ? ((Collection<?>) result).size() : 1;
                this.meterFetcher.ifPresent(meter -> meter.mark(size));
                this.meterField.ifPresent(meter -> meter.mark(size));
                this.meterPath.ifPresent(meter -> meter.mark(size));
            }

            return result;
        }
        catch (Exception e)
        {
            this.exceptionMeterFetcher.ifPresent(Meter::mark);
            this.exceptionMeterField.ifPresent(Meter::mark);
            this.exceptionMeterPath.ifPresent(Meter::mark);
            throw e;
        }
        finally
        {
            fetcherSyncClock.ifPresent(Context::stop);
            fieldSyncClock.ifPresent(Context::stop);
            pathSyncClock.ifPresent(Context::stop);
        }
    }

    private static String chooseName(
            String explicitName,
            boolean absolute,
            Class<?> aClass,
            String... suffixes)
    {
        String metricName = InstrumentedDataFetcher.getMetricName(explicitName, absolute, aClass);
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
