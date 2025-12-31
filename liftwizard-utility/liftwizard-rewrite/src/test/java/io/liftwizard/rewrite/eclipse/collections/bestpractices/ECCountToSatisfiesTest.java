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

class ECCountToSatisfiesTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECCountToSatisfiesRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean countEqualsZero = list.count(s -> s.length() > 5) == 0;
					        boolean countGreaterThanZero = list.count(s -> s.length() > 5) > 0;
					        boolean countNotEqualsZero = list.count(s -> s.length() > 5) != 0;
					        boolean countLessThanOrEqualZero = list.count(s -> s.length() > 5) <= 0;
					        boolean countGreaterThanOrEqualOne = list.count(s -> s.length() > 5) >= 1;
					        boolean reversedZeroGreaterThanOrEqualCount = 0 >= list.count(s -> s.length() > 5);
					        boolean reversedOneLessThanOrEqualCount = 1 <= list.count(s -> s.length() > 5);
					        boolean reversedZeroLessThanCount = 0 < list.count(s -> s.length() > 5);

					        boolean otherComparison = list.count(s -> s.length() > 5) >= 2;

					        if (list.count(s -> s.length() > 5) == 0) {
					            // None satisfy
					        }
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean countEqualsZero = list.noneSatisfy(s -> s.length() > 5);
					        boolean countGreaterThanZero = list.anySatisfy(s -> s.length() > 5);
					        boolean countNotEqualsZero = list.anySatisfy(s -> s.length() > 5);
					        boolean countLessThanOrEqualZero = list.noneSatisfy(s -> s.length() > 5);
					        boolean countGreaterThanOrEqualOne = list.anySatisfy(s -> s.length() > 5);
					        boolean reversedZeroGreaterThanOrEqualCount = list.noneSatisfy(s -> s.length() > 5);
					        boolean reversedOneLessThanOrEqualCount = list.anySatisfy(s -> s.length() > 5);
					        boolean reversedZeroLessThanCount = list.anySatisfy(s -> s.length() > 5);

					        boolean otherComparison = list.count(s -> s.length() > 5) >= 2;

					        if (list.noneSatisfy(s -> s.length() > 5)) {
					            // None satisfy
					        }
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
					    void test(MutableList<String> list) {
					        boolean otherComparison = list.count(s -> s.length() > 5) >= 2;
					        int countResult = list.count(s -> s.length() > 5);
					    }
					}
					"""
				)
			);
	}
}
