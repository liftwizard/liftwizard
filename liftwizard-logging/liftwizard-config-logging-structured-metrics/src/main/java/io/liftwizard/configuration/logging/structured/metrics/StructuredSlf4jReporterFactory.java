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

package io.liftwizard.configuration.logging.structured.metrics;

import javax.annotation.Nullable;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.metrics.BaseReporterFactory;
import io.dropwizard.metrics.ReporterFactory;
import io.liftwizard.logging.metrics.structured.Builder;
import io.liftwizard.logging.metrics.structured.StructuredSlf4jReporter;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

/**
 * A {@link ReporterFactory} for {@link StructuredSlf4jReporter} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>logger</td>
 *         <td>metrics</td>
 *         <td>The name of the logger to write metrics to.</td>
 *     </tr>
 *     <tr>
 *         <td>markerName</td>
 *         <td>(none)</td>
 *         <td>The name of the marker to mark logged metrics with.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("structured-log")
@AutoService(ReporterFactory.class)
public class StructuredSlf4jReporterFactory
        extends BaseReporterFactory
{
    @NotEmpty
    private String loggerName = "metrics";

    @Nullable
    private String markerName;

    @JsonProperty("logger")
    public String getLoggerName()
    {
        return this.loggerName;
    }

    @JsonProperty("logger")
    public void setLoggerName(String loggerName)
    {
        this.loggerName = loggerName;
    }

    public Logger getLogger()
    {
        return LoggerFactory.getLogger(this.getLoggerName());
    }

    @JsonProperty
    @Nullable
    public String getMarkerName()
    {
        return this.markerName;
    }

    @JsonProperty
    public void setMarkerName(@Nullable String markerName)
    {
        this.markerName = markerName;
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry)
    {
        Builder builder = StructuredSlf4jReporter.forRegistry(registry)
                .convertDurationsTo(this.getDurationUnit())
                .convertRatesTo(this.getRateUnit())
                .filter(this.getFilter())
                .outputTo(this.getLogger());
        if (this.markerName != null)
        {
            builder.markWith(MarkerFactory.getMarker(this.markerName));
        }

        return builder.build();
    }
}
