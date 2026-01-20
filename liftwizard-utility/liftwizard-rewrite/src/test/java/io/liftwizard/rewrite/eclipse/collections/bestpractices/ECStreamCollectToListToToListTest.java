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

class ECStreamCollectToListToToListTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectToListToToList());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;

					    // MutableList
					    List<String> result1 = mutableList.stream().collect(Collectors.toList());

					    // ImmutableList
					    List<String> result2 = immutableList.stream().collect(Collectors.toList());

					    // MutableSet
					    List<Integer> result3 = mutableSet.stream().collect(Collectors.toList());
					}
					""",
					"""
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;

					    // MutableList
					    List<String> result1 = mutableList.toList();

					    // ImmutableList
					    List<String> result2 = immutableList.toList();

					    // MutableSet
					    List<Integer> result3 = mutableSet.toList();
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

					    // stream().toList() - unmodifiable != mutable
					    List<String> invalid1 = mutableList.stream().toList();

					    // toUnmodifiableList - unmodifiable != mutable
					    List<String> invalid2 = mutableList.stream().collect(Collectors.toUnmodifiableList());

					    // toSet - different recipe handles this
					    Set<String> invalid3 = mutableList.stream().collect(Collectors.toSet());

					    // Only stream without collect
					    Stream<String> invalid4 = mutableList.stream();

					    // Non-Eclipse Collections type
					    List<String> invalid5 = arrayList.stream().collect(Collectors.toList());

					    // Intermediate operations (filter)
					    List<String> invalid6() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .collect(Collectors.toList());
					    }
					}
					"""
				)
			);
	}
}
