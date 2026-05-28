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

package io.liftwizard.logging.metrics.dogstatsd;

import java.util.Arrays;
import java.util.Objects;

/**
 * Encodes Datadog tags into a Dropwizard metric name and decodes them back out.
 *
 * <p>Dropwizard's {@link com.codahale.metrics.MetricRegistry} is tagless, so tags are smuggled
 * through the metric name using the format {@code name[tag1:value1,tag2:value2]}. The encoded
 * form is what gets registered with the {@code MetricRegistry}; the reporter decodes it before
 * forwarding to the StatsD agent.
 */
public final class TaggedMetricName {

	private final String name;
	private final String[] tags;

	public TaggedMetricName(String name, String[] tags) {
		this.name = Objects.requireNonNull(name);
		this.tags = Objects.requireNonNull(tags);
	}

	public String getName() {
		return this.name;
	}

	public String[] getTags() {
		return this.tags;
	}

	public static String encode(String name, String... tags) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(tags);
		if (tags.length == 0) {
			return name;
		}
		return name + "[" + String.join(",", tags) + "]";
	}

	public static TaggedMetricName decode(String encoded) {
		Objects.requireNonNull(encoded);
		int openBracket = encoded.indexOf('[');
		if (openBracket < 0 || !encoded.endsWith("]")) {
			return new TaggedMetricName(encoded, new String[0]);
		}
		String bareName = encoded.substring(0, openBracket);
		String tagSection = encoded.substring(openBracket + 1, encoded.length() - 1);
		if (tagSection.isEmpty()) {
			return new TaggedMetricName(bareName, new String[0]);
		}
		String[] tags = tagSection.split(",");
		return new TaggedMetricName(bareName, tags);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TaggedMetricName)) {
			return false;
		}
		var that = (TaggedMetricName) other;
		return this.name.equals(that.name) && Arrays.equals(this.tags, that.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name) * 31 + Arrays.hashCode(this.tags);
	}

	@Override
	public String toString() {
		return encode(this.name, this.tags);
	}
}
