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

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Collection;
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(MutableList<String> list, MutableSet<Integer> set, Function<String, Integer> function) {
					        var result1 = Iterate.collect(list, String::length);
					        var result2 = Iterate.collect(list, function);
					        var result3 = Iterate.collect(list, s -> s.toUpperCase());
					        var result4 = Iterate.collect(set, i -> i * 2);
					        Collection<Integer> result5 = Iterate.collect(list, String::length);
					        Collection<Integer> result6 = Iterate.collect(list, function);
					        Collection<String> result7 = Iterate.collect(list, s -> s.toUpperCase());
					        Collection<Integer> result8 = Iterate.collect(set, i -> i * 2);
					    }
					}
					""",
					"""
					import java.util.Collection;
					import org.eclipse.collections.api.block.function.Function;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void test(MutableList<String> list, MutableSet<Integer> set, Function<String, Integer> function) {
					        var result1 = list.collect(String::length);
					        var result2 = list.collect(function);
					        var result3 = list.collect(s -> s.toUpperCase());
					        var result4 = set.collect(i -> i * 2);
					        Collection<Integer> result5 = list.collect(String::length);
					        Collection<Integer> result6 = list.collect(function);
					        Collection<String> result7 = list.collect(s -> s.toUpperCase());
					        Collection<Integer> result8 = set.collect(i -> i * 2);
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
					import java.util.Collection;
					import java.util.List;
					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(ImmutableList<String> immutableList, List<String> javaList, Iterable<String> iterable) {
					        var result1 = Iterate.collect(immutableList, s -> s.toUpperCase());
					        var result2 = Iterate.collect(javaList, String::length);
					        var result3 = Iterate.collect(iterable, String::length);
					        Collection<String> result4 = Iterate.collect(immutableList, s -> s.toUpperCase());
					        Collection<Integer> result5 = Iterate.collect(javaList, String::length);
					        Collection<Integer> result6 = Iterate.collect(iterable, String::length);
					    }
					}
					"""
				)
			);
	}
}
