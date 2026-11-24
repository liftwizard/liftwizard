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

package io.liftwizard.dropwizard.configuration.graphql.limits;

import java.time.Clock;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import io.dropwizard.util.Duration;
import io.liftwizard.graphql.instrumentation.limits.GraphQLExecutionTimeoutInstrumentation;
import io.liftwizard.graphql.instrumentation.limits.MaxDataQueryDepthInstrumentation;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Bounds on a single GraphQL request. Each nested field in a query can fan out into more data fetches and a larger result, so an unbounded query can exhaust CPU and heap.
 *
 * @see <a href="https://liftwizard.io/docs/graphql/instrumentation-limits">https://liftwizard.io/docs/graphql/instrumentation-limits</a>
 */
public class GraphQLLimitsFactory
{
	private @Min(1) int maxDepth = 8;

	private @Min(1) int maxComplexity = 200;

	private @NotNull Duration timeout = Duration.seconds(30);

	@JsonProperty
	public int getMaxDepth()
	{
		return this.maxDepth;
	}

	@JsonProperty
	public void setMaxDepth(int maxDepth)
	{
		this.maxDepth = maxDepth;
	}

	@JsonProperty
	public int getMaxComplexity()
	{
		return this.maxComplexity;
	}

	@JsonProperty
	public void setMaxComplexity(int maxComplexity)
	{
		this.maxComplexity = maxComplexity;
	}

	@JsonProperty
	public Duration getTimeout()
	{
		return this.timeout;
	}

	@JsonProperty
	public void setTimeout(Duration timeout)
	{
		this.timeout = timeout;
	}

	public ImmutableList<Instrumentation> createInstrumentations(Clock clock)
	{
		return Lists.immutable.with(
			new MaxDataQueryDepthInstrumentation(this.maxDepth),
			new MaxQueryComplexityInstrumentation(this.maxComplexity),
			new GraphQLExecutionTimeoutInstrumentation(clock, this.timeout.toJavaDuration())
		);
	}
}
