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

package io.liftwizard.dropwizard.bundle.clock;

import java.time.Clock;
import java.util.Objects;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ClockBinder extends AbstractBinder {

    private final Clock clock;

    public ClockBinder(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    protected void configure() {
        this.bind(this.clock).to(Clock.class);
    }
}
