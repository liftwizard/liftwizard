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

package com.liftwizard.dropwizard.bundle.reladomo.connection.manager.holder;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.connectionmanager.ConnectionManagerFactoryProvider;
import com.liftwizard.reladomo.connection.manager.holder.ConnectionManagerHolder;
import io.dropwizard.setup.Environment;
import org.eclipse.collections.api.map.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class ConnectionManagerHolderBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManagerHolderBundle.class);

    @Override
    public int getPriority()
    {
        return -4;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        ConnectionManagerFactoryProvider connectionManagerFactoryProvider = this.safeCastConfiguration(
                ConnectionManagerFactoryProvider.class,
                configuration);

        LOGGER.info("Running {}.", ConnectionManagerHolderBundle.class.getSimpleName());

        ImmutableMap<String, SourcelessConnectionManager> connectionManagersByName =
                connectionManagerFactoryProvider.getConnectionManagersByName();

        ConnectionManagerHolder.setConnectionManagersByName(connectionManagersByName);

        LOGGER.info("Completing {}.", ConnectionManagerHolderBundle.class.getSimpleName());
    }
}
