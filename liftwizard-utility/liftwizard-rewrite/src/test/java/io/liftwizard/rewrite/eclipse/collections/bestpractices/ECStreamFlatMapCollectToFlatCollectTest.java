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

class ECStreamFlatMapCollectToFlatCollectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamFlatMapCollectToFlatCollect());
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
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<MutableList<String>> mutableList;
					    ImmutableList<MutableList<String>> immutableList;
					    MutableSet<MutableList<String>> mutableSet;

					    // Lambda with toList
					    List<String> result1 = mutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toList());

					    // Lambda with toSet
					    Set<String> result2 = mutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());

					    // Lambda with method call then stream
					    List<String> result3 = mutableList.stream().flatMap(x -> x.subList(0, 1).stream()).collect(Collectors.toList());

					    // ImmutableList
					    List<String> result4 = immutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toList());

					    // MutableSet with toSet
					    Set<String> result5 = mutableSet.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());
					}
					""",
					"""
					import java.util.List;
					import java.util.Set;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<MutableList<String>> mutableList;
					    ImmutableList<MutableList<String>> immutableList;
					    MutableSet<MutableList<String>> mutableSet;

					    // Lambda with toList
					    List<String> result1 = mutableList.flatCollect(x -> x);

					    // Lambda with toSet
					    Set<String> result2 = mutableList.flatCollect(x -> x).toSet();

					    // Lambda with method call then stream
					    List<String> result3 = mutableList.flatCollect(x -> x.subList(0, 1));

					    // ImmutableList
					    List<String> result4 = immutableList.flatCollect(x -> x);

					    // MutableSet with toSet
					    Set<String> result5 = mutableSet.flatCollect(x -> x).toSet();
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
					    MutableList<MutableList<String>> mutableList;
					    ArrayList<List<String>> arrayList;

					    // toUnmodifiableList - unmodifiable != immutable
					    List<String> invalid1 = mutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toUnmodifiableList());

					    // toUnmodifiableSet - unmodifiable != immutable
					    Set<String> invalid2 = mutableList.stream().flatMap(x -> x.stream()).collect(Collectors.toUnmodifiableSet());

					    // Only stream
					    Stream<MutableList<String>> invalid3 = mutableList.stream();

					    // Only flatMap without collect
					    Stream<String> invalid4 = mutableList.stream().flatMap(x -> x.stream());

					    // Non-Eclipse Collections type
					    List<String> invalid5 = arrayList.stream().flatMap(x -> x.stream()).collect(Collectors.toList());

					    // Lambda body does not end with .stream()
					    List<Integer> invalid6 = mutableList.stream().flatMap(x -> Stream.of(x.size())).collect(Collectors.toList());
					}
					"""
				)
			);
	}
}
