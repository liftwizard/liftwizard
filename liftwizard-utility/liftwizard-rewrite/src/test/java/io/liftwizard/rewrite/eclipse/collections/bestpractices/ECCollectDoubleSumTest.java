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

class ECCollectDoubleSumTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECCollectDoubleSumRecipes());
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
					    double withCast(MutableList<String> list) {
					        return list.collectDouble(s -> (double) s.length()).sum();
					    }

					    double withLambda(MutableList<Double> list) {
					        return list.collectDouble(d -> d * 2).sum();
					    }

					    double withRichIterable(RichIterable<Double> iterable) {
					        return iterable.collectDouble(d -> d * 2).sum();
					    }

					    void inExpression(MutableList<Double> list) {
					        double result = list.collectDouble(d -> d).sum() + 10.0;
					        if (list.collectDouble(d -> d * 2).sum() > 100.0) {
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
					    double withCast(MutableList<String> list) {
					        return list.sumOfDouble(s -> (double) s.length());
					    }

					    double withLambda(MutableList<Double> list) {
					        return list.sumOfDouble(d -> d * 2);
					    }

					    double withRichIterable(RichIterable<Double> iterable) {
					        return iterable.sumOfDouble(d -> d * 2);
					    }

					    void inExpression(MutableList<Double> list) {
					        double result = list.sumOfDouble(d -> d) + 10.0;
					        if (list.sumOfDouble(d -> d * 2) > 100.0) {
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
					import org.eclipse.collections.api.DoubleIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    double withIntermediateOperations(MutableList<Double> list) {
					        return list.collectDouble(d -> d)
					            .select(i -> i > 5.0)
					            .sum();
					    }

					    DoubleIterable onlyCollectDouble(MutableList<Double> list) {
					        return list.collectDouble(d -> d);
					    }
					}
					"""
				)
			);
	}
}
