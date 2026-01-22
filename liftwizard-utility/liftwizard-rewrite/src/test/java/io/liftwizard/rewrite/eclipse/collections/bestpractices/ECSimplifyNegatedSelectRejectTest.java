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

class ECSimplifyNegatedSelectRejectTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSimplifyNegatedSelectReject());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> stringList, MutableList<Integer> intList, RichIterable<String> iterable) {
					        // select with negated lambda -> reject
					        MutableList<String> negatedLambda = stringList.select(s -> !s.isEmpty());

					        // select with != comparison -> reject with ==
					        MutableList<Integer> notEqual = intList.select(n -> n != 0);

					        // reject with negated lambda -> select
					        MutableList<String> rejectNegated = stringList.reject(s -> !s.isEmpty());

					        // reject with != comparison -> select with ==
					        MutableList<Integer> rejectNotEqual = intList.reject(n -> n != 0);

					        // select with negated method calls
					        MutableList<String> lengthCheck = stringList.select(s -> !(s.length() > 5));
					        MutableList<String> contains = stringList.select(s -> !s.contains("x"));

					        // works with RichIterable type
					        RichIterable<String> richResult = iterable.select(s -> !s.isEmpty());

					        // works in chained calls
					        MutableList<String> chained = stringList
					            .select(s -> !s.isEmpty())
					            .reject(s -> !s.startsWith("a"));

					        // works in if condition
					        if (stringList.select(s -> !s.isEmpty()).notEmpty()) {
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
					    void test(MutableList<String> stringList, MutableList<Integer> intList, RichIterable<String> iterable) {
					        // select with negated lambda -> reject
					        MutableList<String> negatedLambda = stringList.reject(s -> s.isEmpty());

					        // select with != comparison -> reject with ==
					        MutableList<Integer> notEqual = intList.reject(n -> n == 0);

					        // reject with negated lambda -> select
					        MutableList<String> rejectNegated = stringList.select(s -> s.isEmpty());

					        // reject with != comparison -> select with ==
					        MutableList<Integer> rejectNotEqual = intList.select(n -> n == 0);

					        // select with negated method calls
					        MutableList<String> lengthCheck = stringList.reject(s -> s.length() > 5);
					        MutableList<String> contains = stringList.reject(s -> s.contains("x"));

					        // works with RichIterable type
					        RichIterable<String> richResult = iterable.reject(s -> s.isEmpty());

					        // works in chained calls
					        MutableList<String> chained = stringList
					            .reject(s -> s.isEmpty())
					            .select(s -> s.startsWith("a"));

					        // works in if condition
					        if (stringList.reject(s -> s.isEmpty()).notEmpty()) {
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
					import org.eclipse.collections.api.block.predicate.Predicate;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> stringList, MutableList<Integer> intList, Predicate<String> predicate) {
					        // non-negated lambdas should not change
					        MutableList<String> selectResult = stringList.select(s -> s.isEmpty());
					        MutableList<String> rejectResult = stringList.reject(s -> s.isEmpty());
					        MutableList<String> lengthCheck = stringList.select(s -> s.length() > 5);

					        // method references should not change
					        MutableList<String> selectWithMethodRef = stringList.select(String::isEmpty);
					        MutableList<String> selectWithPredicate = stringList.select(predicate);

					        // == comparison should not change
					        MutableList<Integer> equalComparison = intList.select(n -> n == 0);

					        // other comparisons should not change
					        MutableList<Integer> lt = intList.select(n -> n < 5);
					        MutableList<Integer> gt = intList.select(n -> n > 5);
					        MutableList<Integer> le = intList.select(n -> n <= 5);
					        MutableList<Integer> ge = intList.select(n -> n >= 5);
					    }
					}
					"""
				)
			);
	}
}
