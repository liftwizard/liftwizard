/*
 * Copyright 2026 Craig Motlin
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

package io.liftwizard.dropwizard.bundle.metrics.dogstatsd;

import java.util.Objects;
import java.util.function.Supplier;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.timgroup.statsd.StatsDClient;

public class PushingHistogram extends Histogram {

	private final String name;
	private final String[] tags;
	private final Supplier<StatsDClient> statsdSupplier;

	public PushingHistogram(String name, String[] tags, Supplier<StatsDClient> statsdSupplier) {
		super(new ExponentiallyDecayingReservoir());
		this.name = Objects.requireNonNull(name);
		this.tags = Objects.requireNonNull(tags);
		this.statsdSupplier = Objects.requireNonNull(statsdSupplier);
	}

	@Override
	public void update(long value) {
		super.update(value);
		this.statsdSupplier.get().histogram(this.name, value, this.tags);
	}
}
