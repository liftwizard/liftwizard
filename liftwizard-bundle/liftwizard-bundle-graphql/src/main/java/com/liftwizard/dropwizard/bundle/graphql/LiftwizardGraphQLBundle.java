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
