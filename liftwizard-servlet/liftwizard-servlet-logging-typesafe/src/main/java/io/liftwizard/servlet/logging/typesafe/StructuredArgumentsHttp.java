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

import java.util.LinkedHashMap;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.map.mutable.MapAdapter;

public class StructuredArgumentsHttp
{
    private MutableMap<String, String> headers;

    private MutableList<String> excludedHeaders;

    public MapIterable<String, String> getHeaders()
    {
        if (this.headers == null)
        {
            return null;
        }
        return this.headers.asUnmodifiable();
    }

    public void addHeader(String key, String value)
    {
        if (this.headers == null)
        {
            this.headers = MapAdapter.adapt(new LinkedHashMap<>());
        }

        String duplicateValue = this.headers.put(key, value);
        if (duplicateValue != null)
        {
            throw new AssertionError(duplicateValue);
        }
    }

    public void initializeExcludedHeaders()
    {
        if (this.excludedHeaders != null)
        {
            throw new AssertionError(this.excludedHeaders);
        }

        this.excludedHeaders = Lists.mutable.empty();
    }

    public ListIterable<String> getExcludedHeaders()
    {
        if (this.excludedHeaders == null)
        {
            return null;
        }
        return this.excludedHeaders.asUnmodifiable();
    }

    public void addExcludedHeader(String header)
    {
        this.excludedHeaders.add(header);
    }
}
