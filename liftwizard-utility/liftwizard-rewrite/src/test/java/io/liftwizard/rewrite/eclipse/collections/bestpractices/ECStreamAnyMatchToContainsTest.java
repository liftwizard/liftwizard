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

	@Test
	@DocumentExample
	void replaceStreamAnyMatchWithContains() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, String target) {
					        boolean containsTarget = list.stream().anyMatch(target::equals);

					        if (list.stream().anyMatch(target::equals)) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, String target) {
					        boolean containsTarget = list.contains(target);

					        if (list.contains(target)) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					"""
				)
			);
	}

	@Test
	void replaceWithFieldReference() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    private final String expected = "hello";

					    void test(MutableList<String> list) {
					        boolean containsExpected = list.stream().anyMatch(this.expected::equals);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    private final String expected = "hello";

					    void test(MutableList<String> list) {
					        boolean containsExpected = list.contains(this.expected);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithIntegerType() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Integer> list, Integer target) {
					        boolean containsTarget = list.stream().anyMatch(target::equals);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Integer> list, Integer target) {
					        boolean containsTarget = list.contains(target);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithImmutableList() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    void test(ImmutableList<String> list, String target) {
					        boolean containsTarget = list.stream().anyMatch(target::equals);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    void test(ImmutableList<String> list, String target) {
					        boolean containsTarget = list.contains(target);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithMutableSet() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void test(MutableSet<String> set, String target) {
					        boolean containsTarget = set.stream().anyMatch(target::equals);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void test(MutableSet<String> set, String target) {
					        boolean containsTarget = set.contains(target);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceOtherPredicates() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate) {
					        boolean anyMatchPredicate = list.stream().anyMatch(predicate);
					        boolean anyMatchLambda = list.stream().anyMatch(s -> s.length() > 5);
					        boolean directContains = list.contains("hello");
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceStreamWithIntermediateOperations() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    boolean test(MutableList<String> list, String target) {
					        // Should not replace when there are intermediate operations
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
