/*
 * Copyright 2025 Craig Motlin
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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ECMapGetOrDefaultToGetIfAbsentValueTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECMapGetOrDefaultToGetIfAbsentValue());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.map.MutableMap;

					class Test {
					    void test(MutableMap<String, String> map, MutableMap<String, Integer> intMap, String key) {
					        String result1 = map.getOrDefault("key", "default");
					        String result2 = map.getOrDefault(key, "fallback");
					        Integer result3 = intMap.getOrDefault("key", 0);
					        String result4 = map.getOrDefault("key", "default");
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.map.MutableMap;

					class Test {
					    void test(MutableMap<String, String> map, MutableMap<String, Integer> intMap, String key) {
					        String result1 = map.getIfAbsentValue("key", "default");
					        String result2 = map.getIfAbsentValue(key, "fallback");
					        Integer result3 = intMap.getIfAbsentValue("key", 0);
					        String result4 = map.getIfAbsentValue("key", "default");
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.HashMap;
					import java.util.Map;

					class Test {
					    void test(Map<String, String> map, HashMap<String, String> hashMap) {
					        String result1 = map.getOrDefault("key", "default");
					        String result2 = hashMap.getOrDefault("key", "default");
					    }
					}
					"""
				)
			);
	}
}
