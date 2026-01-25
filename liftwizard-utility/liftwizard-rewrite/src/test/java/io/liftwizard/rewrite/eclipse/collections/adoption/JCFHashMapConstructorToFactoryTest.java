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

class JCFHashMapConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new JCFHashMapConstructorToFactory());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.HashMap;
					import java.util.List;
					import java.util.Map;

					class Test {
					    private final Map<String, String> fieldInterfaceEmpty = new HashMap<>();
					    private final Map<String, Integer> fieldInterfaceCapacity = new HashMap<>(10);
					    private final Map<String, String> fieldInterfaceMap = new HashMap<>(this.fieldInterfaceEmpty);

					    void test(Map<String, String> inputMap) {
					        Map<String, Integer> typeInference = new HashMap<>();
					        Map<String, List<Integer>> nestedGenerics = new HashMap<>();
					        Map<String, ? extends Number> wildcardGenerics = new HashMap<>();
					        Map<String, Integer> explicitSimple = new HashMap<String, Integer>();
					        Map<String, List<Integer>> explicitNested = new HashMap<String, List<Integer>>();
					        java.util.Map<String, Integer> fullyQualified = new HashMap<>();
					        Map<String, Integer> withCapacity20 = new HashMap<>(20);
					        Map<String, Integer> explicit30 = new HashMap<String, Integer>(30);
					        Map<String, String> interfaceFromMap = new HashMap<>(inputMap);
					        Map<String, String> fromMap = new HashMap<>(this.fieldInterfaceEmpty);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.Maps;

					import java.util.List;
					import java.util.Map;

					class Test {
					    private final Map<String, String> fieldInterfaceEmpty = Maps.mutable.empty();
					    private final Map<String, Integer> fieldInterfaceCapacity = Maps.mutable.withInitialCapacity(10);
					    private final Map<String, String> fieldInterfaceMap = Maps.mutable.withMap(this.fieldInterfaceEmpty);

					    void test(Map<String, String> inputMap) {
					        Map<String, Integer> typeInference = Maps.mutable.empty();
					        Map<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
					        Map<String, ? extends Number> wildcardGenerics = Maps.mutable.empty();
					        Map<String, Integer> explicitSimple = Maps.mutable.<String, Integer>empty();
					        Map<String, List<Integer>> explicitNested = Maps.mutable.<String, List<Integer>>empty();
					        java.util.Map<String, Integer> fullyQualified = Maps.mutable.empty();
					        Map<String, Integer> withCapacity20 = Maps.mutable.withInitialCapacity(20);
					        Map<String, Integer> explicit30 = Maps.mutable.<String, Integer>withInitialCapacity(30);
					        Map<String, String> interfaceFromMap = Maps.mutable.withMap(inputMap);
					        Map<String, String> fromMap = Maps.mutable.withMap(this.fieldInterfaceEmpty);
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
					    private final HashMap<String, String> fieldConcreteType = new HashMap<>();

					    void test(Map<String, String> inputMap) {
					        HashMap<String, Integer> diamondMap = new HashMap<>();
					        HashMap rawMap = new HashMap();
					        HashMap<String, Integer> withInitialCapacity = new HashMap<>(10);
					        HashMap<String, String> concreteFromMap = new HashMap<>(inputMap);
					    }
					}
					"""
				)
			);
	}
}
