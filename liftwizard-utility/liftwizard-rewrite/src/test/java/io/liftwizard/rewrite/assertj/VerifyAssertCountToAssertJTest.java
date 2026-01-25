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

package io.liftwizard.rewrite.assertj;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class VerifyAssertCountToAssertJTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec
			.recipe(new VerifyAssertCountToAssertJRecipe())
			.parser(
				JavaParser.fromJavaVersion()
					.dependsOn(
						"""
						package org.eclipse.collections.impl.test;

						import org.eclipse.collections.api.block.predicate.Predicate;

						public final class Verify {
						    public static <T> void assertCount(int expectedCount, Iterable<T> iterable, Predicate<? super T> predicate) {}
						}
						"""
					)
					.classpath("eclipse-collections-api", "eclipse-collections", "assertj-core")
			);
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.test.Verify;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.factory.Lists;

					class Test {
					    void test() {
					        MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
					        Verify.assertCount(2, numbers, each -> each % 2 == 0);

					        MutableList<String> emptyList = Lists.mutable.empty();
					        Verify.assertCount(0, emptyList, s -> s.length() > 0);
					    }
					}
					""",
					"""
					import org.assertj.core.api.Assertions;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.factory.Lists;

					class Test {
					    void test() {
					        MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
					        Assertions.assertThat(numbers).filteredOn(each -> each % 2 == 0).hasSize(2);

					        MutableList<String> emptyList = Lists.mutable.empty();
					        Assertions.assertThat(emptyList).filteredOn(s -> s.length() > 0).hasSize(0);
					    }
					}
					"""
				)
			);
	}
}
