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

class ECCollectIntSumTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECCollectIntSumRecipes());
	}

	@DocumentExample
	@Test
	void replaceCollectIntSumWithSumOfInt() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        return list.collectInt(String::length).sum();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        return list.sumOfInt(String::length);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceCollectIntSumWithLambda() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        return list.collectInt(s -> s.length()).sum();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        return list.sumOfInt(s -> s.length());
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceCollectIntSumWithRichIterable() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.RichIterable;

					class Test {
					    long test(RichIterable<Integer> iterable) {
					        return iterable.collectInt(i -> i * 2).sum();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.RichIterable;

					class Test {
					    long test(RichIterable<Integer> iterable) {
					        return iterable.sumOfInt(i -> i * 2);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceCollectIntSumInExpression() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        long result = list.collectInt(String::length).sum() + 10;
					        if (list.collectInt(s -> s.length()).sum() > 100) {
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
					        long result = list.sumOfInt(String::length) + 10;
					        if (list.sumOfInt(s -> s.length()) > 100) {
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
	void doNotReplaceIntermediateOperations() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        // Should not replace when there are intermediate operations
					        long sumOfFiltered = list.collectInt(String::length)
					            .select(i -> i > 5)
					            .sum();
					        return sumOfFiltered;
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceWhenOnlyCollectInt() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.IntIterable;

					class Test {
					    IntIterable test(MutableList<String> list) {
					        // Should not replace when we only call collectInt without sum
					        return list.collectInt(String::length);
					    }
					}
					"""
				)
			);
	}
}
