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

package io.liftwizard.dropwizard.bundle.graphql;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.codahale.metrics.MetricRegistry;
import com.smoketurner.dropwizard.graphql.GraphQLBundle;
import com.smoketurner.dropwizard.graphql.GraphQLFactory;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.liftwizard.dropwizard.configuration.graphql.GraphQLFactoryProvider;
import io.liftwizard.graphql.instrumentation.logging.LiftwizardGraphQLLoggingInstrumentation;
import io.liftwizard.graphql.instrumentation.metrics.LiftwizardGraphQLMetricsInstrumentation;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

/**
 * The bundle registers the GraphIQL UI at /graphiql and the GraphQL Playground UI at /graphql-playground, by delegating to AssetsBundle. This overrides the behavior of the smoketurner bundle {@link GraphQLBundle}, which registers just one UI (graphiql in older versions, and graphql-playground in newer versions) and registers the UI at the root.
 *
 * <p>
 * The bundle also registers two instrumentations for logging and metrics.
 *
 * @see <a href="https://liftwizard.io/docs/graphql/bundle#liftwizardgraphqlbundle">https://liftwizard.io/docs/graphql/bundle#liftwizardgraphqlbundle</a>
 */
public class LiftwizardGraphQLBundle<T extends Configuration & GraphQLFactoryProvider>
        extends GraphQLBundle<T>
{
    @Nonnull
    private final Consumer<Builder> runtimeWiringBuilder;

    private MetricRegistry metricRegistry;

    public LiftwizardGraphQLBundle(@Nonnull Consumer<Builder> runtimeWiringBuilder)
    {
        this.runtimeWiringBuilder = Objects.requireNonNull(runtimeWiringBuilder);
    }

    @Override
    public void initialize(@Nonnull Bootstrap<?> bootstrap)
    {
        try (MDCCloseable mdc = MDC.putCloseable("liftwizard.bundle", this.getClass().getSimpleName()))
        {
            this.initializeWithMdc(bootstrap);
        }

        this.metricRegistry = bootstrap.getMetricRegistry();
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
        GraphQLFactory factory = configuration.getGraphQLFactory();

        // TODO: Move the Clock to Configuration
        Clock clock = Clock.systemUTC();

        var metricsInstrumentation = new LiftwizardGraphQLMetricsInstrumentation(this.metricRegistry, clock);
        var loggingInstrumentation = new LiftwizardGraphQLLoggingInstrumentation();

        List<Instrumentation> instrumentations = List.of(metricsInstrumentation, loggingInstrumentation);
        factory.setInstrumentations(instrumentations);

        Builder builder = RuntimeWiring.newRuntimeWiring();
        this.runtimeWiringBuilder.accept(builder);
        RuntimeWiring runtimeWiring = builder.build();
        factory.setRuntimeWiring(runtimeWiring);
        return factory;
    }
}
