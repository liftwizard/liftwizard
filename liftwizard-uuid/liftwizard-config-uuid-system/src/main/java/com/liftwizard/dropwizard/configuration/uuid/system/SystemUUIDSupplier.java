package com.liftwizard.dropwizard.configuration.uuid.system;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class SystemUUIDSupplier implements Supplier<UUID>
{
    @Nonnull
    @Override
    public UUID get()
    {
        return UUID.randomUUID();
    }
}
