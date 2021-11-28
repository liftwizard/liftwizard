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
    private final boolean               logExcludedHeaderNames;
    private final ImmutableList<String> includedHeaders;
    private final int                   maxEntitySize;

    public LoggingConfig(
            boolean logRequests,
            boolean logRequestBodies,
            boolean logResponses,
            boolean logResponseBodies,
            boolean logExcludedHeaderNames,
            ImmutableList<String> includedHeaders,
            int maxEntitySize)
    {
        this.logRequests            = logRequests;
        this.logRequestBodies       = logRequestBodies;
        this.logResponses           = logResponses;
        this.logResponseBodies      = logResponseBodies;
        this.logExcludedHeaderNames = logExcludedHeaderNames;
        this.includedHeaders        = Objects.requireNonNull(includedHeaders);
        this.maxEntitySize          = maxEntitySize;
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

    public boolean isLogExcludedHeaderNames()
    {
        return this.logExcludedHeaderNames;
    }

    public ImmutableList<String> getIncludedHeaders()
    {
        return this.includedHeaders;
    }

    public int getMaxEntitySize()
    {
        return this.maxEntitySize;
    }
}
