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

package io.liftwizard.graphql.instrumentation.metrics;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentedDataFetcherTest
{
	@Test
	void asyncCompletionRecordsAsyncTimers()
		throws Exception
	{
		MetricRegistry metricRegistry = new MetricRegistry();
		AsyncDataFetcher asyncDataFetcher = new AsyncDataFetcher();
		var instrumentedDataFetcher = new InstrumentedDataFetcher<>(
			metricRegistry,
			asyncDataFetcher,
			"items",
			"Query",
			"items"
		);

		CompletableFuture<String> result = instrumentedDataFetcher.get(null);
		asyncDataFetcher.future.complete("value");

		assertThat(result).isCompletedWithValue("value");
		Map<String, Long> timerCounts = new TreeMap<>();
		metricRegistry.getTimers().forEach((name, timer) -> timerCounts.put(name, timer.getCount()));
		assertThat(timerCounts).isEqualTo(
			Map.of(
				"asyncFetcher.sync",
				1L,
				"asyncFetcher.async",
				1L,
				"liftwizard.graphql.field.Query.items.sync",
				1L,
				"liftwizard.graphql.field.Query.items.async",
				1L,
				"liftwizard.graphql.path.items.sync",
				1L,
				"liftwizard.graphql.path.items.async",
				1L
			)
		);
	}

	private static final class AsyncDataFetcher
		implements DataFetcher<CompletableFuture<String>>
	{
		private final CompletableFuture<String> future = new CompletableFuture<>();

		@Override
		@Timed(name = "asyncFetcher", absolute = true)
		public CompletableFuture<String> get(DataFetchingEnvironment environment)
		{
			return this.future;
		}
	}
}
