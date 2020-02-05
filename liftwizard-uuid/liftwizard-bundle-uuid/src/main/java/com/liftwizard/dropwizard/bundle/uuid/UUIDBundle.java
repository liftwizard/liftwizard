package com.liftwizard.dropwizard.bundle.uuid;

import java.util.UUID;
import java.util.function.Supplier;

import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class UUIDBundle
        implements ConfiguredBundle<Object>
{
    @Override
    public void initialize(Bootstrap<?> bootstrap)
    {
    }

    @Override
    public void run(Object configuration, Environment environment)
    {
        if (!(configuration instanceof UUIDSupplierFactoryProvider))
        {
            String message = String.format(
                    "Expected configuration to implement %s but found %s",
                    UUIDSupplierFactory.class.getCanonicalName(),
                    configuration.getClass().getCanonicalName());
            throw new IllegalStateException(message);
        }

        UUIDSupplierFactoryProvider uuidSupplierFactoryProvider = (UUIDSupplierFactoryProvider) configuration;
        UUIDSupplierFactory         uuidSupplierFactory         = uuidSupplierFactoryProvider.getUuidSupplierFactory();
        Supplier<UUID>              uuidSupplier                = uuidSupplierFactory.createUUIDSupplier();
        UUIDBinder                  uuidBinder                  = new UUIDBinder(uuidSupplier);
        environment.jersey().register(uuidBinder);
    }
}
