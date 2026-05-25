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
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long withMethodReference(MutableList<String> list) {
					        return list.collectInt(String::length).sum();
					    }

					    long withLambda(MutableList<String> list) {
					        return list.collectInt(s -> s.length()).sum();
					    }

					    long withRichIterable(RichIterable<Integer> iterable) {
					        return iterable.collectInt(i -> i * 2).sum();
					    }

					    void inExpression(MutableList<String> list) {
					        long result = list.collectInt(String::length).sum() + 10;
					        if (list.collectInt(s -> s.length()).sum() > 100) {
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
					    long withMethodReference(MutableList<String> list) {
					        return list.sumOfInt(String::length);
					    }

					    long withLambda(MutableList<String> list) {
					        return list.sumOfInt(s -> s.length());
					    }

					    long withRichIterable(RichIterable<Integer> iterable) {
					        return iterable.sumOfInt(i -> i * 2);
					    }

					    void inExpression(MutableList<String> list) {
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
	void doNotReplaceInvalidPatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.IntIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long withIntermediateOperations(MutableList<String> list) {
					        return list.collectInt(String::length)
					            .select(i -> i > 5)
					            .sum();
					    }

					    IntIterable onlyCollectInt(MutableList<String> list) {
					        return list.collectInt(String::length);
					    }
					}
					"""
				)
			);
	}
}
