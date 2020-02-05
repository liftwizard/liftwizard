package com.liftwizard.dropwizard.configuration.uuid.seed;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;

@JsonTypeName("seed")
@AutoService(UUIDSupplierFactory.class)
public class SeedUUIDSupplierFactory implements UUIDSupplierFactory
{
    private @Valid @NotNull String seed = "example seed";

    @Nonnull
    @Override
    public Supplier<UUID> createUUIDSupplier()
    {
        return new SeedUUIDSupplier(this.seed);
    }

    @JsonProperty
    public String getSeed()
    {
        return this.seed;
    }

    @JsonProperty
    public void setSeed(String seed)
    {
        this.seed = seed;
    }
}
