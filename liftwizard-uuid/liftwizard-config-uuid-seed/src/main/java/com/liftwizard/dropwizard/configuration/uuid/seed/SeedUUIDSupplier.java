package com.liftwizard.dropwizard.configuration.uuid.seed;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class SeedUUIDSupplier implements Supplier<UUID>
{
    private final String seed;
    private       int    counter;

    public SeedUUIDSupplier(@Nonnull String seed)
    {
        this.seed = Objects.requireNonNull(seed);
    }

    @Nonnull
    @Override
    public UUID get()
    {
        this.counter++;
        String name  = this.seed + this.counter;
        byte[] bytes = name.getBytes();
        return UUID.nameUUIDFromBytes(bytes);
    }
}
