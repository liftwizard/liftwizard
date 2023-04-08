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

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.auto.service.AutoService;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

@JsonTypeInfo(use = Id.NAME, property = "type", defaultImpl = DefaultScheduledExecutorServiceFactory.class)
@AutoService(Discoverable.class)
public interface ScheduledExecutorServiceFactory
        extends Discoverable
{
    @JsonIgnore
    ScheduledExecutorService build(Environment environment);

    @JsonIgnore
    ScheduledExecutorService build(LifecycleEnvironment environment, MetricRegistry metricRegistry);
}
