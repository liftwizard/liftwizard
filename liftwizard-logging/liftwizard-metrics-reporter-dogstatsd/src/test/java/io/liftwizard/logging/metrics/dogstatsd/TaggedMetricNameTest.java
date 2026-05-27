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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaggedMetricNameTest {

	@Test
	void encodeWithoutTagsReturnsBareName() {
		assertThat(TaggedMetricName.encode("requests")).isEqualTo("requests");
	}

	@Test
	void encodeWithTagsAppendsBracketSection() {
		String encoded = TaggedMetricName.encode("requests", "endpoint:/users", "method:GET");
		assertThat(encoded).isEqualTo("requests[endpoint:/users,method:GET]");
	}

	@Test
	void decodeBareNameReturnsEmptyTags() {
		TaggedMetricName tmn = TaggedMetricName.decode("requests");
		assertThat(tmn.getName()).isEqualTo("requests");
		assertThat(tmn.getTags()).isEmpty();
	}

	@Test
	void decodeWithTags() {
		TaggedMetricName tmn = TaggedMetricName.decode("requests[endpoint:/users,method:GET]");
		assertThat(tmn.getName()).isEqualTo("requests");
		assertThat(tmn.getTags()).containsExactly("endpoint:/users", "method:GET");
	}

	@Test
	void decodeWithEmptyBracketSection() {
		TaggedMetricName tmn = TaggedMetricName.decode("requests[]");
		assertThat(tmn.getName()).isEqualTo("requests");
		assertThat(tmn.getTags()).isEmpty();
	}

	@Test
	void encodeDecodeRoundtripsCleanly() {
		String original = TaggedMetricName.encode("cache.hit_rate", "name:user_cache");
		TaggedMetricName decoded = TaggedMetricName.decode(original);
		assertThat(decoded.getName()).isEqualTo("cache.hit_rate");
		assertThat(decoded.getTags()).containsExactly("name:user_cache");
		assertThat(decoded.toString()).isEqualTo(original);
	}
}
