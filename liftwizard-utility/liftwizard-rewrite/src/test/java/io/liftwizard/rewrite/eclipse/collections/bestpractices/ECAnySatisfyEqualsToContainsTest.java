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

class ECAnySatisfyEqualsToContainsTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECAnySatisfyEqualsToContainsRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, String target) {
					        boolean containsTarget = list.anySatisfy(target::equals);

					        if (list.anySatisfy(target::equals)) {
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
					        boolean containsExpected = list.anySatisfy(this.expected::equals);
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
					        boolean containsTarget = list.anySatisfy(target::equals);
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
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.block.predicate.Predicate;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate) {
					        boolean anySatisfyPredicate = list.anySatisfy(predicate);
					        boolean anySatisfyLambda = list.anySatisfy(s -> s.length() > 5);
					        boolean directContains = list.contains("hello");
					    }
					}
					"""
				)
			);
	}
}
