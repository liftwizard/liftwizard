/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.dropwizard.configuration.executor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public abstract class AbstractDelegatingScheduledExecutorService
        implements ScheduledExecutorService
{
    protected final ScheduledExecutorService delegate;

    protected AbstractDelegatingScheduledExecutorService(ScheduledExecutorService delegate)
    {
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected abstract Runnable wrapTask(Runnable command);

    protected abstract <V> Callable<V> wrapTask(Callable<V> callable);

    protected <T> Collection<? extends Callable<T>> wrapTasks(Collection<? extends Callable<T>> tasks)
    {
        return tasks.stream().map(this::wrapTask).collect(Collectors.toList());
    }

    @Override
    public ScheduledFuture<?> schedule(@Nonnull Runnable command, long delay, @Nonnull TimeUnit unit)
    {
        return this.delegate.schedule(this.wrapTask(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit)
    {
        return this.delegate.schedule(this.wrapTask(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(
            @Nonnull Runnable command,
            long initialDelay,
            long period,
            @Nonnull TimeUnit unit)
    {
        return this.delegate.scheduleAtFixedRate(this.wrapTask(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(
            @Nonnull Runnable command,
            long initialDelay,
            long delay,
            @Nonnull TimeUnit unit)
    {
        return this.delegate.scheduleWithFixedDelay(this.wrapTask(command), initialDelay, delay, unit);
    }

    @Override
    public void shutdown()
    {
        this.delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        return this.delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown()
    {
        return this.delegate.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return this.delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit)
            throws InterruptedException
    {
        return this.delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(@Nonnull Callable<T> task)
    {
        return this.delegate.submit(this.wrapTask(task));
    }

    @Override
    public <T> Future<T> submit(@Nonnull Runnable task, T result)
    {
        return this.delegate.submit(this.wrapTask(task), result);
    }

    @Override
    public Future<?> submit(@Nonnull Runnable task)
    {
        return this.delegate.submit(this.wrapTask(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks)
            throws InterruptedException
    {
        return this.delegate.invokeAll(this.wrapTasks(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            @Nonnull Collection<? extends Callable<T>> tasks,
            long timeout,
            @Nonnull TimeUnit unit)
            throws InterruptedException
    {
        return this.delegate.invokeAll(this.wrapTasks(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException
    {
        return this.delegate.invokeAny(this.wrapTasks(tasks));
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        return this.delegate.invokeAny(this.wrapTasks(tasks), timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command)
    {
        this.delegate.execute(this.wrapTask(command));
    }
}
