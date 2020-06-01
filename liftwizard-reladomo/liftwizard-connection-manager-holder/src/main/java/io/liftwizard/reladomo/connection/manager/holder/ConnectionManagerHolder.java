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

package io.liftwizard.reladomo.connection.manager.holder;

import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.factory.Sets;

public final class ConnectionManagerHolder
{
    private static final String KEY_NAME = "connectionManagerName";

    private static SourcelessConnectionManager instance;
    private static ImmutableMap<String, SourcelessConnectionManager> connectionManagersByName;

    private ConnectionManagerHolder()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    @Nonnull
    @SuppressWarnings("unused")
    public static SourcelessConnectionManager getInstance(Properties properties)
    {
        if (!Sets.immutable.with(KEY_NAME).equals(properties.keySet()))
        {
            throw new IllegalStateException("Expected a single property called name but found " + properties.keySet());
        }

        String name = (String) properties.get(KEY_NAME);
        Objects.requireNonNull(connectionManagersByName, "connectionManagersByName is null. Did you remember to run ConnectionManagerHolderBundle?");
        SourcelessConnectionManager sourcelessConnectionManager = connectionManagersByName.get(name);
        Objects.requireNonNull(
                sourcelessConnectionManager,
                () -> String.format(
                        "Could not find connection manager with name %s. Valid choices are %s",
                        name,
                        connectionManagersByName.keysView()));
        return sourcelessConnectionManager;
    }

    public static void setConnectionManagersByName(ImmutableMap<String, SourcelessConnectionManager> connectionManagersByName)
    {
        ConnectionManagerHolder.connectionManagersByName = Objects.requireNonNull(connectionManagersByName);
    }
}
