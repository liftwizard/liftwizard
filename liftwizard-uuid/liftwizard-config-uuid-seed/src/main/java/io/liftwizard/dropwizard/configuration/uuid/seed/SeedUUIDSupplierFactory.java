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

package io.liftwizard.dropwizard.configuration.uuid.seed;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;

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
