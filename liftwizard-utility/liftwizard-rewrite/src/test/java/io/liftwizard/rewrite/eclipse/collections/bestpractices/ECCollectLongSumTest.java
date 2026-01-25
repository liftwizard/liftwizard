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

class ECCollectLongSumTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECCollectLongSumRecipes());
	}

	@DocumentExample
	@Test
	void replaceCollectLongSumWithSumOfLong() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        return list.collectLong(s -> (long) s.length()).sum();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<String> list) {
					        return list.sumOfLong(s -> (long) s.length());
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceCollectLongSumWithLambda() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<Long> list) {
					        return list.collectLong(l -> l * 2).sum();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long test(MutableList<Long> list) {
					        return list.sumOfLong(l -> l * 2);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceCollectLongSumWithRichIterable() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.RichIterable;

					class Test {
					    long test(RichIterable<Long> iterable) {
					        return iterable.collectLong(l -> l * 2).sum();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.RichIterable;

					class Test {
					    long test(RichIterable<Long> iterable) {
					        return iterable.sumOfLong(l -> l * 2);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceCollectLongSumInExpression() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Long> list) {
					        long result = list.collectLong(l -> l).sum() + 10;
					        if (list.collectLong(l -> l * 2).sum() > 100) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<Long> list) {
					        long result = list.sumOfLong(l -> l) + 10;
					        if (list.sumOfLong(l -> l * 2) > 100) {
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
					    long test(MutableList<Long> list) {
					        // Should not replace when there are intermediate operations
					        long sumOfFiltered = list.collectLong(l -> l)
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
	void doNotReplaceWhenOnlyCollectLong() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.LongIterable;

					class Test {
					    LongIterable test(MutableList<Long> list) {
					        // Should not replace when we only call collectLong without sum
					        return list.collectLong(l -> l);
					    }
					}
					"""
				)
			);
	}
}
