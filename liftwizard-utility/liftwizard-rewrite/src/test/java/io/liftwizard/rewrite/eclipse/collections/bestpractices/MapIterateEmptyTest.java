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

class MapIterateEmptyTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new MapIterateEmptyRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Map;
					import org.eclipse.collections.impl.utility.MapIterate;

					class Test {
					    boolean testIsEmpty(Map<String, Integer> map) {
					        return map == null || map.isEmpty();
					    }

					    boolean testNotEmpty(Map<String, Integer> map) {
					        return map != null && !map.isEmpty();
					    }

					    boolean testNegatedMapIterateIsEmpty(Map<String, Integer> map) {
					        return !MapIterate.isEmpty(map);
					    }

					    boolean testNegatedMapIterateNotEmpty(Map<String, Integer> map) {
					        return !MapIterate.notEmpty(map);
					    }

					    void testMultiple(Map<String, Integer> map1, Map<String, Object> map2) {
					        if (map1 == null || map1.isEmpty()) {
					        }

					        if (map2 != null && !map2.isEmpty()) {
					        }

					        if (!MapIterate.isEmpty(map1)) {
					        }

					        if (!MapIterate.notEmpty(map2)) {
					        }
					    }
					}
					""",
					"""
					import java.util.Map;
					import org.eclipse.collections.impl.utility.MapIterate;

					class Test {
					    boolean testIsEmpty(Map<String, Integer> map) {
					        return MapIterate.isEmpty(map);
					    }

					    boolean testNotEmpty(Map<String, Integer> map) {
					        return MapIterate.notEmpty(map);
					    }

					    boolean testNegatedMapIterateIsEmpty(Map<String, Integer> map) {
					        return MapIterate.notEmpty(map);
					    }

					    boolean testNegatedMapIterateNotEmpty(Map<String, Integer> map) {
					        return MapIterate.isEmpty(map);
					    }

					    void testMultiple(Map<String, Integer> map1, Map<String, Object> map2) {
					        if (MapIterate.isEmpty(map1)) {
					        }

					        if (MapIterate.notEmpty(map2)) {
					        }

					        if (MapIterate.notEmpty(map1)) {
					        }

					        if (MapIterate.isEmpty(map2)) {
					        }
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
					import java.util.Map;

					class Test {
					    void test(Map<String, Integer> map) {
					        boolean simpleNullCheck = map == null;
					        boolean simpleIsEmptyCheck = map.isEmpty();
					        boolean wrongOperator = map != null || !map.isEmpty();
					    }
					}
					"""
				)
			);
	}
}
