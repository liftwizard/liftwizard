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

class ECStreamCollectGroupingByIdentityCountingToToBagTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectGroupingByIdentityCountingToToBag());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Map;
					import java.util.function.Function;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    void test() {
					        Map<String, Long> result1 = mutableList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					        Map<String, Long> result2 = immutableList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					        Map<String, Long> result3 = mutableSet.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					    }
					}
					""",
					"""
					import java.util.Map;
					import java.util.function.Function;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    void test() {
					        Map<String, Long> result1 = mutableList.toBag();
					        Map<String, Long> result2 = immutableList.toBag();
					        Map<String, Long> result3 = mutableSet.toBag();
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
					import java.util.function.Function;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> list;
					    ArrayList<String> arrayList = new ArrayList<>();

					    void test() {
					        Map<String, Long> result1 = arrayList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					        Map<Integer, Long> result2 = list.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));
					        Map<String, List<String>> result3 = list.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.toList()));
					        Map<String, List<String>> result4 = list.stream().collect(Collectors.groupingBy(Function.identity()));
					    }
					}
					"""
				)
			);
	}
}
