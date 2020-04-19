package com.liftwizard.dropwizard.bundle.uuid;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class UUIDBinder
        extends AbstractBinder
{
    private final Supplier<UUID> uuidSupplier;

    public UUIDBinder(Supplier<UUID> uuidSupplier)
    {
        this.uuidSupplier = Objects.requireNonNull(uuidSupplier);
    }

    @Override
    protected void configure()
    {
        this.bind(this.uuidSupplier).to(new TypeLiteral<Supplier<UUID>>() {});
    }
}
