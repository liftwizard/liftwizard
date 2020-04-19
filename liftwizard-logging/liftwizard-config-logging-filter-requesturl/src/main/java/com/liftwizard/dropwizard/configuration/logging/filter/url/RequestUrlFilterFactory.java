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

package com.liftwizard.dropwizard.configuration.logging.filter.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.service.AutoService;
import com.liftwizard.logging.logback.filter.requesturl.RequestUrlFilter;
import io.dropwizard.logging.filter.FilterFactory;
import org.hibernate.validator.constraints.NotEmpty;

@JsonTypeName("url")
@AutoService(FilterFactory.class)
public class RequestUrlFilterFactory implements FilterFactory<IAccessEvent>
{
    @NotEmpty
    private @Valid @NotNull List<String> bannedUrls = new ArrayList<>();

    @Override
    public Filter<IAccessEvent> build()
    {
        return new RequestUrlFilter(this.bannedUrls);
    }

    @JsonProperty
    public List<String> getBannedUrls()
    {
        return this.bannedUrls;
    }

    @JsonProperty
    public void setBannedUrls(List<String> bannedUrls)
    {
        this.bannedUrls = Objects.requireNonNull(bannedUrls);
    }
}
