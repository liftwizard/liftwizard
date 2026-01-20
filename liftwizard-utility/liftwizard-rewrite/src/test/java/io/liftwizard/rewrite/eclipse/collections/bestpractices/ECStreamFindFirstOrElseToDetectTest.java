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

class ECStreamFindFirstOrElseToDetectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamFindFirstOrElseToDetect());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;
					    Predicate<String> predicate;
					    Predicate<Integer> intPredicate;

					    // Predicate variable
					    String result1 = mutableList.stream().filter(predicate).findFirst().orElse(null);

					    // Lambda predicate
					    String result2 = mutableList.stream().filter(s -> s.length() > 5).findFirst().orElse(null);

					    // Method reference
					    String result3 = mutableList.stream().filter(String::isEmpty).findFirst().orElse(null);

					    // ImmutableList
					    String result4 = immutableList.stream().filter(predicate).findFirst().orElse(null);

					    // MutableSet
					    Integer result5 = mutableSet.stream().filter(intPredicate).findFirst().orElse(null);
					}
					""",
					"""
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    MutableList<String> mutableList;
					    ImmutableList<String> immutableList;
					    MutableSet<Integer> mutableSet;
					    Predicate<String> predicate;
					    Predicate<Integer> intPredicate;

					    // Predicate variable
					    String result1 = mutableList.detect(predicate);

					    // Lambda predicate
					    String result2 = mutableList.detect(s -> s.length() > 5);

					    // Method reference
					    String result3 = mutableList.detect(String::isEmpty);

					    // ImmutableList
					    String result4 = immutableList.detect(predicate);

					    // MutableSet
					    Integer result5 = mutableSet.detect(intPredicate);
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
					import java.util.List;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> mutableList;
					    List<String> jcfList;
					    Predicate<String> predicate;
					    String defaultValue;

					    // orElse with literal default value - would need detectIfNone
					    String invalid1 = mutableList.stream().filter(predicate).findFirst().orElse("default");

					    // orElse with variable default value - would need detectIfNone
					    String invalid2 = mutableList.stream().filter(predicate).findFirst().orElse(defaultValue);

					    // Non-Eclipse Collections type
					    String invalid3 = jcfList.stream().filter(predicate).findFirst().orElse(null);

					    // Without filter
					    String invalid4 = mutableList.stream().findFirst().orElse(null);

					    // Multiple intermediate operations
					    String invalid5() {
					        return mutableList.stream()
					            .filter(s -> s.length() > 3)
					            .map(String::toUpperCase)
					            .findFirst()
					            .orElse(null);
					    }
					}
					"""
				)
			);
	}
}
