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

class ECStreamFilterCollectToSelectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamFilterCollectToSelect());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.function.Predicate;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;
					    Predicate<String> predicate;
					    Predicate<Integer> intPredicate;

					    // Predicate variable with toList
					    List<String> result1 = mutableList.stream().filter(predicate).collect(Collectors.toList());

					    // toSet
					    Set<String> result2 = mutableList.stream().filter(predicate).collect(Collectors.toSet());

					    // Lambda predicate
					    List<String> result3 = mutableList.stream().filter(s -> s.length() > 5).collect(Collectors.toList());

					    // Method reference
					    List<String> result4 = mutableList.stream().filter(String::isEmpty).collect(Collectors.toList());

					    // ImmutableList
					    List<String> result5 = immutableList.stream().filter(predicate).collect(Collectors.toList());

					    // MutableSet
					    Set<Integer> result6 = mutableSet.stream().filter(intPredicate).collect(Collectors.toSet());
					}
					""",
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.function.Predicate;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;
					    Predicate<String> predicate;
					    Predicate<Integer> intPredicate;

					    // Predicate variable with toList
					    List<String> result1 = mutableList.select(predicate);

					    // toSet
					    Set<String> result2 = mutableList.select(predicate).toSet();

					    // Lambda predicate
					    List<String> result3 = mutableList.select(s -> s.length() > 5);

					    // Method reference
					    List<String> result4 = mutableList.select(String::isEmpty);

					    // ImmutableList
					    List<String> result5 = immutableList.select(predicate);

					    // MutableSet
					    Set<Integer> result6 = mutableSet.select(intPredicate).toSet();
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
					import java.util.function.Predicate;
					import java.util.stream.Collectors;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    ArrayList<String> arrayList;
					    Predicate<String> predicate;

					    // toUnmodifiableList - unmodifiable != immutable
					    List<String> invalid1 = mutableList.stream().filter(predicate).collect(Collectors.toUnmodifiableList());

					    // toUnmodifiableSet - unmodifiable != immutable
					    Set<String> invalid2 = mutableList.stream().filter(predicate).collect(Collectors.toUnmodifiableSet());

					    // Only stream
					    Stream<String> invalid3 = mutableList.stream();

					    // Only filter without collect
					    Stream<String> invalid4 = mutableList.stream().filter(String::isEmpty);

					    // Non-Eclipse Collections type
					    List<String> invalid5 = arrayList.stream().filter(predicate).collect(Collectors.toList());

					    // Multiple intermediate operations
					    List<String> invalid6() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .map(String::toUpperCase)
					            .collect(Collectors.toList());
					    }
					}
					"""
				)
			);
	}
}
