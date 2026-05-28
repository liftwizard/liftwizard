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

package io.liftwizard.dropwizard.bundle.metrics.dogstatsd;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.liftwizard.dropwizard.bundle.metrics.dogstatsd.RecordingStatsDClient.Call;
import io.liftwizard.dropwizard.bundle.metrics.dogstatsd.RecordingStatsDClient.Kind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PushingMetricRegistryTest {

	private RecordingStatsDClient statsd;
	private PushingMetricRegistry registry;

	@BeforeEach
	void setUp() {
		this.statsd = new RecordingStatsDClient();
		this.registry = new PushingMetricRegistry(this.statsd);
	}

	@Test
	void counterReturnsPushingVariantAndIncrementPushesCount() {
		Counter counter = this.registry.counter("orders.placed");

		assertThat(counter).isInstanceOf(PushingCounter.class);

		counter.inc(3);

		assertThat(counter.getCount()).isEqualTo(3);
		assertThat(this.statsd.getCalls()).hasSize(1);
		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.kind).isEqualTo(Kind.COUNT);
		assertThat(call.name).isEqualTo("orders.placed");
		assertThat(call.value).isEqualTo(3.0);
		assertThat(call.tags).isEmpty();
	}

	@Test
	void counterDecrementPushesNegativeCount() {
		Counter counter = this.registry.counter("queue.depth");
		counter.dec(2);

		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.kind).isEqualTo(Kind.COUNT);
		assertThat(call.value).isEqualTo(-2.0);
	}

	@Test
	void counterDecodesTagsFromEncodedName() {
		Counter counter = this.registry.counter("requests[endpoint:/users,method:GET]");
		counter.inc();

		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.name).isEqualTo("requests");
		assertThat(call.tags).containsExactly("endpoint:/users", "method:GET");
	}

	@Test
	void secondLookupReturnsSamePushingInstance() {
		Counter first = this.registry.counter("orders.placed");
		Counter second = this.registry.counter("orders.placed");
		assertThat(second).isSameAs(first);
	}

	@Test
	void meterMarkPushesAsCount() {
		Meter meter = this.registry.meter("events.received");
		meter.mark(5);

		assertThat(meter).isInstanceOf(PushingMeter.class);
		assertThat(meter.getCount()).isEqualTo(5);
		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.kind).isEqualTo(Kind.COUNT);
		assertThat(call.value).isEqualTo(5.0);
	}

	@Test
	void histogramUpdatePushesAsHistogram() {
		Histogram histogram = this.registry.histogram("response.size");
		histogram.update(42);

		assertThat(histogram).isInstanceOf(PushingHistogram.class);
		assertThat(this.statsd.getCalls()).hasSize(1);
		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.kind).isEqualTo(Kind.HISTOGRAM);
		assertThat(call.value).isEqualTo(42.0);
	}

	@Test
	void timerUpdatePushesAsExecutionTimeInMillis() {
		Timer timer = this.registry.timer("request.latency");
		timer.update(250, TimeUnit.MILLISECONDS);

		assertThat(timer).isInstanceOf(PushingTimer.class);
		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.kind).isEqualTo(Kind.EXECUTION_TIME);
		assertThat(call.value).isEqualTo(250.0);
	}

	@Test
	void settableGaugeSetValuePushesGauge() {
		var gauge = new PushingSettableGauge<Integer>("queue.size", () -> this.statsd);
		gauge.setValue(7);

		assertThat(gauge.getValue()).isEqualTo(7);
		Call call = this.statsd.getCalls().getFirst();
		assertThat(call.kind).isEqualTo(Kind.GAUGE);
		assertThat(call.value).isEqualTo(7.0);
	}

	@Test
	void clientSwapAffectsSubsequentPushes() {
		Counter counter = this.registry.counter("hits");
		counter.inc();
		assertThat(this.statsd.getCalls()).hasSize(1);

		var replacement = new RecordingStatsDClient();
		this.registry.setStatsDClient(replacement);

		counter.inc();
		assertThat(this.statsd.getCalls()).hasSize(1);
		assertThat(replacement.getCalls()).hasSize(1);
	}

	@Test
	void pushingSettableGaugeIsAGaugeButShouldBeExcludedFromPollingFilter() {
		var gauge = new PushingSettableGauge<Integer>("queue.size", () -> this.statsd);
		assertThat(gauge)
				.isInstanceOf(Gauge.class)
				.isInstanceOf(PushingSettableGauge.class);
	}
}
