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

class ECStreamAnyMatchToContainsTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamAnyMatchToContainsRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    private final String expected = "hello";

					    void streamAnyMatchWithContains(MutableList<String> list, String target) {
					        boolean containsTarget = list.stream().anyMatch(target::equals);

					        if (list.stream().anyMatch(target::equals)) {
					            this.doWork();
					        }
					    }

					    void withFieldReference(MutableList<String> list) {
					        boolean containsExpected = list.stream().anyMatch(this.expected::equals);
					    }

					    void withIntegerType(MutableList<Integer> list, Integer target) {
					        boolean containsTarget = list.stream().anyMatch(target::equals);
					    }

					    void withImmutableList(ImmutableList<String> list, String target) {
					        boolean containsTarget = list.stream().anyMatch(target::equals);
					    }

					    void withMutableSet(MutableSet<String> set, String target) {
					        boolean containsTarget = set.stream().anyMatch(target::equals);
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    private final String expected = "hello";

					    void streamAnyMatchWithContains(MutableList<String> list, String target) {
					        boolean containsTarget = list.contains(target);

					        if (list.contains(target)) {
					            this.doWork();
					        }
					    }

					    void withFieldReference(MutableList<String> list) {
					        boolean containsExpected = list.contains(this.expected);
					    }

					    void withIntegerType(MutableList<Integer> list, Integer target) {
					        boolean containsTarget = list.contains(target);
					    }

					    void withImmutableList(ImmutableList<String> list, String target) {
					        boolean containsTarget = list.contains(target);
					    }

					    void withMutableSet(MutableSet<String> set, String target) {
					        boolean containsTarget = set.contains(target);
					    }

					    void doWork() {}
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
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void otherPredicates(MutableList<String> list, Predicate<String> predicate) {
					        boolean anyMatchPredicate = list.stream().anyMatch(predicate);
					        boolean anyMatchLambda = list.stream().anyMatch(s -> s.length() > 5);
					        boolean directContains = list.contains("hello");
					    }

					    boolean withIntermediateOperations(MutableList<String> list, String target) {
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .anyMatch(target::equals);
					    }
					}
					"""
				)
			);
	}
}
