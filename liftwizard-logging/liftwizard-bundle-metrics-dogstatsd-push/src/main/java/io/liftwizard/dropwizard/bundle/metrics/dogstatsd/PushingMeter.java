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

import com.codahale.metrics.Meter;
import com.timgroup.statsd.StatsDClient;
import io.liftwizard.logging.metrics.dogstatsd.TaggedMetricName;

public class PushingMeter extends Meter {

	private final TaggedMetricName tagged;
	private final Supplier<StatsDClient> statsdSupplier;

	public PushingMeter(String encodedName, Supplier<StatsDClient> statsdSupplier) {
		this.tagged = TaggedMetricName.decode(Objects.requireNonNull(encodedName));
		this.statsdSupplier = Objects.requireNonNull(statsdSupplier);
	}

	@Override
	public void mark(long n) {
		super.mark(n);
		this.statsdSupplier.get().count(this.tagged.getName(), n, this.tagged.getTags());
	}
}
