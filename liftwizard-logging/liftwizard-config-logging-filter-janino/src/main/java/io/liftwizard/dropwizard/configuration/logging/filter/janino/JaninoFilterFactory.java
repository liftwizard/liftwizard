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

package io.liftwizard.dropwizard.configuration.logging.filter.janino;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.logging.LoggingUtil;
import io.dropwizard.logging.filter.FilterFactory;
import org.hibernate.validator.constraints.NotEmpty;

@JsonTypeName("janino")
@AutoService(FilterFactory.class)
public class JaninoFilterFactory
        implements FilterFactory<ILoggingEvent>
{
    @NotEmpty
    private @Valid @NotNull String javaExpression;

    private @Valid @NotNull FilterReply onMatch    = FilterReply.DENY;
    private @Valid @NotNull FilterReply onMismatch = FilterReply.NEUTRAL;

    @Override
    public Filter<ILoggingEvent> build()
    {
        var evaluator = new JaninoEventEvaluator();
        evaluator.setExpression(this.javaExpression);
        evaluator.setContext(LoggingUtil.getLoggerContext());
        evaluator.start();

        var filter = new EvaluatorFilter<ILoggingEvent>();
        filter.setEvaluator(evaluator);
        filter.setOnMatch(this.onMatch);
        filter.setOnMismatch(this.onMismatch);
        filter.start();

        return filter;
    }

    @JsonProperty
    public String getJavaExpression()
    {
        return this.javaExpression;
    }

    @JsonProperty
    public void setJavaExpression(String javaExpression)
    {
        this.javaExpression = javaExpression;
    }

    @JsonProperty
    public FilterReply getOnMatch()
    {
        return this.onMatch;
    }

    @JsonProperty
    public void setOnMatch(FilterReply onMatch)
    {
        this.onMatch = onMatch;
    }

    @JsonProperty
    public FilterReply getOnMismatch()
    {
        return this.onMismatch;
    }

    @JsonProperty
    public void setOnMismatch(FilterReply onMismatch)
    {
        this.onMismatch = onMismatch;
    }
}
