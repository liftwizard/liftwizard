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

import java.util.concurrent.ScheduledExecutorService;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

@JsonTypeName("noop")
@AutoService(ScheduledExecutorServiceFactory.class)
public class NoopScheduledExecutorServiceFactory
        implements ScheduledExecutorServiceFactory
{
    @Override
    @JsonIgnore
    public ScheduledExecutorService build(Environment environment)
    {
        return this.build(environment.lifecycle(), environment.metrics());
    }

    @Override
    @JsonIgnore
    public ScheduledExecutorService build(LifecycleEnvironment environment, MetricRegistry metricRegistry)
    {
        ScheduledExecutorService scheduledExecutorService = environment
                .scheduledExecutorService("noop", true)
                .build();
        NoopScheduledExecutorService noopScheduledExecutorService = new NoopScheduledExecutorService(
                scheduledExecutorService);
        return new InstrumentedScheduledExecutorService(noopScheduledExecutorService, metricRegistry, "noop");
    }
}
