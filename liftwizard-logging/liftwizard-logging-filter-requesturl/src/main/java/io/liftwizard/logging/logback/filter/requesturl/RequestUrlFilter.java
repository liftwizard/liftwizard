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

package io.liftwizard.logging.logback.filter.requesturl;

import java.util.Objects;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class RequestUrlFilter extends Filter<IAccessEvent>
{
    private final ImmutableList<String> bannedUrls;

    public RequestUrlFilter(Iterable<String> bannedUrls)
    {
        Objects.requireNonNull(bannedUrls);
        this.bannedUrls = Lists.immutable.withAll(bannedUrls);
    }

    @Override
    public FilterReply decide(IAccessEvent event)
    {
        String requestURL = event.getRequestURL();

        return this.bannedUrls.anySatisfy(requestURL::contains)
                ? FilterReply.DENY
                : FilterReply.NEUTRAL;
    }
}
