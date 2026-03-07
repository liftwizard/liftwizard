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

class ECStreamCollectGroupingByCountingToCountByTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectGroupingByCountingToCountBy());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Map;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> set;

					    void test() {
					        Map<Integer, Long> result1 = mutableList.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
					        Map<String, Long> result2 = mutableList.stream().collect(Collectors.groupingBy(s -> s.substring(0, 1), Collectors.counting()));
					        Map<Integer, Long> result3 = immutableList.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
					        Map<Integer, Long> result4 = set.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
					    }
					}
					""",
					"""
					import java.util.Map;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> set;

					    void test() {
					        Map<Integer, Long> result1 = mutableList.countBy(String::length);
					        Map<String, Long> result2 = mutableList.countBy(s -> s.substring(0, 1));
					        Map<Integer, Long> result3 = immutableList.countBy(String::length);
					        Map<Integer, Long> result4 = set.countBy(String::length);
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
					import java.util.List;
					import java.util.Map;
					import java.util.TreeMap;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    ArrayList<String> arrayList = new ArrayList<>();
					    MutableList<String> list;

					    void test() {
					        Map<Integer, Long> result1 = arrayList.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
					        Map<Integer, List<String>> result2 = list.stream().collect(Collectors.groupingBy(String::length, Collectors.toList()));
					        Map<Integer, List<String>> result3 = list.stream().collect(Collectors.groupingBy(String::length));
					        Map<Integer, Long> result4 = list.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.counting()));
					    }
					}
					"""
				)
			);
	}
}
