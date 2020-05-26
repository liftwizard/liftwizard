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

package com.liftwizard.dropwizard.bundle.uuid;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactory;
import com.liftwizard.dropwizard.configuration.uuid.UUIDSupplierFactoryProvider;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

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
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.runWithMdc(configuration, environment);
        }
    }

    private void runWithMdc(
            UUIDSupplierFactoryProvider configuration,
            @Nonnull Environment environment)
    {
        UUIDSupplierFactory uuidSupplierFactory = configuration.getUuidSupplierFactory();
        Supplier<UUID>      uuidSupplier        = uuidSupplierFactory.createUUIDSupplier();
        UUIDBinder          uuidBinder          = new UUIDBinder(uuidSupplier);
        environment.jersey().register(uuidBinder);
    }
}
