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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.timgroup.statsd.StatsDClient;
import io.liftwizard.logging.metrics.dogstatsd.TaggedMetricRegistry;

/**
 * A {@link MetricRegistry} that returns push-on-update variants of the standard mutating
 * metric types ({@link Counter}, {@link Meter}, {@link Histogram}, {@link Timer}) from the
 * single-argument factory methods. Per-metric Datadog tags are stored out-of-band on the
 * parent {@link TaggedMetricRegistry}; pass them at metric-creation time via the
 * {@code (String name, String... tags)} overloads.
 *
 * <p>The underlying {@link StatsDClient} can be swapped at runtime via
 * {@link #setStatsDClient(StatsDClient)} — bundles typically install a no-op client at
 * {@code initialize()} time and replace it with a real client after configuration parsing.
 *
 * <p>Pull-style {@code Gauge<T>} suppliers are not intercepted (Dropwizard exposes no read
 * hook); register them via {@link #register(String, Metric, String...)} and rely on a
 * polling reporter, or use {@link PushingSettableGauge} when the application owns the
 * value transitions.
 *
 * <p>Plain {@link Counter}/{@link Meter}/{@link Histogram}/{@link Timer} instances handed
 * to {@link #register(String, Metric, String...)} are rejected with
 * {@link IllegalArgumentException} — wrapping them is impossible because the caller holds
 * the reference. Use the factory methods or pre-wrap with a {@code Pushing*} variant.
 */
public class PushingMetricRegistry extends TaggedMetricRegistry {

	private final AtomicReference<StatsDClient> statsdRef;

	public PushingMetricRegistry(StatsDClient initial) {
		this.statsdRef = new AtomicReference<>(Objects.requireNonNull(initial));
	}

	/**
	 * Replace the active {@link StatsDClient}. Returns the previous client so the caller can
	 * stop/close it.
	 */
	public StatsDClient setStatsDClient(StatsDClient client) {
		return this.statsdRef.getAndSet(Objects.requireNonNull(client));
	}

	public StatsDClient getStatsDClient() {
		return this.statsdRef.get();
	}

	@Override
	public Counter counter(String name) {
		return super.counter(name, () -> new PushingCounter(name, this.getTags(name), this.statsdRef::get));
	}

	@Override
	public Meter meter(String name) {
		return super.meter(name, () -> new PushingMeter(name, this.getTags(name), this.statsdRef::get));
	}

	@Override
	public Histogram histogram(String name) {
		return super.histogram(name, () -> new PushingHistogram(name, this.getTags(name), this.statsdRef::get));
	}

	@Override
	public Timer timer(String name) {
		return super.timer(name, () -> new PushingTimer(name, this.getTags(name), this.statsdRef::get));
	}

	@Override
	public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
		this.rejectPlainMutatingMetric(name, metric);
		return super.register(name, metric);
	}

	private void rejectPlainMutatingMetric(String name, Metric metric) {
		if (
			metric instanceof PushingCounter
			|| metric instanceof PushingMeter
			|| metric instanceof PushingHistogram
			|| metric instanceof PushingTimer
			|| metric instanceof PushingSettableGauge
		) {
			return;
		}
		if (
			metric instanceof Counter
			|| metric instanceof Meter
			|| metric instanceof Histogram
			|| metric instanceof Timer
		) {
			throw new IllegalArgumentException(
				"Refusing to register plain "
				+ metric.getClass().getSimpleName()
				+ " '"
				+ name
				+ "' in a PushingMetricRegistry — its mutations cannot be intercepted. "
				+ "Use the factory methods (counter/meter/histogram/timer) so the registry "
				+ "can return a Pushing variant, or pre-wrap with the corresponding Pushing* class."
			);
		}
	}
}
