/*
 * Copyright 2022 Craig Motlin
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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopScheduledExecutorService
        extends AbstractDelegatingScheduledExecutorService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopScheduledExecutorService.class);

    public NoopScheduledExecutorService(ScheduledExecutorService delegate)
    {
        super(delegate);
    }

    @Override
    protected Runnable wrapTask(Runnable command)
    {
        return () -> LOGGER.debug("Skip scheduled task: {}", command);
    }

    @Override
    protected <V> Callable<V> wrapTask(Callable<V> callable)
    {
        return () ->
        {
            LOGGER.debug("Skip scheduled task: {}", callable);
            return null;
        };
    }
}
