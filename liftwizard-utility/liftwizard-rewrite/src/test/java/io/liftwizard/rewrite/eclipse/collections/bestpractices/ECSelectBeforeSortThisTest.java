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

class ECSelectBeforeSortThisTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSelectBeforeSortThisRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Comparator;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> sortThisSelect(MutableList<String> list) {
					        return list.sortThis().select(s -> s.length() > 3);
					    }

					    MutableList<String> sortThisWithComparatorSelect(MutableList<String> list) {
					        return list.sortThis(Comparator.comparing(String::length)).select(s -> s.length() > 3);
					    }

					    MutableList<String> sortThisReject(MutableList<String> list) {
					        return list.sortThis().reject(s -> s.isEmpty());
					    }

					    MutableList<String> sortThisWithComparatorReject(MutableList<String> list) {
					        return list.sortThis(Comparator.reverseOrder()).reject(String::isEmpty);
					    }

					    void multiplePatterns(MutableList<String> list1, MutableList<Integer> list2) {
					        MutableList<String> result1 = list1.sortThis().select(s -> s.length() > 3);
					        MutableList<Integer> result2 = list2.sortThis(Comparator.naturalOrder()).reject(i -> i < 0);
					    }
					}
					""",
					"""
					import java.util.Comparator;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> sortThisSelect(MutableList<String> list) {
					        return list.select(s -> s.length() > 3).sortThis();
					    }

					    MutableList<String> sortThisWithComparatorSelect(MutableList<String> list) {
					        return list.select(s -> s.length() > 3).sortThis(Comparator.comparing(String::length));
					    }

					    MutableList<String> sortThisReject(MutableList<String> list) {
					        return list.reject(s -> s.isEmpty()).sortThis();
					    }

					    MutableList<String> sortThisWithComparatorReject(MutableList<String> list) {
					        return list.reject(String::isEmpty).sortThis(Comparator.reverseOrder());
					    }

					    void multiplePatterns(MutableList<String> list1, MutableList<Integer> list2) {
					        MutableList<String> result1 = list1.select(s -> s.length() > 3).sortThis();
					        MutableList<Integer> result2 = list2.reject(i -> i < 0).sortThis(Comparator.naturalOrder());
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
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    MutableList<String> selectBeforeSortThis(MutableList<String> list) {
					        return list.select(s -> s.length() > 3).sortThis();
					    }

					    MutableList<String> sortThisAlone(MutableList<String> list) {
					        return list.sortThis();
					    }

					    MutableList<Integer> sortThisFollowedByCollect(MutableList<String> list) {
					        return list.sortThis().collect(String::length);
					    }
					}
					"""
				)
			);
	}
}
