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

class JCFSortedMapToMutableSortedMapTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new JCFSortedMapToMutableSortedMap());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.SortedMap;
					import org.eclipse.collections.api.factory.SortedMaps;
					import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

					class Test {
					    private final SortedMap<String, Integer> fieldSortedMap = SortedMaps.mutable.empty();

					    void test() {
					        SortedMap<String, Integer> sortedMap = SortedMaps.mutable.empty();
					        java.util.SortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
					        SortedMap rawSortedMap = SortedMaps.mutable.empty();
					        java.util.SortedMap rawSortedMapFullyQualified = SortedMaps.mutable.empty();
					        SortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
					        SortedMap<String, Integer> treeSortedMap = TreeSortedMap.newMap();
					        SortedMap<String, Integer> map1 = SortedMaps.mutable.empty(), map2 = SortedMaps.mutable.with("a", 1);
					    }
					}
					""",
					"""
					import java.util.List;
					import java.util.SortedMap;
					import org.eclipse.collections.api.factory.SortedMaps;
					import org.eclipse.collections.api.map.sorted.MutableSortedMap;
					import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

					class Test {
					    private final MutableSortedMap<String, Integer> fieldSortedMap = SortedMaps.mutable.empty();

					    void test() {
					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.empty();
					        MutableSortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
					        MutableSortedMap rawSortedMap = SortedMaps.mutable.empty();
					        MutableSortedMap rawSortedMapFullyQualified = SortedMaps.mutable.empty();
					        MutableSortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
					        MutableSortedMap<String, Integer> treeSortedMap = TreeSortedMap.newMap();
					        MutableSortedMap<String, Integer> map1 = SortedMaps.mutable.empty(), map2 = SortedMaps.mutable.with("a", 1);
					    }
					}
					"""
				)
			);
	}
}
