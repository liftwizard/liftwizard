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

class ECStreamCollectGroupingByToGroupByTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectGroupingByToGroupBy());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Map;
					import java.util.Set;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    void test() {
					        Map<Integer, Set<String>> result1 = mutableList.stream().collect(Collectors.groupingBy(String::length, Collectors.toSet()));
					        Map<Integer, List<String>> result2 = mutableList.stream().collect(Collectors.groupingBy(String::length, Collectors.toList()));
					        Map<Integer, List<String>> result3 = mutableList.stream().collect(Collectors.groupingBy(String::length));
					        Map<String, Set<String>> result4 = mutableList.stream().collect(Collectors.groupingBy(s -> s.substring(0, 1), Collectors.toSet()));
					        Map<Integer, Set<String>> result5 = immutableList.stream().collect(Collectors.groupingBy(String::length, Collectors.toSet()));
					        Map<Integer, Set<String>> result6 = mutableSet.stream().collect(Collectors.groupingBy(String::length, Collectors.toSet()));
					    }
					}
					""",
					"""
					import java.util.List;
					import java.util.Map;
					import java.util.Set;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    void test() {
					        Map<Integer, Set<String>> result1 = mutableList.groupBy(String::length);
					        Map<Integer, List<String>> result2 = mutableList.groupBy(String::length);
					        Map<Integer, List<String>> result3 = mutableList.groupBy(String::length);
					        Map<String, Set<String>> result4 = mutableList.groupBy(s -> s.substring(0, 1));
					        Map<Integer, Set<String>> result5 = immutableList.groupBy(String::length);
					        Map<Integer, Set<String>> result6 = mutableSet.groupBy(String::length);
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
					import java.util.ArrayList;
					import java.util.Map;
					import java.util.Set;
					import java.util.TreeMap;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> list;
					    ArrayList<String> arrayList = new ArrayList<>();

					    void test() {
					        Map<Integer, Set<String>> result1 = arrayList.stream().collect(Collectors.groupingBy(String::length, Collectors.toSet()));
					        Map<Integer, Long> result2 = list.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
					        Map<Integer, Set<String>> result3 = list.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.toSet()));
					    }
					}
					"""
				)
			);
	}
}
