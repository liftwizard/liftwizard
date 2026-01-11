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

class ECNoneSatisfyPredicatesNotToAnySatisfyTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECNoneSatisfyPredicatesNotToAnySatisfyRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.block.factory.Predicates;
					import org.eclipse.collections.api.block.predicate.Predicate;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate) {
					        boolean noneSatisfyPredicatesNot = list.noneSatisfy(Predicates.not(predicate));
					        boolean noneSatisfyPredicatesNotLambda = list.noneSatisfy(Predicates.not(s -> s.length() > 5));
					        boolean noneSatisfyPredicatesNotMethodRef = list.noneSatisfy(Predicates.not(String::isEmpty));

					        if (list.noneSatisfy(Predicates.not(s -> s.isEmpty()))) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.block.predicate.Predicate;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate) {
					        boolean noneSatisfyPredicatesNot = list.anySatisfy(predicate);
					        boolean noneSatisfyPredicatesNotLambda = list.anySatisfy(s -> s.length() > 5);
					        boolean noneSatisfyPredicatesNotMethodRef = list.anySatisfy(String::isEmpty);

					        if (list.anySatisfy(s -> s.isEmpty())) {
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
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.impl.block.factory.Predicates;
					import org.eclipse.collections.api.block.predicate.Predicate;

					class Test {
					    void test(MutableList<String> list, Predicate<String> predicate) {
					        boolean nonNegatedNoneSatisfy = list.noneSatisfy(predicate);
					        boolean withOtherPredicateMethod = list.noneSatisfy(Predicates.alwaysTrue());
					    }
					}
					"""
				)
			);
	}
}
