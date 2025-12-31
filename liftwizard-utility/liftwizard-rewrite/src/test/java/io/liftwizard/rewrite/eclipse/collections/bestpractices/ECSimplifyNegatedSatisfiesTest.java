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

class ECSimplifyNegatedSatisfiesTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSimplifyNegatedSatisfiesRecipes());
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
					        boolean negatedNoneSatisfy = !list.noneSatisfy(s -> s.length() > 5);
					        boolean negatedAnySatisfy = !list.anySatisfy(s -> s.length() > 5);
					        boolean negatedNoneSatisfyMethodRef = !list.noneSatisfy(String::isEmpty);
					        boolean negatedAnySatisfyMethodRef = !list.anySatisfy(String::isEmpty);

					        if (!list.noneSatisfy(s -> s.isEmpty())) {
					            this.doWork();
					        }

					        if (!list.anySatisfy(s -> s.startsWith("a"))) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean negatedNoneSatisfy = list.anySatisfy(s -> s.length() > 5);
					        boolean negatedAnySatisfy = list.noneSatisfy(s -> s.length() > 5);
					        boolean negatedNoneSatisfyMethodRef = list.anySatisfy(String::isEmpty);
					        boolean negatedAnySatisfyMethodRef = list.noneSatisfy(String::isEmpty);

					        if (list.anySatisfy(s -> s.isEmpty())) {
					            this.doWork();
					        }

					        if (list.noneSatisfy(s -> s.startsWith("a"))) {
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
					import org.eclipse.collections.api.RichIterable;
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    void nonNegatedCalls(MutableList<String> list) {
					        boolean nonNegatedAnySatisfy = list.anySatisfy(s -> s.length() > 5);
					        boolean nonNegatedNoneSatisfy = list.noneSatisfy(s -> s.isEmpty());
					        boolean combined = list.anySatisfy(s -> s.length() > 5) || list.noneSatisfy(s -> s.isEmpty());
					    }

					    class NoneSatisfyImplementation implements RichIterable<String> {
					        @Override
					        public boolean noneSatisfy(Predicate<? super String> predicate) {
					            return !this.anySatisfy(predicate);
					        }

					        @Override
					        public boolean anySatisfy(Predicate<? super String> predicate) {
					            return false;
					        }
					    }

					    class AnySatisfyImplementation implements RichIterable<String> {
					        @Override
					        public boolean anySatisfy(Predicate<? super String> predicate) {
					            return !this.noneSatisfy(predicate);
					        }

					        @Override
					        public boolean noneSatisfy(Predicate<? super String> predicate) {
					            return true;
					        }
					    }

					    class CircularImplementation implements RichIterable<String> {
					        @Override
					        public boolean noneSatisfy(Predicate<? super String> predicate) {
					            return !this.anySatisfy(predicate);
					        }

					        @Override
					        public boolean anySatisfy(Predicate<? super String> predicate) {
					            return !this.noneSatisfy(predicate);
					        }
					    }
					}
					"""
				)
			);
	}
}
