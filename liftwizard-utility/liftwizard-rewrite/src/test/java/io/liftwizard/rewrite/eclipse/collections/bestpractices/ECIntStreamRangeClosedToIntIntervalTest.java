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

class ECIntStreamRangeClosedToIntIntervalTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECIntStreamRangeClosedToIntInterval());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.stream.IntStream;

					class Test {
					    void test(int from, int to, int n) {
					        IntStream.rangeClosed(1, 5).forEach(System.out::println);
					        long result = IntStream.rangeClosed(1, 100).sum();
					        IntStream.rangeClosed(from, to).forEach(System.out::println);
					        IntStream.rangeClosed(n + 1, n * 2).forEach(System.out::println);
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.primitive.IntInterval;

					class Test {
					    void test(int from, int to, int n) {
					        IntInterval.fromTo(1, 5).forEach(System.out::println);
					        long result = IntInterval.fromTo(1, 100).sum();
					        IntInterval.fromTo(from, to).forEach(System.out::println);
					        IntInterval.fromTo(n + 1, n * 2).forEach(System.out::println);
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
					import java.util.stream.IntStream;

					class Test {
					    void test() {
					        IntStream.range(1, 10).forEach(System.out::println);
					        IntStream stream = IntStream.of(1, 2, 3);
					    }
					}
					"""
				)
			);
	}
}
