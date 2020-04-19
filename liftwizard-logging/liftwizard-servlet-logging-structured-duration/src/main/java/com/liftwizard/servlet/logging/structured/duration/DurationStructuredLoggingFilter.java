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

package com.liftwizard.servlet.logging.structured.duration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

// Priority must be less than the priority of StructuredArgumentLoggingFilter
@Provider
@PreMatching
@Priority(Priorities.USER - 30)
public class DurationStructuredLoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter
{
    public static final String STRUCTURED_ARGUMENTS_ATTRIBUTE_NAME = "structuredArguments";
    public static final String START_TIME_KEY                      = "liftwizard.time.startTime";

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d[HMS])(?!$)");

    private final Clock  clock;
    private final String structuredArgumentsAttributeName;
    private final String startTimeKey;

    public DurationStructuredLoggingFilter(@Nonnull @Context Clock clock)
    {
        this(clock, STRUCTURED_ARGUMENTS_ATTRIBUTE_NAME, START_TIME_KEY);
    }

    public DurationStructuredLoggingFilter(Clock clock, String structuredArgumentsAttributeName, String startTimeKey)
    {
        this.clock                            = Objects.requireNonNull(clock);
        this.structuredArgumentsAttributeName = Objects.requireNonNull(structuredArgumentsAttributeName);
        this.startTimeKey                     = Objects.requireNonNull(startTimeKey);
    }

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        Instant startTime = this.clock.instant();

        Object structuredArguments = requestContext.getProperty(this.structuredArgumentsAttributeName);
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        structuredArgumentsMap.put(this.startTimeKey, startTime);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
    {
        Object structuredArguments = requestContext.getProperty(this.structuredArgumentsAttributeName);
        Objects.requireNonNull(structuredArguments);
        Map<String, Object> structuredArgumentsMap = (Map<String, Object>) structuredArguments;

        Instant  startTime      = (Instant) structuredArgumentsMap.get(this.startTimeKey);
        Instant  endTime        = this.clock.instant();
        Duration duration       = Duration.between(startTime, endTime);
        String   prettyDuration = DurationStructuredLoggingFilter.prettyPrintDuration(duration);

        structuredArgumentsMap.put("liftwizard.time.endTime", endTime);
        structuredArgumentsMap.put("liftwizard.time.duration.pretty", prettyDuration);
        structuredArgumentsMap.put("liftwizard.time.duration.ms", duration.toMillis());
        structuredArgumentsMap.put("liftwizard.time.duration.ns", duration.toNanos());
    }

    private static String prettyPrintDuration(Duration duration)
    {
        String  trimmedString = duration.toString().substring(2);
        Matcher matcher       = DURATION_PATTERN.matcher(trimmedString);
        return matcher.replaceAll("$1 ").toLowerCase();
    }
}
