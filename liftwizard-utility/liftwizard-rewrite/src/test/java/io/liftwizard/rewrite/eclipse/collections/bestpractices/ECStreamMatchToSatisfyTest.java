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

class ECStreamMatchToSatisfyTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamMatchToSatisfy());
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
					    boolean anyMatchToAnySatisfy(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().anyMatch(predicate);
					    }

					    boolean allMatchToAllSatisfy(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().allMatch(predicate);
					    }

					    boolean noneMatchToNoneSatisfy(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().noneMatch(predicate);
					    }

					    void withLambdaPredicate(MutableList<String> list) {
					        boolean anyLong = list.stream().anyMatch(s -> s.length() > 5);
					        boolean allLong = list.stream().allMatch(s -> s.length() > 5);
					        boolean noneLong = list.stream().noneMatch(s -> s.length() > 5);
					    }

					    void withMethodReference(MutableList<String> list) {
					        boolean anyEmpty = list.stream().anyMatch(String::isEmpty);
					        boolean allEmpty = list.stream().allMatch(String::isEmpty);
					        boolean noneEmpty = list.stream().noneMatch(String::isEmpty);
					    }

					    boolean withImmutableList(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.stream().anyMatch(predicate);
					    }

					    boolean withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.stream().anyMatch(predicate);
					    }

					    void inIfCondition(MutableList<String> list) {
					        if (list.stream().anyMatch(String::isEmpty)) {
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
					    boolean anyMatchToAnySatisfy(MutableList<String> list, Predicate<String> predicate) {
					        return list.anySatisfy(predicate);
					    }

					    boolean allMatchToAllSatisfy(MutableList<String> list, Predicate<String> predicate) {
					        return list.allSatisfy(predicate);
					    }

					    boolean noneMatchToNoneSatisfy(MutableList<String> list, Predicate<String> predicate) {
					        return list.noneSatisfy(predicate);
					    }

					    void withLambdaPredicate(MutableList<String> list) {
					        boolean anyLong = list.anySatisfy(s -> s.length() > 5);
					        boolean allLong = list.allSatisfy(s -> s.length() > 5);
					        boolean noneLong = list.noneSatisfy(s -> s.length() > 5);
					    }

					    void withMethodReference(MutableList<String> list) {
					        boolean anyEmpty = list.anySatisfy(String::isEmpty);
					        boolean allEmpty = list.allSatisfy(String::isEmpty);
					        boolean noneEmpty = list.noneSatisfy(String::isEmpty);
					    }

					    boolean withImmutableList(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.anySatisfy(predicate);
					    }

					    boolean withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.anySatisfy(predicate);
					    }

					    void inIfCondition(MutableList<String> list) {
					        if (list.anySatisfy(String::isEmpty)) {
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
					    boolean withIntermediateOperations(MutableList<String> list) {
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .anyMatch(String::isEmpty);
					    }

					    Stream<String> onlyStream(MutableList<String> list) {
					        return list.stream();
					    }
					}
					"""
				)
			);
	}
}
