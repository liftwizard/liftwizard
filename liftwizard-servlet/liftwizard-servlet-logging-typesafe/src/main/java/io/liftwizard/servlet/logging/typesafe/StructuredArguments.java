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

public class StructuredArguments
{
    private       String                      event;
    private final StructuredArgumentsRequest  request = new StructuredArgumentsRequest();
    private       StructuredArgumentsResponse response;

    @JsonProperty
    public String getEvent()
    {
        return this.event;
    }

    public void setEvent(String event)
    {
        if (Objects.equals(this.event, event))
        {
            throw new AssertionError(this.event);
        }
        this.event = event;
    }

    @JsonProperty
    public StructuredArgumentsRequest getRequest()
    {
        return this.request;
    }

    public void initializeResponse()
    {
        if (this.response != null)
        {
            throw new AssertionError(this.response);
        }
        this.response = new StructuredArgumentsResponse();
    }

    @JsonProperty
    public StructuredArgumentsResponse getResponse()
    {
        return this.response;
    }
}
