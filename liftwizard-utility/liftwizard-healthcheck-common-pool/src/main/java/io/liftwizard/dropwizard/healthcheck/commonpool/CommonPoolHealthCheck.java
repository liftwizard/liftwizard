/*
 * Copyright 2024 Craig Motlin
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

package io.liftwizard.dropwizard.healthcheck.commonpool;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.codahale.metrics.health.HealthCheck;
import io.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.fixed.ArrayAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonPoolHealthCheck
        extends HealthCheck
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonPoolHealthCheck.class);

    private static final int MAX_STACK_TRACE_DEPTH = 100;

    @Nonnull
    private final ThreadMXBean           threads;
    @Nonnull
    private final String                 threadNamePrefix;
    @Nonnull
    private final Set<State>             threadStates;
    @Nonnull
    private final ImmutableList<Pattern> alwaysAllowedPatterns;
    @Nonnull
    private final ImmutableList<Pattern> bannedPatterns;

    public CommonPoolHealthCheck()
    {
        this(
                "ForkJoinPool.commonPool-worker-",
                Lists.immutable.with(State.RUNNABLE),
                Lists.immutable.empty(),
                Lists.immutable.empty());
    }

    /**
     * @param threadNamePrefix      Threads with a name matching this prefix will be checked further. Default is {@code "ForkJoinPool.commonPool-worker-"}
     * @param threadStates          Unhealthy states. Default is {@code Set.of(State.RUNNABLE)}
     * @param alwaysAllowedPatterns Patterns that match traces that are allowed to put background tasks into the common pool. For example: com\.github\.benmane\.caffeine\.cache\..*
     * @param bannedPatterns        Patterns that match traces that are not allowed to put background tasks into the common pool. Can be used to limit the check to your own packages. For example: com\.example\.mycompany\..*  If alwaysAllowedPatterns and bannedPatterns both match, the stack trace is allowed/ignored.
     */
    public CommonPoolHealthCheck(
            @Nonnull String threadNamePrefix,
            @Nonnull ImmutableList<State> threadStates,
            @Nonnull ImmutableList<Pattern> alwaysAllowedPatterns,
            @Nonnull ImmutableList<Pattern> bannedPatterns)
    {
        this(
                ManagementFactory.getThreadMXBean(),
                threadNamePrefix,
                threadStates,
                alwaysAllowedPatterns,
                bannedPatterns);
    }

    public CommonPoolHealthCheck(
            @Nonnull ThreadMXBean threads,
            @Nonnull String threadNamePrefix,
            @Nonnull ImmutableList<State> threadStates,
            @Nonnull ImmutableList<Pattern> alwaysAllowedPatterns,
            @Nonnull ImmutableList<Pattern> bannedPatterns)
    {
        this.threads               = Objects.requireNonNull(threads);
        this.threadNamePrefix      = Objects.requireNonNull(threadNamePrefix);
        this.threadStates          = new LinkedHashSet<>(threadStates.castToList());
        this.alwaysAllowedPatterns = Objects.requireNonNull(alwaysAllowedPatterns);
        this.bannedPatterns        = Objects.requireNonNull(bannedPatterns);
    }

    @Nonnull
    @Override
    protected Result check()
    {
        ThreadInfo[] threadInfos = this.threads.getThreadInfo(this.threads.getAllThreadIds(), MAX_STACK_TRACE_DEPTH);
        List<ThreadInfo> badThreadInfos = Stream.of(threadInfos)
                .filter(threadInfo -> threadInfo.getThreadName().startsWith(this.threadNamePrefix))
                .filter(threadInfo -> this.threadStates.contains(threadInfo.getThreadState()))
                .filter(threadInfo -> this.bannedPatterns.isEmpty()
                        || this.bannedPatterns.anySatisfy(bannedPattern -> ArrayAdapter
                        .adapt(threadInfo.getStackTrace())
                        .anySatisfyWith(this::traceMatchesPattern, bannedPattern)))
                .filter(threadInfo -> this.alwaysAllowedPatterns.noneSatisfy(alwaysAllowedPattern -> ArrayAdapter
                        .adapt(threadInfo.getStackTrace())
                        .anySatisfyWith(this::traceMatchesPattern, alwaysAllowedPattern)))
                .toList();

        if (badThreadInfos.isEmpty())
        {
            return Result.healthy();
        }

        List<String> badThreadInfoStrings = new ArrayList<>();

        for (ThreadInfo badThreadInfo : badThreadInfos)
        {
            State  threadState      = badThreadInfo.getThreadState();
            String threadName       = badThreadInfo.getThreadName();
            String stackTraceString = this.getStackTraceString(badThreadInfo.getStackTrace());

            try (MultiMDCCloseable mdc = new MultiMDCCloseable())
            {
                mdc.put("threadState", threadState.name());
                mdc.put("threadName", threadName);
                mdc.put("stackTrace", stackTraceString);

                String message = "Found thread '%s' in state '%s'%n%s".formatted(
                        threadName,
                        threadState,
                        stackTraceString);
                badThreadInfoStrings.add(message);
                LOGGER.warn(message);
            }
        }

        String message = String.join("\n\n", badThreadInfoStrings);

        return Result.unhealthy(message);
    }

    private boolean traceMatchesPattern(
            StackTraceElement stackStrace,
            Pattern bannedPattern)
    {
        return bannedPattern
                .matcher(stackStrace.getClassName() + "." + stackStrace.getMethodName())
                .matches();
    }

    @Nonnull
    private String getStackTraceString(StackTraceElement[] stackTrace)
    {
        return Stream.of(stackTrace)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n\t at ", "", System.lineSeparator()));
    }
}
