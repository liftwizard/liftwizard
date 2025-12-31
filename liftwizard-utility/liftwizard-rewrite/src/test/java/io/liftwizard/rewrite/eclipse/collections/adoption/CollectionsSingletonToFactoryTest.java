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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class CollectionsSingletonToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new CollectionsSingletonToFactoryRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Collections;
					import java.util.List;
					import java.util.Map;
					import java.util.Set;

					class Test {
					    private final List<String> fieldSingletonList = Collections.singletonList("element");
					    private final Set<String> fieldSingleton = Collections.singleton("element");
					    private final Map<String, String> fieldSingletonMap = Collections.singletonMap("key", "value");

					    void test() {
					        List<String> singletonList = Collections.singletonList("element");
					        Set<String> singleton = Collections.singleton("element");
					        Map<String, String> singletonMap = Collections.singletonMap("key", "value");
					    }

					    void testWithExplicitGenerics(Object element, Object key, Object value) {
					        List<String> singletonList = Collections.<String>singletonList((String) element);
					        Set<String> singleton = Collections.<String>singleton((String) element);
					        Map<String, String> singletonMap = Collections.<String, String>singletonMap((String) key, (String) value);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.factory.Maps;
					import org.eclipse.collections.api.factory.Sets;

					import java.util.List;
					import java.util.Map;
					import java.util.Set;

					class Test {
					    private final List<String> fieldSingletonList = Lists.fixedSize.with("element");
					    private final Set<String> fieldSingleton = Sets.fixedSize.with("element");
					    private final Map<String, String> fieldSingletonMap = Maps.fixedSize.with("key", "value");

					    void test() {
					        List<String> singletonList = Lists.fixedSize.with("element");
					        Set<String> singleton = Sets.fixedSize.with("element");
					        Map<String, String> singletonMap = Maps.fixedSize.with("key", "value");
					    }

					    void testWithExplicitGenerics(Object element, Object key, Object value) {
					        List<String> singletonList = Lists.fixedSize.with((String) element);
					        Set<String> singleton = Sets.fixedSize.with((String) element);
					        Map<String, String> singletonMap = Maps.fixedSize.with((String) key, (String) value);
					    }
					}
					"""
				)
			);
	}
}
