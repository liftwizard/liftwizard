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
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import graphql.execution.instrumentation.InstrumentationContext;

public class FieldInstrumentationContext
        implements InstrumentationContext<Object>
{
    private final Meter allFieldsExceptionsMeter;

    private final Context allFieldsSyncClock;
    private final Context allFieldsAsyncClock;

    public FieldInstrumentationContext(
            @Nonnull Timer allFieldsSyncTimer,
            @Nonnull Timer allFieldsAsyncTimer,
            @Nonnull Meter allFieldsExceptionsMeter)
    {
        this.allFieldsExceptionsMeter = Objects.requireNonNull(allFieldsExceptionsMeter);

        this.allFieldsSyncClock = allFieldsSyncTimer.time();
        this.allFieldsAsyncClock = allFieldsAsyncTimer.time();
    }

    @Override
    public void onDispatched(CompletableFuture<Object> result)
    {
        this.allFieldsSyncClock.stop();
    }

    @Override
    public void onCompleted(Object result, Throwable throwable)
    {
        if (throwable != null)
        {
            this.allFieldsExceptionsMeter.mark();
        }

        this.allFieldsAsyncClock.stop();
    }
}
