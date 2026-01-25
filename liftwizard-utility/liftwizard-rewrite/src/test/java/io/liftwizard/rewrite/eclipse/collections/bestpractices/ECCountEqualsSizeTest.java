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

class ECCountEqualsSizeTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECCountEqualsSizeRecipes());
	}

	@DocumentExample
	@Test
	void replaceCountEqualsSizeWithAllSatisfy() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean allMatch = list.count(s -> s.length() > 5) == list.size();
					        boolean reversedAllMatch = list.size() == list.count(s -> s.length() > 5);

					        if (list.count(s -> s.length() > 5) == list.size()) {
					            // All satisfy
					        }
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean allMatch = list.allSatisfy(s -> s.length() > 5);
					        boolean reversedAllMatch = list.allSatisfy(s -> s.length() > 5);

					        if (list.allSatisfy(s -> s.length() > 5)) {
					            // All satisfy
					        }
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceOtherPatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, MutableList<String> otherList) {
					        // Different collections - should not transform
					        boolean differentLists = list.count(s -> s.length() > 5) == otherList.size();

					        // Count used standalone - should not transform
					        int countResult = list.count(s -> s.length() > 5);

					        // Count compared to zero - different recipe handles this
					        boolean countEqualsZero = list.count(s -> s.length() > 5) == 0;
					    }
					}
					"""
				)
			);
	}
}
