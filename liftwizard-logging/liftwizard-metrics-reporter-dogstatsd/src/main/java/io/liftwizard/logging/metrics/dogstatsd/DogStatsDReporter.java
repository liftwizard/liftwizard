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
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.timgroup.statsd.StatsDClient;

/**
 * A Dropwizard {@link ScheduledReporter} that pushes metrics to a DogStatsD agent.
 *
 * <p>If the {@link MetricRegistry} is a {@link TaggedMetricRegistry}, per-metric Datadog
 * tags are looked up from it; otherwise no per-metric tags are sent (constant tags
 * configured on the {@link StatsDClient} still apply).
 *
 * <p>{@link Counter} values are reported as DogStatsD counts of the delta since the last
 * poll, matching the in-flight push semantics in
 * {@code liftwizard-bundle-metrics-dogstatsd-push}'s {@code PushingCounter}.
 */
public class DogStatsDReporter extends ScheduledReporter {

	private static final String[] EMPTY_TAGS = new String[0];

	private final TaggedMetricRegistry taggedRegistry;
	private final StatsDClient statsd;
	private final boolean closeStatsdOnStop;
	private final ConcurrentMap<String, Long> lastReportedCount = new ConcurrentHashMap<>();

	public DogStatsDReporter(
		MetricRegistry registry,
		StatsDClient statsd,
		boolean closeStatsdOnStop,
		MetricFilter filter,
		TimeUnit rateUnit,
		TimeUnit durationUnit
	) {
		super(registry, "dogstatsd-reporter", filter, rateUnit, durationUnit);
		this.taggedRegistry = registry instanceof TaggedMetricRegistry tagged ? tagged : null;
		this.statsd = Objects.requireNonNull(statsd);
		this.closeStatsdOnStop = closeStatsdOnStop;
	}

	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	@Override
	public void stop() {
		super.stop();
		if (this.closeStatsdOnStop) {
			// ScheduledReporter.report() acquires `synchronized (this)` for each cycle;
			// hold the same monitor here so we don't close the StatsDClient mid-flush.
			synchronized (this) {
				this.statsd.close();
			}
		}
	}

	@Override
	public void report(
		SortedMap<String, Gauge> gauges,
		SortedMap<String, Counter> counters,
		SortedMap<String, Histogram> histograms,
		SortedMap<String, Meter> meters,
		SortedMap<String, Timer> timers
	) {
		gauges.forEach(this::reportGauge);
		counters.forEach(this::reportCounter);
		histograms.forEach(this::reportHistogram);
		meters.forEach(this::reportMeter);
		timers.forEach(this::reportTimer);
	}

	private void reportGauge(String name, Gauge<?> gauge) {
		Object value = gauge.getValue();
		if (!(value instanceof Number number)) {
			return;
		}
		this.statsd.gauge(name, number.doubleValue(), this.tagsFor(name));
	}

	private void reportCounter(String name, Counter counter) {
		long current = counter.getCount();
		long previous = this.lastReportedCount.getOrDefault(name, 0L);
		long delta = current - previous;
		this.lastReportedCount.put(name, current);
		if (delta != 0) {
			this.statsd.count(name, delta, this.tagsFor(name));
		}
	}

	private void reportHistogram(String name, Histogram histogram) {
		String[] tags = this.tagsFor(name);
		Snapshot snapshot = histogram.getSnapshot();
		this.statsd.gauge(name + ".count", histogram.getCount(), tags);
		this.statsd.gauge(name + ".min", snapshot.getMin(), tags);
		this.statsd.gauge(name + ".max", snapshot.getMax(), tags);
		this.statsd.gauge(name + ".mean", snapshot.getMean(), tags);
		this.statsd.gauge(name + ".stddev", snapshot.getStdDev(), tags);
		this.statsd.gauge(name + ".p50", snapshot.getMedian(), tags);
		this.statsd.gauge(name + ".p75", snapshot.get75thPercentile(), tags);
		this.statsd.gauge(name + ".p95", snapshot.get95thPercentile(), tags);
		this.statsd.gauge(name + ".p98", snapshot.get98thPercentile(), tags);
		this.statsd.gauge(name + ".p99", snapshot.get99thPercentile(), tags);
		this.statsd.gauge(name + ".p999", snapshot.get999thPercentile(), tags);
	}

