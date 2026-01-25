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

class JCFTreeMapConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new JCFTreeMapConstructorToFactory());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Comparator;
					import java.util.List;
					import java.util.Map;
					import java.util.SortedMap;
					import java.util.TreeMap;

					class Test {
					    private final SortedMap<String, String> fieldInterfaceEmpty = new TreeMap<>();
					    private final SortedMap<String, String> fieldInterfaceComparator = new TreeMap<>(Comparator.naturalOrder());
					    private final SortedMap<String, String> fieldInterfaceMap = new TreeMap<>(this.fieldInterfaceEmpty);

					    void test(SortedMap<String, String> inputMap) {
					        SortedMap<String, Integer> typeInference = new TreeMap<>();
					        SortedMap<String, List<Integer>> nestedGenerics = new TreeMap<>();
					        SortedMap<String, ? extends Number> wildcardGenerics = new TreeMap<>();
					        SortedMap<String, Integer> explicitSimple = new TreeMap<String, Integer>();
					        SortedMap<String, List<Integer>> explicitNested = new TreeMap<String, List<Integer>>();
					        java.util.SortedMap<String, Integer> fullyQualified = new TreeMap<>();
					        SortedMap<String, String> sortedMapWithComparator = new TreeMap<>(Comparator.naturalOrder());
					        SortedMap<String, String> interfaceFromMap = new TreeMap<>(inputMap);
					        SortedMap<String, String> fromMap = new TreeMap<>(this.fieldInterfaceEmpty);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.SortedMaps;

					import java.util.Comparator;
					import java.util.List;
					import java.util.Map;
					import java.util.SortedMap;

					class Test {
					    private final SortedMap<String, String> fieldInterfaceEmpty = SortedMaps.mutable.empty();
					    private final SortedMap<String, String> fieldInterfaceComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
					    private final SortedMap<String, String> fieldInterfaceMap = SortedMaps.mutable.withSortedMap(this.fieldInterfaceEmpty);

					    void test(SortedMap<String, String> inputMap) {
					        SortedMap<String, Integer> typeInference = SortedMaps.mutable.empty();
					        SortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
					        SortedMap<String, ? extends Number> wildcardGenerics = SortedMaps.mutable.empty();
					        SortedMap<String, Integer> explicitSimple = SortedMaps.mutable.<String, Integer>empty();
					        SortedMap<String, List<Integer>> explicitNested = SortedMaps.mutable.<String, List<Integer>>empty();
					        java.util.SortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
					        SortedMap<String, String> sortedMapWithComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
					        SortedMap<String, String> interfaceFromMap = SortedMaps.mutable.withSortedMap(inputMap);
					        SortedMap<String, String> fromMap = SortedMaps.mutable.withSortedMap(this.fieldInterfaceEmpty);
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
					import java.util.Comparator;
					import java.util.Map;
					import java.util.SortedMap;
					import java.util.TreeMap;

					class Test {
					    private final TreeMap<String, String> fieldConcreteType = new TreeMap<>();

					    void test(SortedMap<String, String> inputMap) {
					        TreeMap<String, Integer> diamondMap = new TreeMap<>();
					        TreeMap rawMap = new TreeMap();
					        TreeMap<String, Integer> concreteTypeWithComparator = new TreeMap<>(Comparator.naturalOrder());
					        TreeMap<String, String> concreteFromMap = new TreeMap<>(inputMap);
					    }
					}
					"""
				)
			);
	}
}
