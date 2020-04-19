package com.liftwizard.dropwizard.configuration.uuid;

import java.util.UUID;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.auto.service.AutoService;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = Id.NAME, property = "type")
@AutoService(Discoverable.class)
public interface UUIDSupplierFactory extends Discoverable
{
    Supplier<UUID> createUUIDSupplier();
}
