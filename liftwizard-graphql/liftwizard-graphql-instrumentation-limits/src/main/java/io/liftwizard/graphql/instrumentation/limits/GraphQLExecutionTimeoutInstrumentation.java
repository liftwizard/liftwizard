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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import graphql.ExecutionResult;
import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimplePerformantInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters;
import graphql.schema.DataFetcher;

/**
 * Aborts a GraphQL execution once it has run longer than the timeout.
 *
 * <p>The JVM cannot preempt a running thread, so the deadline is checked cooperatively before each field starts and after each data fetcher returns. Work inside a single data fetcher is not interrupted; the depth and complexity limits bound that work instead.
 */
public class GraphQLExecutionTimeoutInstrumentation
	extends SimplePerformantInstrumentation
{
	private final Clock clock;
	private final Duration timeout;

	public GraphQLExecutionTimeoutInstrumentation(Clock clock, Duration timeout)
	{
		this.clock = Objects.requireNonNull(clock);
		this.timeout = Objects.requireNonNull(timeout);
	}

	@Override
	public InstrumentationState createState(InstrumentationCreateStateParameters parameters)
	{
		return new DeadlineState(this.clock.instant().plus(this.timeout));
	}

	@Override
	public InstrumentationContext<ExecutionResult> beginField(
		InstrumentationFieldParameters parameters,
		InstrumentationState state
	)
	{
		this.assertBeforeDeadline(state);
		return super.beginField(parameters, state);
	}

	@Override
	public DataFetcher<?> instrumentDataFetcher(
		DataFetcher<?> dataFetcher,
		InstrumentationFieldFetchParameters parameters,
		InstrumentationState state
	)
	{
		return (environment) ->
		{
			Object result = dataFetcher.get(environment);
			this.assertBeforeDeadline(state);
			return result;
		};
	}

	private void assertBeforeDeadline(InstrumentationState state)
	{
		Instant deadline = ((DeadlineState) state).deadline();
		if (this.clock.instant().isAfter(deadline))
		{
			throw new AbortExecutionException("GraphQL execution exceeded the timeout of " + this.timeout + ".");
		}
	}

	private record DeadlineState(Instant deadline)
		implements InstrumentationState
	{
	}
}
