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

class IterateRejectRedundantTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new IterateRejectRedundantRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Collection;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.impl.utility.Iterate;

					class Test {
					    void test(MutableList<String> list, MutableSet<Integer> set, Predicate<String> predicate) {
					        var result1 = Iterate.reject(list, s -> s.length() > 5);
					        var result2 = Iterate.reject(list, predicate);
					        var result3 = Iterate.reject(list, String::isEmpty);
					        var result4 = Iterate.reject(set, i -> i > 0);
					        Collection<String> result5 = Iterate.reject(list, s -> s.length() > 5);
					        Collection<String> result6 = Iterate.reject(list, predicate);
					        Collection<String> result7 = Iterate.reject(list, String::isEmpty);
					        Collection<Integer> result8 = Iterate.reject(set, i -> i > 0);
					    }
					}
					""",
					"""
					import java.util.Collection;
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void test(MutableList<String> list, MutableSet<Integer> set, Predicate<String> predicate) {
					        var result1 = list.reject(s -> s.length() > 5);
					        var result2 = list.reject(predicate);
					        var result3 = list.reject(String::isEmpty);
					        var result4 = set.reject(i -> i > 0);
					        Collection<String> result5 = list.reject(s -> s.length() > 5);
					        Collection<String> result6 = list.reject(predicate);
					        Collection<String> result7 = list.reject(String::isEmpty);
					        Collection<Integer> result8 = set.reject(i -> i > 0);
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
					        var result1 = Iterate.reject(immutableList, String::isEmpty);
					        var result2 = Iterate.reject(javaList, s -> s.length() > 5);
					        var result3 = Iterate.reject(iterable, s -> s.length() > 5);
					        Collection<String> result4 = Iterate.reject(immutableList, String::isEmpty);
					        Collection<String> result5 = Iterate.reject(javaList, s -> s.length() > 5);
					        Collection<String> result6 = Iterate.reject(iterable, s -> s.length() > 5);
					    }
					}
					"""
				)
			);
	}
}
