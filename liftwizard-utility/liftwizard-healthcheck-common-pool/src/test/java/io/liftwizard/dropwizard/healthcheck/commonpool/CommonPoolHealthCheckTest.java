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
import java.util.regex.Pattern;

import com.codahale.metrics.health.HealthCheck.Result;
import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonPoolHealthCheckTest
{
    @RegisterExtension
    private final LogMarkerTestExtension logMarkerTestExtension = new LogMarkerTestExtension();

    @Test
    public void healthy()
    {
        Result result = new CommonPoolHealthCheck().check();
        assertTrue(result.isHealthy(), result.toString());
    }

    @Test
    public void unhealthy()
    {
        CommonPoolHealthCheck commonPoolHealthCheck = new CommonPoolHealthCheck(
                "main",
                Lists.immutable.with(State.RUNNABLE),
                Lists.immutable.empty(),
                Lists.immutable.empty());
        Result result = commonPoolHealthCheck.check();
        assertFalse(result.isHealthy());
        assertThat(result.getMessage(), containsString("Found thread 'main' in state 'RUNNABLE'"));
    }

    @Test
    public void allow()
    {
        CommonPoolHealthCheck commonPoolHealthCheck = new CommonPoolHealthCheck(
                "main",
                Lists.immutable.with(State.RUNNABLE),
                pattern("io.liftwizard.dropwizard.healthcheck.commonpool.CommonPoolHealthCheck.check"),
                Lists.immutable.empty());
        Result result = commonPoolHealthCheck.check();
        assertTrue(result.isHealthy(), result.toString());
    }

    @Test
    public void ban()
    {
        CommonPoolHealthCheck commonPoolHealthCheck = new CommonPoolHealthCheck(
                "main",
                Lists.immutable.with(State.RUNNABLE),
                Lists.immutable.empty(),
                pattern("io.liftwizard.dropwizard.healthcheck.commonpool.CommonPoolHealthCheck.check"));
        Result result = commonPoolHealthCheck.check();
        assertFalse(result.isHealthy());
        assertThat(result.getMessage(), containsString("Found thread 'main' in state 'RUNNABLE'"));
    }

    @Test
    public void both()
    {
        CommonPoolHealthCheck commonPoolHealthCheck = new CommonPoolHealthCheck(
                "main",
                Lists.immutable.with(State.RUNNABLE),
                pattern("io.liftwizard.dropwizard.healthcheck.commonpool.CommonPoolHealthCheck.check"),
                pattern("io.liftwizard.dropwizard.healthcheck.commonpool.CommonPoolHealthCheck.allow"));
        Result result = commonPoolHealthCheck.check();
        assertTrue(result.isHealthy(), result.toString());
    }

    private static ImmutableList<Pattern> pattern(String string)
    {
        return Lists.immutable.with(Pattern.compile(string));
    }
}
