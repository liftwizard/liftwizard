/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.logging.metrics.structured;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.liftwizard.logging.metrics.structured.proxy.AbstractLoggerProxy;
import org.slf4j.Marker;

/**
 * @see Slf4jReporter
 */
public class StructuredSlf4jReporter
        extends ScheduledReporter
{
    private final AbstractLoggerProxy loggerProxy;
    private final Marker marker;
    private final String prefix;
    private final String message;
    private final Function<Map<String, Object>, ?> mapToStructuredObjectFunction;

    public StructuredSlf4jReporter(
            MetricRegistry registry,
            AbstractLoggerProxy loggerProxy,
            Marker marker,
            String prefix,
            TimeUnit rateUnit,
            TimeUnit durationUnit,
            MetricFilter filter,
            ScheduledExecutorService executor,
            boolean shutdownExecutorOnStop,
            Set<MetricAttribute> disabledMetricAttributes,
            Function<Map<String, Object>, ?> mapToStructuredObjectFunction,
            String message)
    {
        super(
                registry,
                "structured-logger-reporter",
                filter,
                rateUnit,
                durationUnit,
                executor,
                shutdownExecutorOnStop,
                disabledMetricAttributes);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
        this.prefix = prefix;
        this.mapToStructuredObjectFunction = mapToStructuredObjectFunction;
        this.message = message;
    }

    /**
     * Returns a new {@link Builder} for {@link StructuredSlf4jReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link StructuredSlf4jReporter}
     */
    public static Builder forRegistry(MetricRegistry registry)
    {
        return new Builder(registry);
    }

    @Override
    public void report(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers)
    {
        if (!this.loggerProxy.isEnabled(this.marker))
        {
            return;
        }

        gauges.forEach(this::logGauge);
        counters.forEach(this::logCounter);
        histograms.forEach(this::logHistogram);
        meters.forEach(this::logMeter);
        timers.forEach(this::logTimer);
    }

    private void logTimer(String name, Timer timer)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metric_type", "TIMER");
        map.put("metric_name", this.prefix(name));
        Snapshot snapshot = timer.getSnapshot();
        this.appendCountIfEnabled(map, timer);
        this.appendLongDurationIfEnabled(map, MetricAttribute.MIN, snapshot::getMin);
        this.appendLongDurationIfEnabled(map, MetricAttribute.MAX, snapshot::getMax);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.MEAN, snapshot::getMean);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.STDDEV, snapshot::getStdDev);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.P50, snapshot::getMedian);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.P75, snapshot::get75thPercentile);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.P95, snapshot::get95thPercentile);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.P98, snapshot::get98thPercentile);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.P99, snapshot::get99thPercentile);
        this.appendDoubleDurationIfEnabled(map, MetricAttribute.P999, snapshot::get999thPercentile);
        this.appendMetered(map, timer);
        map.put("metric_rate_unit", this.getRateUnit());
        map.put("metric_duration_unit", this.getDurationUnit());
        this.log(map);
    }

    private void logMeter(String name, Metered meter)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metric_type", "METER");
        map.put("metric_name", this.prefix(name));
        this.appendCountIfEnabled(map, meter);
        this.appendMetered(map, meter);
        map.put("metric_rate_unit", this.getRateUnit());
        this.log(map);
    }

    private void logHistogram(String name, Histogram histogram)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metric_type", "HISTOGRAM");
        map.put("metric_name", this.prefix(name));
        Snapshot snapshot = histogram.getSnapshot();
        this.appendCountIfEnabled(map, histogram);
        this.appendLongIfEnabled(map, MetricAttribute.MIN, snapshot::getMin);
        this.appendLongIfEnabled(map, MetricAttribute.MAX, snapshot::getMax);
        this.appendDoubleIfEnabled(map, MetricAttribute.MEAN, snapshot::getMean);
        this.appendDoubleIfEnabled(map, MetricAttribute.STDDEV, snapshot::getStdDev);
        this.appendDoubleIfEnabled(map, MetricAttribute.P50, snapshot::getMedian);
        this.appendDoubleIfEnabled(map, MetricAttribute.P75, snapshot::get75thPercentile);
        this.appendDoubleIfEnabled(map, MetricAttribute.P95, snapshot::get95thPercentile);
        this.appendDoubleIfEnabled(map, MetricAttribute.P98, snapshot::get98thPercentile);
        this.appendDoubleIfEnabled(map, MetricAttribute.P99, snapshot::get99thPercentile);
        this.appendDoubleIfEnabled(map, MetricAttribute.P999, snapshot::get999thPercentile);
        this.log(map);
    }

    private void logCounter(String name, Counter counter)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metric_type", "COUNTER");
        map.put("metric_name", this.prefix(name));
        map.put(MetricAttribute.COUNT.getCode(), counter.getCount());
        this.log(map);
    }

    private void logGauge(String name, Gauge<?> gauge)
    {
        Object value = gauge.getValue();
        if (!(value instanceof Number))
        {
            return;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("metric_type", "GAUGE");
        map.put("metric_name", this.prefix(name));
        map.put("metric_value", value);
        this.log(map);
    }

    private void appendLongDurationIfEnabled(
            Map<String, Object> map,
            MetricAttribute metricAttribute,
            Supplier<Long> durationSupplier)
    {
        if (!this.getDisabledMetricAttributes().contains(metricAttribute))
        {
            map.put(metricAttribute.getCode(), this.convertDuration(durationSupplier.get()));
        }
    }

    private void appendDoubleDurationIfEnabled(
            Map<String, Object> map,
            MetricAttribute metricAttribute,
            Supplier<Double> durationSupplier)
    {
        if (!this.getDisabledMetricAttributes().contains(metricAttribute))
        {
            map.put(metricAttribute.getCode(), this.convertDuration(durationSupplier.get()));
        }
    }

    private void appendLongIfEnabled(
            Map<String, Object> map,
            MetricAttribute metricAttribute,
            Supplier<Long> valueSupplier)
    {
        if (!this.getDisabledMetricAttributes().contains(metricAttribute))
        {
            map.put(metricAttribute.getCode(), valueSupplier.get());
        }
    }

    private void appendDoubleIfEnabled(
            Map<String, Object> map,
            MetricAttribute metricAttribute,
            Supplier<Double> valueSupplier)
    {
        if (!this.getDisabledMetricAttributes().contains(metricAttribute))
        {
            map.put(metricAttribute.getCode(), valueSupplier.get());
        }
    }

    private void appendCountIfEnabled(Map<String, Object> map, Counting counting)
    {
        if (!this.getDisabledMetricAttributes().contains(MetricAttribute.COUNT))
        {
            map.put(MetricAttribute.COUNT.getCode(), counting.getCount());
        }
    }

    private void appendMetered(Map<String, Object> map, Metered meter)
    {
        this.appendRateIfEnabled(map, MetricAttribute.M1_RATE, meter::getOneMinuteRate);
        this.appendRateIfEnabled(map, MetricAttribute.M5_RATE, meter::getFiveMinuteRate);
        this.appendRateIfEnabled(map, MetricAttribute.M15_RATE, meter::getFifteenMinuteRate);
        this.appendRateIfEnabled(map, MetricAttribute.MEAN_RATE, meter::getMeanRate);
    }

    private void appendRateIfEnabled(
            Map<String, Object> map,
            MetricAttribute metricAttribute,
            Supplier<Double> rateSupplier)
    {
        if (!this.getDisabledMetricAttributes().contains(metricAttribute))
        {
            map.put(metricAttribute.getCode(), this.convertRate(rateSupplier.get()));
        }
    }

    @Override
    protected String getRateUnit()
    {
        return "events/" + super.getRateUnit();
    }

    private String prefix(String... components)
    {
        return MetricRegistry.name(this.prefix, components);
    }

    private void log(Map<String, Object> map)
    {
        Object structuredObject = this.mapToStructuredObjectFunction.apply(map);
        this.loggerProxy.log(this.marker, this.message, structuredObject);
    }
}
