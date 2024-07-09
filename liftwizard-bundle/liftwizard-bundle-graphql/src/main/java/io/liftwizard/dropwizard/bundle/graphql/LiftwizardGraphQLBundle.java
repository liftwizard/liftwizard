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
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.servlet.ServletRegistration.Dynamic;

import com.codahale.metrics.MetricRegistry;
import com.smoketurner.dropwizard.graphql.CachingPreparsedDocumentProvider;
import com.smoketurner.dropwizard.graphql.GraphQLBundle;
import com.smoketurner.dropwizard.graphql.GraphQLFactory;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import graphql.scalars.java.JavaPrimitives;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.configuration.graphql.GraphQLFactoryProvider;
import io.liftwizard.graphql.instrumentation.logging.LiftwizardGraphQLLoggingInstrumentation;
import io.liftwizard.graphql.instrumentation.metrics.LiftwizardGraphQLMetricsInstrumentation;
import io.liftwizard.graphql.scalar.temporal.GraphQLLocalDateScalar;
import io.liftwizard.graphql.scalar.temporal.GraphQLTemporalScalar;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
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
    private final ImmutableList<Consumer<Builder>> runtimeWiringBuilders;

    private MetricRegistry metricRegistry;

    @SafeVarargs
    public LiftwizardGraphQLBundle(@Nonnull Consumer<Builder>... runtimeWiringBuilders)
    {
        this.runtimeWiringBuilders = Lists.immutable.with(runtimeWiringBuilders);
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

    @Override
    public void run(T configuration, Environment environment)
            throws Exception
    {
        GraphQLFactory factory = this.getGraphQLFactory(configuration);

        PreparsedDocumentProvider provider =
                new CachingPreparsedDocumentProvider(factory.getQueryCache(), environment.metrics());

        GraphQLSchema schema = factory.build();

        GraphQLQueryInvoker queryInvoker =
                GraphQLQueryInvoker.newBuilder()
                        .withPreparsedDocumentProvider(provider)
                        .withInstrumentation(factory.getInstrumentations())
                        .build();

        GraphQLConfiguration config =
                GraphQLConfiguration.with(schema).with(queryInvoker).build();

        GraphQLHttpServlet servlet = GraphQLHttpServlet.with(config);

        Dynamic servletRegistration = environment
                .servlets()
                .addServlet("graphql", servlet);
        servletRegistration.setAsyncSupported(false);
        servletRegistration
                .addMapping("/graphql", "/schema.json");
    }

    @Nonnull
    @Override
    public GraphQLFactory getGraphQLFactory(@Nonnull T configuration)
    {
        // the RuntimeWiring must be configured prior to the run()
        // methods being called so the schema is connected properly.
        GraphQLFactory factory = configuration.getGraphQLFactory();

        // TODO: Move the Clock to Configuration
        List<Instrumentation> instrumentations = this.getInstrumentations();
        factory.setInstrumentations(instrumentations);

        Builder builder = RuntimeWiring.newRuntimeWiring();
        builder
                .scalar(GraphQLTemporalScalar.INSTANT_INSTANCE)
                .scalar(GraphQLTemporalScalar.TEMPORAL_INSTANT_INSTANCE)
                .scalar(GraphQLTemporalScalar.TEMPORAL_RANGE_INSTANCE)
                .scalar(JavaPrimitives.GraphQLLong)
                .scalar(GraphQLLocalDateScalar.INSTANCE);

        for (Consumer<Builder> runtimeWiringBuilder : this.runtimeWiringBuilders)
        {
            runtimeWiringBuilder.accept(builder);
        }
        RuntimeWiring runtimeWiring = builder.build();
        factory.setRuntimeWiring(runtimeWiring);
        return factory;
    }

    @Nonnull
    private List<Instrumentation> getInstrumentations()
    {
        // TODO 2024-07-07: Move the Clock to Configuration
        Clock clock = Clock.systemUTC();

        var metricsInstrumentation = new LiftwizardGraphQLMetricsInstrumentation(this.metricRegistry, clock);
        var loggingInstrumentation = new LiftwizardGraphQLLoggingInstrumentation();

        List<Instrumentation> instrumentations = List.of(metricsInstrumentation, loggingInstrumentation);
        return instrumentations;
    }
}
