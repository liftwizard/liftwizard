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
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import graphql.execution.instrumentation.InstrumentationContext;

public class GlobalInstrumentationContext<T>
        implements InstrumentationContext<T>
{
    private final Timer timer;
    private final Meter exceptionsMeter;

    private Context context;

    public GlobalInstrumentationContext(Timer timer, Meter exceptionsMeter)
    {
        this.timer           = Objects.requireNonNull(timer);
        this.exceptionsMeter = Objects.requireNonNull(exceptionsMeter);
    }

    @Nonnull
    public static <T> GlobalInstrumentationContext<T> build(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull String suffix)
    {
        Objects.requireNonNull(suffix);
        Timer timer = metricRegistry.timer(MetricRegistry.name("liftwizard", "graphql", suffix));
        Meter exceptionsMeter = metricRegistry.meter(MetricRegistry.name("liftwizard", "graphql", suffix, "exceptions"));

        return new GlobalInstrumentationContext<>(timer, exceptionsMeter);
    }

    @Override
    public void onDispatched(CompletableFuture<T> result)
    {
        this.context = this.timer.time();
    }

    @Override
    public void onCompleted(T result, Throwable t)
    {
        if (t != null)
        {
            this.exceptionsMeter.mark();
        }
        this.context.stop();
    }
}
