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

class ECStreamCollectToUnmodifiableToToImmutableTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectToUnmodifiableToToImmutable());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.ImmutableSet;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;
					    ImmutableSet<Integer> immutableSet;

					    // toUnmodifiableList - MutableList
					    List<String> result1 = mutableList.stream().collect(Collectors.toUnmodifiableList());

					    // toUnmodifiableList - ImmutableList
					    List<String> result2 = immutableList.stream().collect(Collectors.toUnmodifiableList());

					    // toUnmodifiableList - MutableSet
					    List<Integer> result3 = mutableSet.stream().collect(Collectors.toUnmodifiableList());

					    // toUnmodifiableSet - MutableList
					    Set<String> result4 = mutableList.stream().collect(Collectors.toUnmodifiableSet());

					    // toUnmodifiableSet - MutableSet
					    Set<Integer> result5 = mutableSet.stream().collect(Collectors.toUnmodifiableSet());

					    // toUnmodifiableSet - ImmutableSet
					    Set<Integer> result6 = immutableSet.stream().collect(Collectors.toUnmodifiableSet());
					}
					""",
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.ImmutableSet;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;
					    ImmutableSet<Integer> immutableSet;

					    // toUnmodifiableList - MutableList
					    List<String> result1 = mutableList.toImmutableList();

					    // toUnmodifiableList - ImmutableList
					    List<String> result2 = immutableList.toImmutableList();

					    // toUnmodifiableList - MutableSet
					    List<Integer> result3 = mutableSet.toImmutableList();

					    // toUnmodifiableSet - MutableList
					    Set<String> result4 = mutableList.toImmutableSet();

					    // toUnmodifiableSet - MutableSet
					    Set<Integer> result5 = mutableSet.toImmutableSet();

					    // toUnmodifiableSet - ImmutableSet
					    Set<Integer> result6 = immutableSet.toImmutableSet();
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
					import java.util.stream.Collectors;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    ArrayList<String> arrayList;

					    // Collectors.toList() - different recipe handles mutable toList
					    List<String> invalid1 = mutableList.stream().collect(Collectors.toList());

					    // Collectors.toSet() - different recipe handles mutable toSet
					    Set<String> invalid2 = mutableList.stream().collect(Collectors.toSet());

					    // Only stream without collect
					    Stream<String> invalid3 = mutableList.stream();

					    // Non-Eclipse Collections type
					    List<String> invalid4 = arrayList.stream().collect(Collectors.toUnmodifiableList());

					    // Intermediate operations (filter)
					    List<String> invalid5() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .collect(Collectors.toUnmodifiableList());
					    }
					}
					"""
				)
			);
	}
}
