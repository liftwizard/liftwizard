package com.liftwizard.dropwizard.configuration.uuid.system;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;

@JsonTypeName("system")
@AutoService(UUIDSupplierFactory.class)
public class SystemUUIDSupplierFactory implements UUIDSupplierFactory
{
    @Nonnull
    @Override
    public Supplier<UUID> createUUIDSupplier()
    {
        return new SystemUUIDSupplier();
    }
}
