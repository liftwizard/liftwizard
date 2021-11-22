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

package io.liftwizard.dropwizard.healthcheck.commonpool;

import java.lang.Thread.State;
import java.util.regex.Pattern;

import com.codahale.metrics.health.HealthCheck.Result;
import io.liftwizard.junit.rule.log.marker.LogMarkerTestRule;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CommonPoolHealthCheckTest
{
    @Rule
    public final TestRule logMarkerTestRule = new LogMarkerTestRule();

    @Test
    public void healthy()
    {
        Result result = new CommonPoolHealthCheck().check();
        assertEquals(Result.healthy(), result);
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
        assertEquals(Result.healthy(), result);
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
        assertEquals(Result.healthy(), result);
    }

    private static ImmutableList<Pattern> pattern(String string)
    {
        return Lists.immutable.with(Pattern.compile(string));
    }
}
