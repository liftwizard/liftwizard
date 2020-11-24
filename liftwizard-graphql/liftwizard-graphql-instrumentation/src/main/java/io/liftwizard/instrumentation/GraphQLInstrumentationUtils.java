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

package io.liftwizard.instrumentation;

import graphql.execution.ExecutionPath;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLType;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.MutableStack;

public final class GraphQLInstrumentationUtils
{
    private GraphQLInstrumentationUtils()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String getPathWithIndex(ExecutionStepInfo executionStepInfo)
    {
        return getPath(executionStepInfo, true);
    }

    public static String getPathWithoutIndex(ExecutionStepInfo executionStepInfo)
    {
        return getPath(executionStepInfo, false);
    }

    public static String getPath(ExecutionStepInfo executionStepInfo, boolean withIndex)
    {
        MutableStack<String> result = Stacks.mutable.empty();
        getPath(executionStepInfo, result, withIndex);
        return result.makeString("/", "/", "");
    }

    public static void getPath(ExecutionStepInfo executionStepInfo, MutableStack<String> stack, boolean withIndex)
    {
        ExecutionPath path = executionStepInfo.getPath();
        if (path.isRootPath())
        {
            return;
        }

        if (path.isListSegment())
        {
            String indexSuffix = withIndex ? String.format("[%d]", path.getSegmentIndex()) : "";
            String name        = executionStepInfo.getField().getName() + indexSuffix;
            stack.push(name);

            getPath(executionStepInfo.getParent().getParent(), stack, withIndex);
        }
        else
        {
            stack.push(executionStepInfo.getField().getName());
            getPath(executionStepInfo.getParent(), stack, withIndex);
        }
    }

    public static String getTypeName(GraphQLType type)
    {
        if (type instanceof GraphQLNamedSchemaElement)
        {
            return ((GraphQLNamedSchemaElement) type).getName();
        }

        if (type instanceof GraphQLModifiedType)
        {
            GraphQLType wrappedType = ((GraphQLModifiedType) type).getWrappedType();
            return getTypeName(wrappedType);
        }

        throw new AssertionError(type);
    }
}
