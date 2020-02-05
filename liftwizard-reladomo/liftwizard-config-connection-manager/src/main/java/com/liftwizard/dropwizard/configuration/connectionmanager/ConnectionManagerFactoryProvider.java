package com.liftwizard.dropwizard.configuration.connectionmanager;

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
