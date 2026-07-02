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

import com.codahale.metrics.Counter;
import com.timgroup.statsd.StatsDClient;

/**
 * A {@link Counter} that mirrors every {@code inc}/{@code dec} call to a {@link StatsDClient}
 * as a count delta. The in-memory state stays accurate so polling reporters still see the
 * cumulative value.
 */
public class PushingCounter extends Counter {

	private final String name;
	private final String[] tags;
	private final Supplier<StatsDClient> statsdSupplier;

	public PushingCounter(String name, String[] tags, Supplier<StatsDClient> statsdSupplier) {
		this.name = Objects.requireNonNull(name);
		this.tags = Objects.requireNonNull(tags);
		this.statsdSupplier = Objects.requireNonNull(statsdSupplier);
	}

	@Override
	public void inc(long n) {
		super.inc(n);
		this.statsdSupplier.get().count(this.name, n, this.tags);
	}

	@Override
	public void dec(long n) {
		super.dec(n);
		this.statsdSupplier.get().count(this.name, -n, this.tags);
	}
}
