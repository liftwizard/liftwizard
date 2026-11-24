/*
 * Copyright 2026 Craig Motlin
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

package io.liftwizard.graphql.instrumentation.limits;

import graphql.ExecutionResult;
import graphql.analysis.QueryTraverser;
import graphql.analysis.QueryVisitorFieldEnvironment;
import graphql.execution.AbortExecutionException;
import graphql.execution.ExecutionContext;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;

/**
 * Like {@link graphql.analysis.MaxQueryDepthInstrumentation}, but ignores introspection fields. Introspection reads only the static schema, yet the standard introspection query nests deeper than any data query needs, so counting it would force a limit loose enough to allow long chains of to-many relationships.
 */
public class MaxDataQueryDepthInstrumentation
	extends SimplePerformantInstrumentation
{
	private final int maxDepth;

	public MaxDataQueryDepthInstrumentation(int maxDepth)
	{
		this.maxDepth = maxDepth;
	}

	@Override
	public InstrumentationContext<ExecutionResult> beginExecuteOperation(
		InstrumentationExecuteOperationParameters parameters,
		InstrumentationState state
	)
	{
		ExecutionContext executionContext = parameters.getExecutionContext();
		QueryTraverser queryTraverser = QueryTraverser.newQueryTraverser()
			.schema(executionContext.getGraphQLSchema())
			.document(executionContext.getDocument())
			.operationName(executionContext.getExecutionInput().getOperationName())
			.coercedVariables(executionContext.getCoercedVariables())
			.build();

		int depth = queryTraverser.reducePreOrder(
			(environment, maxSoFar) -> Math.max(maxSoFar, getDataDepth(environment)),
			0
		);
		if (depth > this.maxDepth)
		{
			throw new AbortExecutionException("maximum query depth exceeded " + depth + " > " + this.maxDepth);
		}
		return super.beginExecuteOperation(parameters, state);
	}

	private static int getDataDepth(QueryVisitorFieldEnvironment environment)
	{
		var depth = 1;
		QueryVisitorFieldEnvironment root = environment;
		while (root.getParentEnvironment() != null)
		{
			root = root.getParentEnvironment();
			depth++;
		}
		return root.getField().getName().startsWith("__") ? 0 : depth;
	}
}
