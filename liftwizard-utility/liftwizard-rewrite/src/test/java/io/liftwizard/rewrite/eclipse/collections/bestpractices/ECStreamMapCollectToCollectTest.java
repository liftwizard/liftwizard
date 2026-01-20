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

class ECStreamMapCollectToCollectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamMapCollectToCollect());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.function.Function;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;
					    Function<String, Integer> function;

					    // Function variable with toList
					    List<Integer> result1 = mutableList.stream().map(function).collect(Collectors.toList());

					    // toSet
					    Set<Integer> result2 = mutableList.stream().map(function).collect(Collectors.toSet());

					    // Lambda function
					    List<Integer> result3 = mutableList.stream().map(s -> s.length()).collect(Collectors.toList());

					    // Method reference
					    List<Integer> result4 = mutableList.stream().map(String::length).collect(Collectors.toList());

					    // ImmutableList
					    List<Integer> result5 = immutableList.stream().map(function).collect(Collectors.toList());

					    // MutableSet
					    Set<Integer> result6 = mutableSet.stream().map(function).collect(Collectors.toSet());
					}
					""",
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.function.Function;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;
					    Function<String, Integer> function;

					    // Function variable with toList
					    List<Integer> result1 = mutableList.collect(function);

					    // toSet
					    Set<Integer> result2 = mutableList.collect(function).toSet();

					    // Lambda function
					    List<Integer> result3 = mutableList.collect(s -> s.length());

					    // Method reference
					    List<Integer> result4 = mutableList.collect(String::length);

					    // ImmutableList
					    List<Integer> result5 = immutableList.collect(function);

					    // MutableSet
					    Set<Integer> result6 = mutableSet.collect(function).toSet();
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
					import java.util.Set;
					import java.util.function.Function;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    ArrayList<String> arrayList;
					    Function<String, Integer> function;

					    // toUnmodifiableList - unmodifiable != immutable
					    List<Integer> invalid1 = mutableList.stream().map(function).collect(Collectors.toUnmodifiableList());

					    // toUnmodifiableSet - unmodifiable != immutable
					    Set<Integer> invalid2 = mutableList.stream().map(function).collect(Collectors.toUnmodifiableSet());

					    // Only stream
					    Stream<String> invalid3 = mutableList.stream();

					    // Only map without collect
					    Stream<Integer> invalid4 = mutableList.stream().map(String::length);

					    // Non-Eclipse Collections type
					    List<Integer> invalid5 = arrayList.stream().map(function).collect(Collectors.toList());

					    // Multiple intermediate operations
					    List<String> invalid6() {
					        return mutableList.stream()
					            .map(String::toUpperCase)
					            .filter(s -> s.length() > 3)
					            .collect(Collectors.toList());
					    }
					}
					"""
				)
			);
	}
}
