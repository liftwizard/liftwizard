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

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import graphql.execution.instrumentation.InstrumentationContext;

public class GlobalInstrumentationContext<T>
        implements InstrumentationContext<T>
{
    private final Context clock;
    private final Meter   exceptionsMeter;

    public GlobalInstrumentationContext(Timer timer, Meter exceptionsMeter)
    {
        Objects.requireNonNull(timer);
        this.clock           = timer.time();
        this.exceptionsMeter = Objects.requireNonNull(exceptionsMeter);
    }

    @Override
    public void onDispatched(CompletableFuture<T> result)
    {
    }

    @Override
    public void onCompleted(T result, Throwable t)
    {
        if (t != null)
        {
            this.exceptionsMeter.mark();
        }
        Objects.requireNonNull(this.clock);
        this.clock.stop();
    }
}
