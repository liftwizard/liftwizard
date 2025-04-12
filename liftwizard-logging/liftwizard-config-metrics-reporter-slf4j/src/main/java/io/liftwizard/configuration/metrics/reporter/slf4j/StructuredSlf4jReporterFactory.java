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

package io.liftwizard.configuration.metrics.reporter.slf4j;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.metrics.BaseReporterFactory;
import io.dropwizard.metrics.ReporterFactory;
import io.liftwizard.logging.metrics.structured.Builder;
import io.liftwizard.logging.metrics.structured.StructuredSlf4jReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

/**
 * A {@link ReporterFactory} for {@link StructuredSlf4jReporter} instances.
 */
@JsonTypeName("structured-log")
@AutoService(ReporterFactory.class)
public class StructuredSlf4jReporterFactory extends BaseReporterFactory {

    @NotEmpty
    private String loggerName = "metrics";

    @Nullable
    private String markerName;

    @NotEmpty
    private String message = "metrics";

    @JsonProperty("logger")
    public String getLoggerName() {
        return this.loggerName;
    }

    @JsonProperty("logger")
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getLoggerName());
    }

    @JsonProperty
    @Nullable
    public String getMarkerName() {
        return this.markerName;
    }

    @JsonProperty
    public void setMarkerName(@Nullable String markerName) {
        this.markerName = markerName;
    }

    @JsonProperty
    public String getMessage() {
        return this.message;
    }

    @JsonProperty
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    protected Function<Map<String, Object>, ?> getMapToStructuredObjectFunction() {
        return Function.identity();
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        Builder builder = StructuredSlf4jReporter.forRegistry(registry)
            .convertDurationsTo(this.getDurationUnit())
            .convertRatesTo(this.getRateUnit())
            .filter(this.getFilter())
            .outputTo(this.getLogger())
            .message(this.getMessage())
            .mapToStructuredObjectFunction(this.getMapToStructuredObjectFunction());
        if (this.markerName != null) {
            builder.markWith(MarkerFactory.getMarker(this.markerName));
        }

        return builder.build();
    }
}
