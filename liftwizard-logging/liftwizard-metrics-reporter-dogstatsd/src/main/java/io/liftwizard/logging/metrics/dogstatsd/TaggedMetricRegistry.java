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

package io.liftwizard.logging.metrics.dogstatsd;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * A {@link MetricRegistry} that stores per-metric Datadog tags out-of-band, keyed by the
 * bare (untagged) metric name. The {@link MetricRegistry} itself stays tagless — names
 * are plain Dropwizard names and remain compatible with every other reporter (JMX,
 * Prometheus, slf4j). Reporters that want tags call {@link #getTags(String)}.
 *
 * <p>Tag arrays are first-write-wins: subsequent registrations of the same name with
 * different tags are silently ignored (the first set of tags survives). This matches
 * Dropwizard's metric-creation semantics where the first caller "owns" the metric.
 */
public class TaggedMetricRegistry extends MetricRegistry {

	private static final String[] EMPTY_TAGS = new String[0];

	private final ConcurrentMap<String, String[]> tagsByName = new ConcurrentHashMap<>();

	public String[] getTags(String name) {
		return this.tagsByName.getOrDefault(name, EMPTY_TAGS);
	}

	public Counter counter(String name, String... tags) {
		this.recordTags(name, tags);
		return this.counter(name);
	}

	public Meter meter(String name, String... tags) {
		this.recordTags(name, tags);
		return this.meter(name);
	}

	public Histogram histogram(String name, String... tags) {
		this.recordTags(name, tags);
		return this.histogram(name);
	}

	public Timer timer(String name, String... tags) {
		this.recordTags(name, tags);
		return this.timer(name);
	}

	public <T extends Metric> T register(String name, T metric, String... tags) {
		this.recordTags(name, tags);
		return this.register(name, metric);
	}

	private void recordTags(String name, String[] tags) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(tags);
		if (tags.length == 0) {
			return;
		}
		this.tagsByName.putIfAbsent(name, tags.clone());
	}
}
