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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricFilter;
import com.timgroup.statsd.Event;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.ServiceCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;

class DogStatsDReporterTest {

	private TaggedMetricRegistry registry;
	private RecordingClient statsd;
	private DogStatsDReporter reporter;

	@BeforeEach
	void setUp() {
		this.registry = new TaggedMetricRegistry();
		this.statsd = new RecordingClient();
		this.reporter = DogStatsDReporter.forRegistry(this.registry)
			.convertRatesTo(SECONDS)
			.convertDurationsTo(MILLISECONDS)
			.build(this.statsd);
	}

	@Test
	void counterReportedAsDeltaSinceLastPoll() {
		Counter counter = this.registry.counter("orders");
		counter.inc(5);

		this.reporter.report();
		assertThat(this.statsd.calls()).containsExactly(new CountCall("orders", 5, List.of()));

		counter.inc(3);
		this.statsd.calls().clear();
		this.reporter.report();
		assertThat(this.statsd.calls()).containsExactly(new CountCall("orders", 3, List.of()));
	}

	@Test
	void counterEmitsNothingWhenDeltaIsZero() {
		this.registry.counter("orders");

		this.reporter.report();
		assertThat(this.statsd.calls()).isEmpty();

		this.reporter.report();
		assertThat(this.statsd.calls()).isEmpty();
	}

	@Test
	void counterDeltaIsNegativeAfterDec() {
		Counter counter = this.registry.counter("inventory");
		counter.inc(10);
		this.reporter.report();
		this.statsd.calls().clear();

		counter.dec(4);
		this.reporter.report();

		assertThat(this.statsd.calls()).containsExactly(new CountCall("inventory", -4, List.of()));
	}

	@Test
	void taggedCounterEmitsTagsOnEveryReport() {
		Counter counter = this.registry.counter("requests", "endpoint:/users");
		counter.inc(1);

		this.reporter.report();

		assertThat(this.statsd.calls()).containsExactly(new CountCall("requests", 1, List.of("endpoint:/users")));
	}

	@Test
	void emptyRegistryReportEmitsNothing() {
		this.reporter.report();
		assertThat(this.statsd.calls()).isEmpty();
	}

	@Test
	void closeStatsdOnStopActuallyClosesClientFromStop() {
		var closingClient = new ClosingClient();
		var localReporter = DogStatsDReporter.forRegistry(this.registry).closeStatsdOnStop(true).build(closingClient);

		localReporter.stop();

		assertThat(closingClient.isClosed()).isTrue();
	}

	@Test
	void stopWithoutCloseStatsdOnStopLeavesClientOpen() {
		var closingClient = new ClosingClient();
		var localReporter = DogStatsDReporter.forRegistry(this.registry).build(closingClient);

		localReporter.stop();

		assertThat(closingClient.isClosed()).isFalse();
	}

	@Test
	void filterAppliesToReportedMetrics() {
		Counter included = this.registry.counter("included");
		Counter excluded = this.registry.counter("excluded");
		included.inc(7);
		excluded.inc(99);
		var filtered = DogStatsDReporter.forRegistry(this.registry)
			.filter((name, metric) -> "included".equals(name))
			.build(this.statsd);

		filtered.report();

		assertThat(this.statsd.calls()).containsExactly(new CountCall("included", 7, List.of()));
	}

	@Test
	void plainRegistryReporterUsesEmptyTags() {
		var plainRegistry = new com.codahale.metrics.MetricRegistry();
		var plainReporter = new DogStatsDReporter(
			plainRegistry,
			this.statsd,
			false,
			MetricFilter.ALL,
			SECONDS,
			MILLISECONDS
		);
		plainRegistry.counter("orders").inc(2);

		plainReporter.report();

		assertThat(this.statsd.calls()).containsExactly(new CountCall("orders", 2, List.of()));
	}

	record CountCall(String name, long delta, List<String> tags) {}

	private static final class RecordingClient extends NoOpStatsDClient {

		private final List<CountCall> calls = new ArrayList<>();

		List<CountCall> calls() {
			return this.calls;
		}

		@Override
		public void count(String name, long delta, String... tags) {
			this.calls.add(new CountCall(name, delta, Arrays.asList(tags)));
		}
	}

	private static final class ClosingClient extends NoOpStatsDClient {

		private boolean closed;

		boolean isClosed() {
			return this.closed;
		}

		@Override
		public void close() {
			this.closed = true;
		}

		@Override
		public void recordEvent(Event event, String... tags) {}

		@Override
		public void recordServiceCheckRun(ServiceCheck sc) {}
	}
}
