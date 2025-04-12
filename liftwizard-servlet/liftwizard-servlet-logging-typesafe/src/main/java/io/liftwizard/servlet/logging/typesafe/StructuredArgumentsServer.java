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

public class StructuredArgumentsServer {

    private final String scheme;
    private final String name;
    private final int port;

    public StructuredArgumentsServer(String scheme, String name, int port) {
        this.scheme = Objects.requireNonNull(scheme);
        this.name = Objects.requireNonNull(name);
        this.port = port;
    }

    @JsonProperty
    public String getScheme() {
        return this.scheme;
    }

    @JsonProperty
    public String getName() {
        return this.name;
    }

    @JsonProperty
    public int getPort() {
        return this.port;
    }
}
