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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaggedMetricRegistryTest {

	@Test
	void registryReturnsEmptyTagsByDefault() {
		var registry = new TaggedMetricRegistry();
		assertThat(registry.getTags("absent")).isEmpty();
	}

	@Test
	void counterRegistrationWithTagsStoresThem() {
		var registry = new TaggedMetricRegistry();
		registry.counter("requests", "endpoint:/users", "method:GET");
		assertThat(registry.getTags("requests")).containsExactly("endpoint:/users", "method:GET");
	}

	@Test
	void counterRegistrationWithoutTagsLeavesEmpty() {
		var registry = new TaggedMetricRegistry();
		registry.counter("noTags");
		assertThat(registry.getTags("noTags")).isEmpty();
	}

	@Test
	void firstRegistrationWinsForTags() {
		var registry = new TaggedMetricRegistry();
		registry.counter("requests", "endpoint:/users");
		registry.counter("requests", "endpoint:/other");
		assertThat(registry.getTags("requests")).containsExactly("endpoint:/users");
	}

	@Test
	void registerWithTagsStoresThem() {
		var registry = new TaggedMetricRegistry();
		Gauge<Integer> gauge = () -> 42;
		registry.register("temperature", gauge, "location:office");
		assertThat(registry.getTags("temperature")).containsExactly("location:office");
		assertThat(registry.getGauges()).containsEntry("temperature", gauge);
	}

	@Test
	void tagsArrayIsDefensivelyCloned() {
		var registry = new TaggedMetricRegistry();
		String[] tags = { "a:1", "b:2" };
		registry.counter("foo", tags);
		tags[0] = "mutated";
		assertThat(registry.getTags("foo")).containsExactly("a:1", "b:2");
	}

	@Test
	void plainCounterFactoryReturnsCounter() {
		var registry = new TaggedMetricRegistry();
		Counter counter = registry.counter("plain");
		assertThat(counter).isInstanceOf(Counter.class);
	}
}
