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

package io.liftwizard.graphql.instrumentation.metrics;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.language.Document;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLType;
import graphql.validation.ValidationError;
import io.liftwizard.instrumentation.GraphQLInstrumentationUtils;

/**
 * An Instrumentation that registers performance metrics about data fetching with Dropwizard's MetricsRegistry.
 *
 * @see <a href="https://liftwizard.io/docs/graphql/instrumentation-metrics">https://liftwizard.io/docs/graphql/instrumentation-metrics</a>
 */
public class LiftwizardGraphQLMetricsInstrumentation
        extends SimpleInstrumentation
{
    private final MetricRegistry metricRegistry;
    private final Clock clock;

    private final Timer allFieldsSyncTimer;
    private final Timer allFieldsAsyncTimer;
    private final Meter allFieldsExceptionsMeter;
    private final Timer executionTimer;
    private final Meter executionExceptionsMeter;
    private final Timer parseTimer;
    private final Meter parseExceptionsMeter;
    private final Timer validationTimer;
    private final Meter validationExceptionsMeter;

    public LiftwizardGraphQLMetricsInstrumentation(MetricRegistry metricRegistry, Clock clock)
    {
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
        this.clock          = Objects.requireNonNull(clock);

        this.allFieldsSyncTimer        = metricRegistry.timer(MetricRegistry.name("liftwizard", "graphql", "field", "sync"));
        this.allFieldsAsyncTimer       = metricRegistry.timer(MetricRegistry.name("liftwizard", "graphql", "field", "async"));
        this.allFieldsExceptionsMeter  = metricRegistry.meter(MetricRegistry.name("liftwizard", "graphql", "field", "exceptions"));
        this.executionTimer            = this.metricRegistry.timer(MetricRegistry.name("liftwizard", "graphql", "execution"));
        this.executionExceptionsMeter  = this.metricRegistry.meter(MetricRegistry.name("liftwizard", "graphql", "execution", "exceptions"));
        this.parseTimer                = this.metricRegistry.timer(MetricRegistry.name("liftwizard", "graphql", "parse"));
        this.parseExceptionsMeter      = this.metricRegistry.meter(MetricRegistry.name("liftwizard", "graphql", "parse", "exceptions"));
        this.validationTimer           = this.metricRegistry.timer(MetricRegistry.name("liftwizard", "graphql", "validation"));
        this.validationExceptionsMeter = this.metricRegistry.meter(MetricRegistry.name("liftwizard", "graphql", "validation", "exceptions"));
    }

    @Override
    @Nonnull
    public InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters)
    {
        return new GlobalInstrumentationContext<>(this.executionTimer, this.executionExceptionsMeter);
    }

    @Override
    @Nonnull
    public InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters parameters)
    {
        return new GlobalInstrumentationContext<>(this.parseTimer, this.parseExceptionsMeter);
    }

    @Override
    @Nonnull
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters)
    {
        return new GlobalInstrumentationContext<>(this.validationTimer, this.validationExceptionsMeter);
    }

    @Override
    @Nonnull
    public InstrumentationContext<Object> beginFieldFetch(@Nonnull InstrumentationFieldFetchParameters parameters)
    {
        if (parameters.isTrivialDataFetcher())
        {
            return super.beginFieldFetch(parameters);
        }

        return new FieldInstrumentationContext(
                this.allFieldsSyncTimer,
                this.allFieldsAsyncTimer,
                this.allFieldsExceptionsMeter);
    }

    @Override
    @Nonnull
    public DataFetcher<?> instrumentDataFetcher(
            @Nonnull DataFetcher<?> dataFetcher,
            @Nonnull InstrumentationFieldFetchParameters parameters)
    {
        if (parameters.isTrivialDataFetcher())
        {
            return dataFetcher;
        }

        String      fieldName = parameters.getField().getName();
        GraphQLType type      = parameters.getExecutionStepInfo().getParent().getType();
        String      typeName  = GraphQLInstrumentationUtils.getTypeName(type);
        String      path      = GraphQLInstrumentationUtils.getPathWithoutIndex(parameters.getExecutionStepInfo());

        return new InstrumentedDataFetcher<>(this.metricRegistry, this.clock, dataFetcher, fieldName, typeName, path);
    }
}
