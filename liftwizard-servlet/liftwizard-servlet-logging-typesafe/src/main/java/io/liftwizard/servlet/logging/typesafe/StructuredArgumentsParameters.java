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
import org.eclipse.collections.api.map.MapIterable;

public class StructuredArgumentsParameters
{
    private final MapIterable<String, String> query;
    private final MapIterable<String, String> path;

    public StructuredArgumentsParameters(MapIterable<String, String> query, MapIterable<String, String> path)
    {
        this.query = Objects.requireNonNull(query);
        this.path = Objects.requireNonNull(path);
    }

    @JsonProperty
    public MapIterable<String, String> getQuery()
    {
        return this.query;
    }

    @JsonProperty
    public MapIterable<String, String> getPath()
    {
        return this.path;
    }
}
