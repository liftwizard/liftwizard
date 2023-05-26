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

package io.liftwizard.servlet.logging.feature;

import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * Builder class for logging feature configuration.
 */
public class LoggingConfig
{
    private final boolean               logRequests;
    private final boolean               logRequestBodies;
    private final boolean               logResponses;
    private final boolean               logResponseBodies;
    private final boolean               logRequestHeaderNames;
    private final boolean               logExcludedRequestHeaderNames;
    private final boolean               logResponseHeaderNames;
    private final boolean               logExcludedResponseHeaderNames;
    private final ImmutableList<String> includedRequestHeaders;
    private final ImmutableList<String> includedResponseHeaders;
    private final int                   maxEntitySize;

    public LoggingConfig(
            boolean logRequests,
            boolean logRequestBodies,
            boolean logResponses,
            boolean logResponseBodies,
            boolean logRequestHeaderNames,
            boolean logExcludedRequestHeaderNames,
            boolean logResponseHeaderNames,
            boolean logExcludedResponseHeaderNames,
            ImmutableList<String> includedRequestHeaders,
            ImmutableList<String> includedResponseHeaders,
            int maxEntitySize)
    {
        this.logRequests                    = logRequests;
        this.logRequestBodies               = logRequestBodies;
        this.logResponses                   = logResponses;
        this.logResponseBodies              = logResponseBodies;
        this.logRequestHeaderNames          = logRequestHeaderNames;
        this.logExcludedRequestHeaderNames  = logExcludedRequestHeaderNames;
        this.logResponseHeaderNames         = logResponseHeaderNames;
        this.logExcludedResponseHeaderNames = logExcludedResponseHeaderNames;
        this.includedRequestHeaders         = Objects.requireNonNull(includedRequestHeaders);
        this.includedResponseHeaders        = Objects.requireNonNull(includedResponseHeaders);
        this.maxEntitySize                  = maxEntitySize;
    }

    public boolean isLogRequests()
    {
        return this.logRequests;
    }

    public boolean isLogRequestBodies()
    {
        return this.logRequestBodies;
    }

    public boolean isLogResponses()
    {
        return this.logResponses;
    }

    public boolean isLogResponseBodies()
    {
        return this.logResponseBodies;
    }

    public boolean isLogRequestHeaderNames()
    {
        return this.logRequestHeaderNames;
    }

    public boolean isLogExcludedRequestHeaderNames()
    {
        return this.logExcludedRequestHeaderNames;
    }

    public boolean isLogResponseHeaderNames()
    {
        return this.logResponseHeaderNames;
    }

    public boolean isLogExcludedResponseHeaderNames()
    {
        return this.logExcludedResponseHeaderNames;
    }

    public ImmutableList<String> getIncludedRequestHeaders()
    {
        return this.includedRequestHeaders;
    }

    public ImmutableList<String> getIncludedResponseHeaders()
    {
        return this.includedResponseHeaders;
    }

    public int getMaxEntitySize()
    {
        return this.maxEntitySize;
    }
}
