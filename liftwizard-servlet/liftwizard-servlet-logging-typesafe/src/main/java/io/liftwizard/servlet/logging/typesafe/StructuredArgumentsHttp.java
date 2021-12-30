/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.servlet.logging.typesafe;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;

public class StructuredArgumentsHttp
{
    private MapIterable<String, String> headers;
    private ImmutableList<String>       excludedHeaders;

    @JsonProperty
    public MapIterable<String, String> getHeaders()
    {
        return this.headers;
    }

    public void setHeaders(MutableMap<String, String> headers)
    {
        if (this.headers != null)
        {
            throw new AssertionError(this.headers);
        }
        this.headers = headers.asUnmodifiable();
    }

    @JsonProperty
    public ImmutableList<String> getExcludedHeaders()
    {
        return this.excludedHeaders;
    }

    public void setExcludedHeaders(ImmutableList<String> excludedHeaders)
    {
        if (this.excludedHeaders != null)
        {
            throw new AssertionError(this.excludedHeaders);
        }
        this.excludedHeaders = Objects.requireNonNull(excludedHeaders);
    }
}
