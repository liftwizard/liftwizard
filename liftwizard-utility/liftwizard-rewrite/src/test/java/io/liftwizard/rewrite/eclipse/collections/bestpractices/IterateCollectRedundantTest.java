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

class IterateCollectRedundantTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new IterateCollectRedundantRecipes());
	}

	@Test
	@DocumentExample
	void replaceIterateCollectWithRichIterableCollect() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(MutableList<String> list) {
					        var result = Iterate.collect(list, String::length);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        var result = list.collect(String::length);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceIterateCollectWithFunctionVariable() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(MutableList<String> list, Function<String, Integer> function) {
					        var result = Iterate.collect(list, function);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.block.function.Function;

					class Test {
					    void test(MutableList<String> list, Function<String, Integer> function) {
					        var result = list.collect(function);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceIterateCollectWithImmutableList() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(ImmutableList<String> list) {
					        var result = Iterate.collect(list, s -> s.toUpperCase());
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    void test(ImmutableList<String> list) {
					        var result = list.collect(s -> s.toUpperCase());
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceIterateCollectWithMutableSet() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(MutableSet<Integer> set) {
					        var result = Iterate.collect(set, i -> i * 2);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void test(MutableSet<Integer> set) {
					        var result = set.collect(i -> i * 2);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceIterateCollectWithJavaList() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(List<String> list) {
					        // Should not replace when argument is a JCF List
					        var result = Iterate.collect(list, String::length);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceIterateCollectWithIterable() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(Iterable<String> iterable) {
					        // Should not replace when argument is a generic Iterable
					        var result = Iterate.collect(iterable, String::length);
					    }
					}
					"""
				)
			);
	}
}
