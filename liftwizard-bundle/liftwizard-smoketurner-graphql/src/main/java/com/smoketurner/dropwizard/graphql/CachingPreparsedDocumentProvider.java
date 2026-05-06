/*
 * Copyright © 2019 Smoke Turner, LLC (github@smoketurner.com)
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

package com.smoketurner.dropwizard.graphql;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import graphql.ExecutionInput;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingPreparsedDocumentProvider implements PreparsedDocumentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingPreparsedDocumentProvider.class);
	private final Cache<String, PreparsedDocumentEntry> cache;
	private final Meter cacheMisses;

	public CachingPreparsedDocumentProvider(CacheBuilderSpec spec, MetricRegistry registry) {
		LOGGER.info("Query Cache: {}", spec);
		this.cache = CacheBuilder.from(spec).build();

		this.cacheMisses = registry.meter(MetricRegistry.name(CachingPreparsedDocumentProvider.class, "cache-misses"));
	}

	@Override
	public PreparsedDocumentEntry getDocument(
		ExecutionInput executionInput,
		Function<ExecutionInput, PreparsedDocumentEntry> computeFunction
	) {
		final String query = executionInput.getQuery();

		try {
			return this.cache.get(query, () -> {
					LOGGER.debug("Query cache miss: {}", query);
					this.cacheMisses.mark();
					return computeFunction.apply(executionInput);
				});
		} catch (ExecutionException e) {
			LOGGER.error("Unable to get document from cache", e);
		}

		return computeFunction.apply(executionInput);
	}
}
