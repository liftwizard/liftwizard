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

class ECStreamCollectToMapToGroupByUniqueKeyTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectToMapToGroupByUniqueKey());
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
					        Map<Integer, String> result1 = mutableList.stream().collect(Collectors.toMap(String::length, Function.identity()));
					        Map<String, String> result2 = mutableList.stream().collect(Collectors.toMap(s -> s.substring(0, 1), Function.identity()));
					        Map<Integer, String> result3 = immutableList.stream().collect(Collectors.toMap(String::length, Function.identity()));
					        Map<Integer, String> result4 = mutableSet.stream().collect(Collectors.toMap(String::length, Function.identity()));
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
					        Map<Integer, String> result1 = mutableList.groupByUniqueKey(String::length);
					        Map<String, String> result2 = mutableList.groupByUniqueKey(s -> s.substring(0, 1));
					        Map<Integer, String> result3 = immutableList.groupByUniqueKey(String::length);
					        Map<Integer, String> result4 = mutableSet.groupByUniqueKey(String::length);
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
					import java.util.function.Function;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> list;
					    ArrayList<String> arrayList = new ArrayList<>();

					    void test() {
					        Map<Integer, String> result1 = list.stream().collect(Collectors.toMap(String::length, String::toUpperCase));
					        Map<Integer, String> result2 = arrayList.stream().collect(Collectors.toMap(String::length, Function.identity()));
					        Map<Integer, String> result3 = list.stream().collect(Collectors.toMap(String::length, Function.identity(), (a, b) -> a));
					    }
					}
					"""
				)
			);
	}
}
