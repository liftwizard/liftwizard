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

import java.util.ArrayList;
import java.util.List;

import com.timgroup.statsd.Event;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.ServiceCheck;

/**
 * Records every StatsD call into a list of typed events for inspection in tests. Inherits
 * everything else from {@link NoOpStatsDClient} so unused methods are silent no-ops.
 */
class RecordingStatsDClient extends NoOpStatsDClient {

	enum Kind {
		COUNT,
		GAUGE,
		HISTOGRAM,
		EXECUTION_TIME,
	}

	static final class Call {

		final Kind kind;
		final String name;
		final double value;
		final String[] tags;

		Call(Kind kind, String name, double value, String[] tags) {
			this.kind = kind;
			this.name = name;
			this.value = value;
			this.tags = tags;
		}
	}

	private final List<Call> calls = new ArrayList<>();

	List<Call> getCalls() {
		return this.calls;
	}

	@Override
	public void count(String name, long delta, String... tags) {
		this.calls.add(new Call(Kind.COUNT, name, delta, tags));
	}

	@Override
	public void count(String name, double delta, String... tags) {
		this.calls.add(new Call(Kind.COUNT, name, delta, tags));
	}

	@Override
	public void gauge(String name, double value, String... tags) {
		this.calls.add(new Call(Kind.GAUGE, name, value, tags));
	}

	@Override
	public void gauge(String name, long value, String... tags) {
		this.calls.add(new Call(Kind.GAUGE, name, value, tags));
	}

	@Override
	public void histogram(String name, double value, String... tags) {
		this.calls.add(new Call(Kind.HISTOGRAM, name, value, tags));
	}

	@Override
	public void histogram(String name, long value, String... tags) {
		this.calls.add(new Call(Kind.HISTOGRAM, name, value, tags));
	}

	@Override
	public void recordExecutionTime(String name, long timeInMs, String... tags) {
		this.calls.add(new Call(Kind.EXECUTION_TIME, name, timeInMs, tags));
	}

	@Override
	public void recordEvent(Event event, String... tags) {}

	@Override
	public void recordServiceCheckRun(ServiceCheck sc) {}
}
