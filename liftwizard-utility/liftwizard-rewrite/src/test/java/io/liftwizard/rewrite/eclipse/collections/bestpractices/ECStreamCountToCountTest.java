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

class ECStreamCountToCountTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamCountToCount());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    long streamFilterCount(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().filter(predicate).count();
					    }

					    long withLambdaPredicate(MutableList<String> list) {
					        return list.stream().filter(s -> s.length() > 5).count();
					    }

					    long withMethodReferencePredicate(MutableList<String> list) {
					        return list.stream().filter(String::isEmpty).count();
					    }

					    long withImmutableList(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.stream().filter(predicate).count();
					    }

					    long withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.stream().filter(predicate).count();
					    }

					    void inIfCondition(MutableList<String> list) {
					        if (list.stream().filter(String::isEmpty).count() > 5) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    long streamFilterCount(MutableList<String> list, Predicate<String> predicate) {
					        return list.count(predicate);
					    }

					    long withLambdaPredicate(MutableList<String> list) {
					        return list.count(s -> s.length() > 5);
					    }

					    long withMethodReferencePredicate(MutableList<String> list) {
					        return list.count(String::isEmpty);
					    }

					    long withImmutableList(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.count(predicate);
					    }

					    long withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.count(predicate);
					    }

					    void inIfCondition(MutableList<String> list) {
					        if (list.count(String::isEmpty) > 5) {
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
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    long withMultipleIntermediateOperations(MutableList<String> list) {
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .map(String::toUpperCase)
					            .count();
					    }

					    Stream<String> onlyStream(MutableList<String> list) {
					        return list.stream();
					    }

					    long onlyCount(MutableList<String> list) {
					        return list.stream().count();
					    }
					}
					"""
				)
			);
	}
}
