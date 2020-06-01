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

package io.liftwizard.dropwizard.configuration.connectionmanager;

import java.util.List;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import io.dropwizard.db.ManagedDataSource;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MapIterable;

public interface ConnectionManagerFactoryProvider
{
    List<ConnectionManagerFactory> getConnectionManagerFactories();

    void initializeConnectionManagers(@Nonnull MapIterable<String, ManagedDataSource> dataSourcesByName);

    SourcelessConnectionManager getConnectionManagerByName(@Nonnull String name);

    ImmutableMap<String, SourcelessConnectionManager> getConnectionManagersByName();
}
