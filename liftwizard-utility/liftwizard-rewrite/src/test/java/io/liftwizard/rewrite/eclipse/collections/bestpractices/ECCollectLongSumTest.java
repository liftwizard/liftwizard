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
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long withCast(MutableList<String> list) {
					        return list.collectLong(s -> (long) s.length()).sum();
					    }

					    long withLambda(MutableList<Long> list) {
					        return list.collectLong(l -> l * 2).sum();
					    }

					    long withRichIterable(RichIterable<Long> iterable) {
					        return iterable.collectLong(l -> l * 2).sum();
					    }

					    void inExpression(MutableList<Long> list) {
					        long result = list.collectLong(l -> l).sum() + 10;
					        if (list.collectLong(l -> l * 2).sum() > 100) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long withCast(MutableList<String> list) {
					        return list.sumOfLong(s -> (long) s.length());
					    }

					    long withLambda(MutableList<Long> list) {
					        return list.sumOfLong(l -> l * 2);
					    }

					    long withRichIterable(RichIterable<Long> iterable) {
					        return iterable.sumOfLong(l -> l * 2);
					    }

					    void inExpression(MutableList<Long> list) {
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
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.LongIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long withIntermediateOperations(MutableList<Long> list) {
					        return list.collectLong(l -> l)
					            .select(i -> i > 5)
					            .sum();
					    }

					    LongIterable onlyCollectLong(MutableList<Long> list) {
					        return list.collectLong(l -> l);
					    }
					}
					"""
				)
			);
	}
}
