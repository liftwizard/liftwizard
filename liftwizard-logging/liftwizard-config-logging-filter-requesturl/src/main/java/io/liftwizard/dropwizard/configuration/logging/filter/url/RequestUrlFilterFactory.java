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

package io.liftwizard.dropwizard.configuration.logging.filter.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ch.qos.logback.access.net.URLEvaluator;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import io.dropwizard.logging.filter.FilterFactory;

@JsonTypeName("url")
@AutoService(FilterFactory.class)
public class RequestUrlFilterFactory
        implements FilterFactory<IAccessEvent>
{
    @NotEmpty
    private @Valid @NotNull List<String> urls = new ArrayList<>();

    private @Valid @NotNull FilterReply onMatch = FilterReply.DENY;
    private @Valid @NotNull FilterReply onMismatch = FilterReply.NEUTRAL;

    @Override
    public Filter<IAccessEvent> build()
    {
        var evaluator = new URLEvaluator();
        this.urls.forEach(evaluator::addURL);
        evaluator.start();

        var filter = new EvaluatorFilter<IAccessEvent>();
        filter.setEvaluator(evaluator);
        filter.setOnMatch(this.onMatch);
        filter.setOnMismatch(this.onMismatch);
        filter.start();

        return filter;
    }

    @JsonProperty
    public List<String> getUrls()
    {
        return this.urls;
    }

    @JsonProperty
    public void setUrls(List<String> urls)
    {
        this.urls = Objects.requireNonNull(urls);
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
