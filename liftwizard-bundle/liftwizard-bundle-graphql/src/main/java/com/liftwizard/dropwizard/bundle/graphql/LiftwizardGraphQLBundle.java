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

package com.liftwizard.dropwizard.bundle.graphql;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.liftwizard.dropwizard.configuration.graphql.GraphQLFactoryProvider;
import com.smoketurner.dropwizard.graphql.GraphQLBundle;
import com.smoketurner.dropwizard.graphql.GraphQLFactory;
import graphql.schema.idl.RuntimeWiring;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

public class LiftwizardGraphQLBundle<T extends Configuration & GraphQLFactoryProvider>
        extends GraphQLBundle<T>
{
    @Nonnull
    private final Supplier<RuntimeWiring> runtimeWiringSupplier;

    public LiftwizardGraphQLBundle(@Nonnull Supplier<RuntimeWiring> runtimeWiringSupplier)
    {
        this.runtimeWiringSupplier = Objects.requireNonNull(runtimeWiringSupplier);
    }

    @Override
    public void initialize(@Nonnull Bootstrap<?> bootstrap)
    {
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.initializeWithMdc(bootstrap);
        }
    }

    private void initializeWithMdc(@Nonnull Bootstrap<?> bootstrap)
    {
        bootstrap.addBundle(new AssetsBundle(
                "/graphiql",
                "/graphiql",
                "index.htm",
                "graphiql"));

        bootstrap.addBundle(new AssetsBundle(
                "/assets",
                "/graphql-playground",
                "index.htm",
                "graphql-playground"));
    }

    @Nonnull
    @Override
    public GraphQLFactory getGraphQLFactory(@Nonnull T configuration)
    {
        // the RuntimeWiring must be configured prior to the run()
        // methods being called so the schema is connected properly.

        GraphQLFactory factory       = configuration.getGraphQLFactory();
        RuntimeWiring  runtimeWiring = this.runtimeWiringSupplier.get();
        factory.setRuntimeWiring(runtimeWiring);
        return factory;
    }
}
