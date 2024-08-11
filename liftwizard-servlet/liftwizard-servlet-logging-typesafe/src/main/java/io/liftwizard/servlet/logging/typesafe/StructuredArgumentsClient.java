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

public class StructuredArgumentsClient
{
    private final String address;
    private final String host;
    private final int port;

    public StructuredArgumentsClient(String address, String host, int port)
    {
        this.address = Objects.requireNonNull(address);
        this.host = Objects.requireNonNull(host);
        this.port = port;
    }

    @JsonProperty
    public String getAddress()
    {
        return this.address;
    }

    @JsonProperty
    public String getHost()
    {
        return this.host;
    }

    @JsonProperty
    public int getPort()
    {
        return this.port;
    }
}
