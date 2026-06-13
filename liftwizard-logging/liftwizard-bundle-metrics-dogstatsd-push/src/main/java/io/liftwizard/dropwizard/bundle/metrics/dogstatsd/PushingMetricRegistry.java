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
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.timgroup.statsd.StatsDClient;

/**
 * A {@link MetricRegistry} that returns push-on-update variants of the standard mutating
 * metric types ({@link Counter}, {@link Meter}, {@link Histogram}, {@link Timer}) from the
 * usual factory methods.
 *
 * <p>The underlying {@link StatsDClient} can be swapped at runtime via
 * {@link #setStatsDClient(StatsDClient)} — bundles typically install a no-op client at
 * {@code initialize()} time and replace it with a real client after configuration parsing.
 *
 * <p>Pull-style {@code Gauge<T>} suppliers are not intercepted (Dropwizard exposes no read
 * hook); use {@link PushingSettableGauge} when the application owns the value transitions.
 */
public class PushingMetricRegistry extends MetricRegistry {

	private final AtomicReference<StatsDClient> statsdRef;

	public PushingMetricRegistry(StatsDClient initial) {
		this.statsdRef = new AtomicReference<>(Objects.requireNonNull(initial));
	}

	public void setStatsDClient(StatsDClient client) {
		this.statsdRef.set(Objects.requireNonNull(client));
	}

	public StatsDClient getStatsDClient() {
		return this.statsdRef.get();
	}

	@Override
	public Counter counter(String name) {
		return super.counter(name, () -> new PushingCounter(name, this.statsdRef::get));
	}

	@Override
	public Counter counter(String name, MetricSupplier<Counter> supplier) {
		return super.counter(name, () -> new PushingCounter(name, this.statsdRef::get));
	}

	@Override
	public Meter meter(String name) {
		return super.meter(name, () -> new PushingMeter(name, this.statsdRef::get));
	}

	@Override
	public Meter meter(String name, MetricSupplier<Meter> supplier) {
		return super.meter(name, () -> new PushingMeter(name, this.statsdRef::get));
	}

	@Override
	public Histogram histogram(String name) {
		return super.histogram(name, () -> new PushingHistogram(name, this.statsdRef::get));
	}

	@Override
	public Histogram histogram(String name, MetricSupplier<Histogram> supplier) {
		return super.histogram(name, () -> new PushingHistogram(name, this.statsdRef::get));
	}

	@Override
	public Timer timer(String name) {
		return super.timer(name, () -> new PushingTimer(name, this.statsdRef::get));
	}

	@Override
	public Timer timer(String name, MetricSupplier<Timer> supplier) {
		return super.timer(name, () -> new PushingTimer(name, this.statsdRef::get));
	}
}
