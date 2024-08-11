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

package io.liftwizard.clock.incrementing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.Objects;

public final class IncrementingClock
        extends Clock
{
    private Instant instant;

    private final ZoneId zoneId;
    private final TemporalAmount incrementAmount;

    public IncrementingClock(Instant startInstant, ZoneId zoneId, TemporalAmount incrementAmount)
    {
        this.instant = Objects.requireNonNull(startInstant);
        this.incrementAmount = Objects.requireNonNull(incrementAmount);
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    @Override
    public ZoneId getZone()
    {
        return this.zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone)
    {
        if (zone.equals(this.zoneId))
        {
            return this;
        }
        return new IncrementingClock(this.instant, zone, this.incrementAmount);
    }

    @Override
    public long millis()
    {
        Instant result = this.instant;
        this.instant = this.instant.plus(this.incrementAmount);
        return result.toEpochMilli();
    }

    @Override
    public Instant instant()
    {
        Instant result = this.instant;
        this.instant = this.instant.plus(this.incrementAmount);
        return result;
    }
}
