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

package io.liftwizard.dropwizard.configuration.datasource;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.liftwizard.dropwizard.db.NamedDataSourceFactory;

public interface NamedDataSourceProvider
{
    List<NamedDataSourceFactory> getNamedDataSourceFactories();

    void initializeDataSources(
            @Nonnull MetricRegistry metricRegistry,
            @Nonnull LifecycleEnvironment lifecycle);

    ManagedDataSource getDataSourceByName(@Nonnull String name);

    @Nonnull
    Map<String, ManagedDataSource> getDataSourcesByName();
}
