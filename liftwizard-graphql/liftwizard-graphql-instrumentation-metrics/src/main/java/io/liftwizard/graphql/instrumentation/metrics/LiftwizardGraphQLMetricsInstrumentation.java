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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

public class LiftwizardGraphQLMetricsInstrumentation
        extends SimpleInstrumentation
{
    private final MetricRegistry metricRegistry;

    private final ConcurrentMap<DataFetcher<?>, MeteredDataFetcher<?>> instrumentedDataFetchers = new ConcurrentHashMap<>();

    private final Timer allFieldsSyncTimer;
    private final Timer allFieldsAsyncTimer;
    private final Meter allFieldsExceptionsMeter;
    private final Timer executionTimer;
    private final Meter executionExceptionsMeter;
    private final Timer parseTimer;
    private final Meter parseExceptionsMeter;
    private final Timer validationTimer;
    private final Meter validationExceptionsMeter;

    public LiftwizardGraphQLMetricsInstrumentation(MetricRegistry metricRegistry)
    {
        this.metricRegistry = Objects.requireNonNull(metricRegistry);

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
    public InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters)
    {
        return new GlobalInstrumentationContext<>(this.executionTimer, this.executionExceptionsMeter);
    }

    @Override
    public InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters parameters)
    {
        return new GlobalInstrumentationContext<>(this.parseTimer, this.parseExceptionsMeter);
    }

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters)
    {
        return new GlobalInstrumentationContext<>(this.validationTimer, this.validationExceptionsMeter);
    }

    @Override
    public InstrumentationContext<Object> beginFieldFetch(InstrumentationFieldFetchParameters parameters)
    {
        if (parameters.isTrivialDataFetcher())
        {
            return super.beginFieldFetch(parameters);
        }

        String fieldName = parameters.getField().getName();
        GraphQLType type = parameters.getExecutionStepInfo().getParent().getType();
        String typeName  = GraphQLInstrumentationUtils.getTypeName(type);
        String path      = GraphQLInstrumentationUtils.getPathWithoutIndex(parameters.getExecutionStepInfo());

        return new FieldInstrumentationContext(
                this.metricRegistry,
                this.allFieldsSyncTimer,
                this.allFieldsAsyncTimer,
                this.allFieldsExceptionsMeter,
                path,
                fieldName,
                typeName);
    }

    @Override
    public DataFetcher<?> instrumentDataFetcher(
            @Nonnull DataFetcher<?> dataFetcher,
            @Nonnull InstrumentationFieldFetchParameters parameters)
    {
        if (parameters.isTrivialDataFetcher())
        {
            return dataFetcher;
        }

        return this.instrumentedDataFetchers.computeIfAbsent(
                dataFetcher,
                ignored -> new MeteredDataFetcher<>(dataFetcher, this.metricRegistry));
    }
}
