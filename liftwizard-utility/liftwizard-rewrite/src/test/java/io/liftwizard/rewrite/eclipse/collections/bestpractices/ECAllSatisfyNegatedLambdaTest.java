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

class ECAllSatisfyNegatedLambdaTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECAllSatisfyNegatedLambda());
	}

	@Test
	@DocumentExample
	void replaceAllSatisfyWithNegatedLambda() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean result = list.allSatisfy(s -> !s.isEmpty());
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean result = list.noneSatisfy(s -> s.isEmpty());
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceAllSatisfyWithNegatedMethodCall() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean lengthCheck = list.allSatisfy(s -> !(s.length() > 5));
					        boolean contains = list.allSatisfy(s -> !s.contains("x"));
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean lengthCheck = list.noneSatisfy(s -> s.length() > 5);
					        boolean contains = list.noneSatisfy(s -> s.contains("x"));
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceAllSatisfyWithNegatedComparison() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Integer> list) {
					        boolean result = list.allSatisfy(n -> !(n > 10));
					        boolean equality = list.allSatisfy(n -> !(n == 0));
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Integer> list) {
					        boolean result = list.noneSatisfy(n -> n > 10);
					        boolean equality = list.noneSatisfy(n -> n == 0);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceNonNegatedLambda() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean result = list.allSatisfy(s -> s.isEmpty());
					        boolean lengthCheck = list.allSatisfy(s -> s.length() > 5);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceMethodReference() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.block.predicate.Predicate;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate) {
					        boolean result = list.allSatisfy(String::isEmpty);
					        boolean withPredicate = list.allSatisfy(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceAnySatisfyOrNoneSatisfy() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean any = list.anySatisfy(s -> !s.isEmpty());
					        boolean none = list.noneSatisfy(s -> !s.isEmpty());
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceInIfCondition() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        if (list.allSatisfy(s -> !s.isEmpty())) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        if (list.noneSatisfy(s -> s.isEmpty())) {
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
	void replaceWithRichIterableType() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.RichIterable;

					class Test {
					    void test(RichIterable<String> iterable) {
					        boolean result = iterable.allSatisfy(s -> !s.isEmpty());
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.RichIterable;

					class Test {
					    void test(RichIterable<String> iterable) {
					        boolean result = iterable.noneSatisfy(s -> s.isEmpty());
					    }
					}
					"""
				)
			);
	}
}
