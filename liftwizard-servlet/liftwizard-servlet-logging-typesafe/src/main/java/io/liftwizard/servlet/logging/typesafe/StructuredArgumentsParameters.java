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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.map.mutable.MapAdapter;

public class StructuredArgumentsParameters
{
    private MutableMap<String, String> query;
    private MutableMap<String, String> path;

    public void addQueryParameter(String key, String value)
    {
        if (this.query == null)
        {
            this.query = MapAdapter.adapt(new LinkedHashMap<>());
        }

        String duplicateValue = this.query.put(key, value);
        if (duplicateValue != null)
        {
            throw new AssertionError(duplicateValue);
        }
    }

    @JsonProperty
    public MapIterable<String, String> getQuery()
    {
        if (this.query == null)
        {
            return null;
        }
        return this.query.asUnmodifiable();
    }

    public void addPathParameter(String key, String value)
    {
        if (this.path == null)
        {
            this.path = MapAdapter.adapt(new LinkedHashMap<>());
        }

        String duplicateValue = this.path.put(key, value);
        if (duplicateValue != null)
        {
            throw new AssertionError(duplicateValue);
        }
    }

    @JsonProperty
    public MapIterable<String, String> getPath()
    {
        if (this.path == null)
        {
            return null;
        }
        return this.path.asUnmodifiable();
    }
}
