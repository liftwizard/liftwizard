/*
 * Copyright 2020 Craig Motlin
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.liftwizard.logging.metrics.structured.proxy.AbstractLoggerProxy;
import io.liftwizard.logging.metrics.structured.proxy.DebugLoggerProxy;
import io.liftwizard.logging.metrics.structured.proxy.ErrorLoggerProxy;
import io.liftwizard.logging.metrics.structured.proxy.InfoLoggerProxy;
import io.liftwizard.logging.metrics.structured.proxy.TraceLoggerProxy;
import io.liftwizard.logging.metrics.structured.proxy.WarnLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * A builder for {@link StructuredSlf4jReporter} instances. Defaults to logging to {@code metrics}, not using a marker, converting rates to events/second, converting durations to milliseconds, and not filtering metrics.
 */
public class Builder
{
    private final MetricRegistry registry;

    private Logger logger = LoggerFactory.getLogger("metrics");
    private LoggingLevel loggingLevel = LoggingLevel.INFO;
    private Marker marker;
    private String prefix = "";
    private TimeUnit rateUnit = TimeUnit.SECONDS;
    private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
    private MetricFilter filter = MetricFilter.ALL;
    private ScheduledExecutorService executor;
    private boolean shutdownExecutorOnStop = true;
    private Set<MetricAttribute> disabledMetricAttributes = Collections.emptySet();
    private Function<Map<String, Object>, ?> mapToStructuredObjectFunction = Function.identity();
    private String message = "metrics";

    public Builder(MetricRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
     * Default value is true.
     * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
     *
     * @param newShutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
     * @return {@code this}
     */
    public Builder shutdownExecutorOnStop(boolean newShutdownExecutorOnStop)
    {
        this.shutdownExecutorOnStop = newShutdownExecutorOnStop;
        return this;
    }

    /**
     * Specifies the executor to use while scheduling reporting of metrics.
     * Default value is null.
     * Null value leads to executor will be auto created on start.
     *
     * @param newExecutor the executor to use while scheduling reporting of metrics.
     * @return {@code this}
     */
    public Builder scheduleOn(ScheduledExecutorService newExecutor)
    {
        this.executor = newExecutor;
        return this;
    }

    /**
     * Log metrics to the given logger.
     *
     * @param newLogger an SLF4J {@link Logger}
     * @return {@code this}
     */
    public Builder outputTo(Logger newLogger)
    {
        this.logger = newLogger;
        return this;
    }

    /**
     * Mark all logged metrics with the given marker.
     *
     * @param newMarker an SLF4J {@link Marker}
     * @return {@code this}
     */
    public Builder markWith(Marker newMarker)
    {
        this.marker = newMarker;
        return this;
    }

    /**
     * Prefix all metric names with the given string.
     *
     * @param newPrefix the prefix for all metric names
     * @return {@code this}
     */
    public Builder prefixedWith(String newPrefix)
    {
        this.prefix = newPrefix;
        return this;
    }

    /**
     * Convert rates to the given time unit.
     *
     * @param newRateUnit a unit of time
     * @return {@code this}
     */
    public Builder convertRatesTo(TimeUnit newRateUnit)
    {
        this.rateUnit = newRateUnit;
        return this;
    }

    /**
     * Convert durations to the given time unit.
     *
     * @param newDurationUnit a unit of time
     * @return {@code this}
     */
    public Builder convertDurationsTo(TimeUnit newDurationUnit)
    {
        this.durationUnit = newDurationUnit;
        return this;
    }

    /**
     * Only report metrics which match the given filter.
     *
     * @param newFilter a {@link MetricFilter}
     * @return {@code this}
     */
    public Builder filter(MetricFilter newFilter)
    {
        this.filter = newFilter;
        return this;
    }

    /**
     * Use Logging Level when reporting.
     *
     * @param newLoggingLevel a (@link Slf4jReporter.LoggingLevel}
     * @return {@code this}
     */
    public Builder withLoggingLevel(LoggingLevel newLoggingLevel)
    {
        this.loggingLevel = newLoggingLevel;
        return this;
    }

    /**
     * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
     * See {@link MetricAttribute}.
     *
     * @param newDisabledMetricAttributes a set of {@link MetricAttribute}
     * @return {@code this}
     */
    public Builder disabledMetricAttributes(Set<MetricAttribute> newDisabledMetricAttributes)
    {
        this.disabledMetricAttributes = newDisabledMetricAttributes;
        return this;
    }

    /**
     * A function to convert the structured argument Map to another type, such as net.logstash.logback.marker.LogstashMarker or net.logstash.logback.argument.StructuredArgument
     *
     * @param newToStructuredObjectFunction A function such as {@code Markers::appendEntries} or {@code StructuredArguments::entries}
     * @return {@code this}
     */
    public Builder mapToStructuredObjectFunction(Function<Map<String, Object>, ?> newToStructuredObjectFunction)
    {
        this.mapToStructuredObjectFunction = newToStructuredObjectFunction;
        return this;
    }

    /**
     * A message to log along with the structured argument object. It may optionally have one placeholder if you want the structured object to appear in the message.
     *
     * @param newMessage A String such as {@code "metrics"} or {@code "metrics: {}"}
     * @return {@code this}
     */
    public Builder message(String newMessage)
    {
        this.message = newMessage;
        return this;
    }

    /**
     * Builds a {@link StructuredSlf4jReporter} with the given properties.
     *
     * @return a {@link StructuredSlf4jReporter}
     */
    public StructuredSlf4jReporter build()
    {
        AbstractLoggerProxy loggerProxy = this.getLoggerProxy();
        return new StructuredSlf4jReporter(
                this.registry,
                loggerProxy,
                this.marker,
                this.prefix,
                this.rateUnit,
                this.durationUnit,
                this.filter,
                this.executor,
                this.shutdownExecutorOnStop,
                this.disabledMetricAttributes,
                this.mapToStructuredObjectFunction,
                this.message);
    }

    private AbstractLoggerProxy getLoggerProxy()
    {
        return switch (this.loggingLevel)
        {
            case TRACE -> new TraceLoggerProxy(this.logger);
            case DEBUG -> new DebugLoggerProxy(this.logger);
            case INFO -> new InfoLoggerProxy(this.logger);
            case WARN -> new WarnLoggerProxy(this.logger);
            case ERROR -> new ErrorLoggerProxy(this.logger);
        };
    }
}
