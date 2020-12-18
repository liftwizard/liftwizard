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

import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLType;
import io.liftwizard.instrumentation.GraphQLInstrumentationUtils;

public class LiftwizardGraphQLLoggingInstrumentation
        extends SimpleInstrumentation
{
    @Override
    public DataFetcher<?> instrumentDataFetcher(
            DataFetcher<?> dataFetcher,
            InstrumentationFieldFetchParameters parameters)
    {
        if (parameters.isTrivialDataFetcher())
        {
            return super.instrumentDataFetcher(dataFetcher, parameters);
        }

        var         executionId    = parameters.getExecutionContext().getExecutionId().toString();
        var         stepInfo       = parameters.getExecutionStepInfo();
        String      path           = GraphQLInstrumentationUtils.getPathWithIndex(stepInfo);
        GraphQLType parentType     = stepInfo.getParent().getType();
        String      parentTypeName = GraphQLInstrumentationUtils.getTypeName(parentType);
        String      fieldName      = parameters.getField().getName();
        GraphQLType fieldType      = parameters.getField().getType();
        String      fieldTypeName  = GraphQLInstrumentationUtils.getTypeName(fieldType);

        return new MDCDataFetcher<>(dataFetcher, executionId, path, parentTypeName, fieldName, fieldTypeName);
    }
}
