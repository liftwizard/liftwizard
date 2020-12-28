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

package io.liftwizard.configuration.metrics.reporter.log4j;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.metrics.BaseReporterFactory;
import io.dropwizard.metrics.ReporterFactory;
import io.liftwizard.logging.metrics.structured.log4j.StructuredLog4jReporter;
import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * A {@link ReporterFactory} for {@link StructuredLog4jReporter} instances.
 */
@JsonTypeName("structured-log4j")
@AutoService(ReporterFactory.class)
public class StructuredLog4jReporterFactory
        extends BaseReporterFactory
{
    @NotEmpty
    private String loggerName = "metrics";

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
        return Logger.getLogger(this.getLoggerName());
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry)
    {
        return StructuredLog4jReporter
                .forRegistry(registry)
                .convertDurationsTo(this.getDurationUnit())
                .convertRatesTo(this.getRateUnit())
                .filter(this.getFilter())
                .outputTo(this.getLogger()).build();
    }
}
