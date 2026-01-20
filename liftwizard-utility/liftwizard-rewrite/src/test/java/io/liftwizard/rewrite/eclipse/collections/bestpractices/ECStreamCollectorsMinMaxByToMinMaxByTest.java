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

class ECStreamCollectorsMinMaxByToMinMaxByTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCollectorsMinMaxByToMinMaxBy());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Comparator;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<Integer> immutableList;
					    MutableSet<String> mutableSet;

					    // Method reference
					    String minByMethodRef = mutableList.stream().collect(Collectors.minBy(Comparator.comparing(String::length))).orElse(null);
					    String maxByMethodRef = mutableList.stream().collect(Collectors.maxBy(Comparator.comparing(String::length))).orElse(null);

					    // Lambda
					    String minByLambda = mutableList.stream().collect(Collectors.minBy(Comparator.comparing(s -> s.length()))).orElse(null);
					    String maxByLambda = mutableList.stream().collect(Collectors.maxBy(Comparator.comparing(s -> s.length()))).orElse(null);

					    // ImmutableList
					    Integer minByImmutable = immutableList.stream().collect(Collectors.minBy(Comparator.comparing(n -> n))).orElse(null);
					    Integer maxByImmutable = immutableList.stream().collect(Collectors.maxBy(Comparator.comparing(n -> n))).orElse(null);

					    // MutableSet
					    String minBySet = mutableSet.stream().collect(Collectors.minBy(Comparator.comparing(String::length))).orElse(null);
					    String maxBySet = mutableSet.stream().collect(Collectors.maxBy(Comparator.comparing(String::length))).orElse(null);
					}
					""",
					"""
					import java.util.Comparator;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<Integer> immutableList;
					    MutableSet<String> mutableSet;

					    // Method reference
					    String minByMethodRef = mutableList.minBy(String::length);
					    String maxByMethodRef = mutableList.maxBy(String::length);

					    // Lambda
					    String minByLambda = mutableList.minBy(s -> s.length());
					    String maxByLambda = mutableList.maxBy(s -> s.length());

					    // ImmutableList
					    Integer minByImmutable = immutableList.minBy(n -> n);
					    Integer maxByImmutable = immutableList.maxBy(n -> n);

					    // MutableSet
					    String minBySet = mutableSet.minBy(String::length);
					    String maxBySet = mutableSet.maxBy(String::length);
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
					import java.util.Comparator;
					import java.util.List;
					import java.util.stream.Collectors;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    List<String> jcfList;
					    Comparator<String> rawComparator;

					    // orElse with non-null default value
					    String invalidMin1 = mutableList.stream().collect(Collectors.minBy(Comparator.comparing(String::length))).orElse("default");
					    String invalidMax1 = mutableList.stream().collect(Collectors.maxBy(Comparator.comparing(String::length))).orElse("default");

					    // Non-Eclipse Collections type (JCF List)
					    String invalidMin2 = jcfList.stream().collect(Collectors.minBy(Comparator.comparing(String::length))).orElse(null);
					    String invalidMax2 = jcfList.stream().collect(Collectors.maxBy(Comparator.comparing(String::length))).orElse(null);

					    // Without Comparator.comparing (raw Comparator)
					    String invalidMin3 = mutableList.stream().collect(Collectors.minBy(rawComparator)).orElse(null);
					    String invalidMax3 = mutableList.stream().collect(Collectors.maxBy(rawComparator)).orElse(null);

					    // Intermediate operations (filter) - methods needed for complex expressions
					    String intermediateMin() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .collect(Collectors.minBy(Comparator.comparing(String::length)))
					            .orElse(null);
					    }

					    String intermediateMax() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .collect(Collectors.maxBy(Comparator.comparing(String::length)))
					            .orElse(null);
					    }
					}
					"""
				)
			);
	}
}
