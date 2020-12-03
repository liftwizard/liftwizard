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

package io.liftwizard.graphql.instrumentation.logging;

import java.util.List;

import graphql.ExecutionResult;
import graphql.execution.ExecutionId;
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
import io.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiftwizardGraphQLLoggingInstrumentation
        extends SimpleInstrumentation
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LiftwizardGraphQLLoggingInstrumentation.class);

    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters)
    {
        try
        {
            ExecutionId       executionId = parameters.getExecutionInput().getExecutionId();
            MultiMDCCloseable mdc         = new MultiMDCCloseable();
            mdc.put("liftwizard.graphql.executionId", executionId.toString());

            return new MDCInstrumentationContext<>(mdc);
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("", e);
            throw e;
        }
    }

    @Override
    public InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters parameters)
    {
        try
        {
            MultiMDCCloseable mdc       = new MultiMDCCloseable();
            String            operation = parameters.getOperation();
            mdc.put("liftwizard.graphql.operation", operation);
            return new MDCInstrumentationContext<>(mdc);
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("", e);
            throw e;
        }
    }

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters)
    {
        try
        {
            MultiMDCCloseable mdc       = new MultiMDCCloseable();
            String            operation = parameters.getOperation();
            mdc.put("liftwizard.graphql.operation", operation);

            return new MDCInstrumentationContext<>(mdc);
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("", e);
            throw e;
        }
    }

    @Override
    public InstrumentationContext<Object> beginFieldFetch(InstrumentationFieldFetchParameters parameters)
    {
        try
        {
            if (parameters.isTrivialDataFetcher())
            {
                return super.beginFieldFetch(parameters);
            }

            var         mdc            = new MultiMDCCloseable();
            var         stepInfo       = parameters.getExecutionStepInfo();
            String      path           = GraphQLInstrumentationUtils.getPathWithIndex(stepInfo);
            GraphQLType parentType     = stepInfo.getParent().getType();
            String      parentTypeName = GraphQLInstrumentationUtils.getTypeName(parentType);
            String      fieldName      = parameters.getField().getName();
            GraphQLType fieldType      = parameters.getField().getType();
            String      fieldTypeName  = GraphQLInstrumentationUtils.getTypeName(fieldType);

            mdc.put("liftwizard.graphql.field.path", path);
            mdc.put("liftwizard.graphql.field.parentType", parentTypeName);
            mdc.put("liftwizard.graphql.field.name", fieldName);
            mdc.put("liftwizard.graphql.field.type", fieldTypeName);

            return new MDCInstrumentationContext<>(mdc);
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("", e);
            throw e;
        }
    }

    @Override
    public DataFetcher<?> instrumentDataFetcher(
            DataFetcher<?> dataFetcher,
            InstrumentationFieldFetchParameters parameters)
    {
        if (parameters.isTrivialDataFetcher())
        {
            return super.instrumentDataFetcher(dataFetcher, parameters);
        }

        try
        {
            return new MDCDataFetcher<>(dataFetcher);
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("", e);
            throw e;
        }
    }
}
