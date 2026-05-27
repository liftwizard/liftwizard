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
 * <p>Metric names registered with the {@link MetricRegistry} may carry Datadog tags using
 * the {@link TaggedMetricName} encoding (e.g. {@code "request.latency[endpoint:/users]"}).
 * The reporter decodes the tags off the name before forwarding to the {@link StatsDClient}.
 */
public class DogStatsDReporter extends ScheduledReporter {

	private final StatsDClient statsd;
	private final boolean closeStatsdOnStop;

	public DogStatsDReporter(
		MetricRegistry registry,
		StatsDClient statsd,
		boolean closeStatsdOnStop,
		MetricFilter filter,
		TimeUnit rateUnit,
		TimeUnit durationUnit
	) {
		super(registry, "dogstatsd-reporter", filter, rateUnit, durationUnit);
		this.statsd = Objects.requireNonNull(statsd);
		this.closeStatsdOnStop = closeStatsdOnStop;
	}

	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	@Override
	public void close() {
		super.close();
		if (this.closeStatsdOnStop) {
			this.statsd.close();
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

	private void reportGauge(String encodedName, Gauge<?> gauge) {
		Object value = gauge.getValue();
		if (!(value instanceof Number)) {
			return;
		}
		TaggedMetricName tmn = TaggedMetricName.decode(encodedName);
		this.statsd.gauge(tmn.getName(), ((Number) value).doubleValue(), tmn.getTags());
	}

	private void reportCounter(String encodedName, Counter counter) {
		TaggedMetricName tmn = TaggedMetricName.decode(encodedName);
		this.statsd.gauge(tmn.getName(), counter.getCount(), tmn.getTags());
	}

	private void reportHistogram(String encodedName, Histogram histogram) {
		TaggedMetricName tmn = TaggedMetricName.decode(encodedName);
		String name = tmn.getName();
		String[] tags = tmn.getTags();
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

	private void reportMeter(String encodedName, Meter meter) {
		TaggedMetricName tmn = TaggedMetricName.decode(encodedName);
		String name = tmn.getName();
		String[] tags = tmn.getTags();
		this.statsd.gauge(name + ".count", meter.getCount(), tags);
		this.statsd.gauge(name + ".m1_rate", this.convertRate(meter.getOneMinuteRate()), tags);
		this.statsd.gauge(name + ".m5_rate", this.convertRate(meter.getFiveMinuteRate()), tags);
		this.statsd.gauge(name + ".m15_rate", this.convertRate(meter.getFifteenMinuteRate()), tags);
		this.statsd.gauge(name + ".mean_rate", this.convertRate(meter.getMeanRate()), tags);
	}

	private void reportTimer(String encodedName, Timer timer) {
		TaggedMetricName tmn = TaggedMetricName.decode(encodedName);
		String name = tmn.getName();
		String[] tags = tmn.getTags();
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
