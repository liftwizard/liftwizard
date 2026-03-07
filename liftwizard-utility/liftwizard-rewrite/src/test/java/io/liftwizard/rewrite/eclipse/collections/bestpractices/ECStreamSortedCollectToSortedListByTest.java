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

class ECStreamSortedCollectToSortedListByTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamSortedCollectToSortedListBy());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Comparator;
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    // Method reference
					    List<String> result1 = mutableList.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());

					    // Lambda
					    List<String> result2 = mutableList.stream().sorted(Comparator.comparing(s -> s.length())).collect(Collectors.toList());

					    // ImmutableList
					    List<String> result3 = immutableList.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());

					    // MutableSet
					    List<String> result4 = mutableSet.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());
					}
					""",
					"""
					import java.util.Comparator;
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<String> mutableSet;

					    // Method reference
					    List<String> result1 = mutableList.toSortedListBy(String::length);

					    // Lambda
					    List<String> result2 = mutableList.toSortedListBy(s -> s.length());

					    // ImmutableList
					    List<String> result3 = immutableList.toSortedListBy(String::length);

					    // MutableSet
					    List<String> result4 = mutableSet.toSortedListBy(String::length);
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
					import java.util.Comparator;
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    ArrayList<String> arrayList;
					    Comparator<String> rawComparator;

					    // Non-Eclipse Collections type (JCF ArrayList)
					    List<String> invalid1 = arrayList.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toList());

					    // Without Comparator.comparing (raw Comparator)
					    List<String> invalid2 = mutableList.stream().sorted(rawComparator).collect(Collectors.toList());

					    // Intermediate operations (filter before sorted)
					    List<String> invalid3() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .sorted(Comparator.comparing(String::length))
					            .collect(Collectors.toList());
					    }

					    // toUnmodifiableList instead of toList
					    List<String> invalid4 = mutableList.stream().sorted(Comparator.comparing(String::length)).collect(Collectors.toUnmodifiableList());
					}
					"""
				)
			);
	}
}