	private void reportMeter(String name, Meter meter) {
		String[] tags = this.tagsFor(name);
		this.statsd.gauge(name + ".count", meter.getCount(), tags);
		this.statsd.gauge(name + ".m1_rate", this.convertRate(meter.getOneMinuteRate()), tags);
		this.statsd.gauge(name + ".m5_rate", this.convertRate(meter.getFiveMinuteRate()), tags);
		this.statsd.gauge(name + ".m15_rate", this.convertRate(meter.getFifteenMinuteRate()), tags);
		this.statsd.gauge(name + ".mean_rate", this.convertRate(meter.getMeanRate()), tags);
	}

	private void reportTimer(String name, Timer timer) {
		String[] tags = this.tagsFor(name);
		Snapshot snapshot = timer.getSnapshot();
		this.statsd.gauge(name + ".count", timer.getCount(), tags);
		this.statsd.gauge(name + ".min", this.convertDuration(snapshot.getMin()), tags);
		this.statsd.gauge(name + ".max", this.convertDuration(snapshot.getMax()), tags);
		this.statsd.gauge(name + ".mean", this.convertDuration(snapshot.getMean()), tags);
		this.statsd.gauge(name + ".stddev", this.convertDuration(snapshot.getStdDev()), tags);
		this.statsd.gauge(name + ".p50", this.convertDuration(snapshot.getMedian()), tags);
		this.statsd.gauge(name + ".p75", this.convertDuration(snapshot.get75thPercentile()), tags);
		this.statsd.gauge(name + ".p95", this.convertDuration(snapshot.get95thPercentile()), tags);
		this.statsd.gauge(name + ".p98", this.convertDuration(snapshot.get98thPercentile()), tags);
		this.statsd.gauge(name + ".p99", this.convertDuration(snapshot.get99thPercentile()), tags);
		this.statsd.gauge(name + ".p999", this.convertDuration(snapshot.get999thPercentile()), tags);
		this.statsd.gauge(name + ".m1_rate", this.convertRate(timer.getOneMinuteRate()), tags);
		this.statsd.gauge(name + ".m5_rate", this.convertRate(timer.getFiveMinuteRate()), tags);
		this.statsd.gauge(name + ".m15_rate", this.convertRate(timer.getFifteenMinuteRate()), tags);
		this.statsd.gauge(name + ".mean_rate", this.convertRate(timer.getMeanRate()), tags);
	}

	private String[] tagsFor(String name) {
		return this.taggedRegistry == null ? EMPTY_TAGS : this.taggedRegistry.getTags(name);
	}

	public static final class Builder {

		private final MetricRegistry registry;
		private MetricFilter filter = MetricFilter.ALL;
		private TimeUnit rateUnit = TimeUnit.SECONDS;
		private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
		private boolean closeStatsdOnStop;

		private Builder(MetricRegistry registry) {
			this.registry = Objects.requireNonNull(registry);
		}

		public Builder filter(MetricFilter newFilter) {
			this.filter = Objects.requireNonNull(newFilter);
			return this;
		}

		public Builder convertRatesTo(TimeUnit newRateUnit) {
			this.rateUnit = Objects.requireNonNull(newRateUnit);
			return this;
		}

		public Builder convertDurationsTo(TimeUnit newDurationUnit) {
			this.durationUnit = Objects.requireNonNull(newDurationUnit);
			return this;
		}

		public Builder closeStatsdOnStop(boolean newCloseStatsdOnStop) {
			this.closeStatsdOnStop = newCloseStatsdOnStop;
			return this;
		}

		public DogStatsDReporter build(StatsDClient statsd) {
			return new DogStatsDReporter(
				this.registry,
				statsd,
				this.closeStatsdOnStop,
				this.filter,
				this.rateUnit,
				this.durationUnit
			);
		}
	}
}
