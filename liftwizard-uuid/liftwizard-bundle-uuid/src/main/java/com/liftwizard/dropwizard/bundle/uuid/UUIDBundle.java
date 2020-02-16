package com.liftwizard.dropwizard.bundle.uuid;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class UUIDBundle
        implements ConfiguredBundle<UUIDSupplierFactoryProvider>
{
    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(UUIDSupplierFactoryProvider configuration, @Nonnull Environment environment)
    {
        UUIDSupplierFactory uuidSupplierFactory = configuration.getUuidSupplierFactory();
        Supplier<UUID>      uuidSupplier        = uuidSupplierFactory.createUUIDSupplier();
        UUIDBinder          uuidBinder          = new UUIDBinder(uuidSupplier);
        environment.jersey().register(uuidBinder);
    }
}
