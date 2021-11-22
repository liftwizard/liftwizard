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

package io.liftwizard.dropwizard.config.healthcheck.commonpool;

import java.lang.Thread.State;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class CommonPoolHealthCheckFactory
{
    private boolean enabled = true;

    private @NotNull String                 threadNamePrefix      = "ForkJoinPool.commonPool-worker-";
    private @NotNull ImmutableList<State>   threadStates          = Lists.immutable.with(State.RUNNABLE);
    private @NotNull ImmutableList<Pattern> alwaysAllowedPatterns = Lists.immutable.empty();
    private @NotNull ImmutableList<Pattern> bannedPatterns        = Lists.immutable.empty();

    @JsonProperty
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @JsonProperty
    public String getThreadNamePrefix()
    {
        return this.threadNamePrefix;
    }

    @JsonProperty
    public void setThreadNamePrefix(String threadNamePrefix)
    {
        this.threadNamePrefix = threadNamePrefix;
    }

    @JsonProperty
    public ImmutableList<State> getThreadStates()
    {
        return this.threadStates;
    }

    @JsonProperty
    public void setThreadStates(ImmutableList<State> threadStates)
    {
        this.threadStates = threadStates;
    }

    @JsonProperty
    public ImmutableList<Pattern> getAlwaysAllowedPatterns()
    {
        return this.alwaysAllowedPatterns;
    }

    @JsonProperty
    public void setAlwaysAllowedPatterns(ImmutableList<Pattern> alwaysAllowedPatterns)
    {
        this.alwaysAllowedPatterns = alwaysAllowedPatterns;
    }

    @JsonProperty
    public ImmutableList<Pattern> getBannedPatterns()
    {
        return this.bannedPatterns;
    }

    @JsonProperty
    public void setBannedPatterns(ImmutableList<Pattern> bannedPatterns)
    {
        this.bannedPatterns = bannedPatterns;
    }
}
