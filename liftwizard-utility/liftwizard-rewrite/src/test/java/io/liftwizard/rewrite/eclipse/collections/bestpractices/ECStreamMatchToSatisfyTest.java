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

	@Test
	@DocumentExample
	void replaceStreamAnyMatchWithAnySatisfy() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().anyMatch(predicate);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.anySatisfy(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamAllMatchWithAllSatisfy() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().allMatch(predicate);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.allSatisfy(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamNoneMatchWithNoneSatisfy() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().noneMatch(predicate);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableList<String> list, Predicate<String> predicate) {
					        return list.noneSatisfy(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithLambdaPredicate() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean anyLong = list.stream().anyMatch(s -> s.length() > 5);
					        boolean allLong = list.stream().allMatch(s -> s.length() > 5);
					        boolean noneLong = list.stream().noneMatch(s -> s.length() > 5);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean anyLong = list.anySatisfy(s -> s.length() > 5);
					        boolean allLong = list.allSatisfy(s -> s.length() > 5);
					        boolean noneLong = list.noneSatisfy(s -> s.length() > 5);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithMethodReference() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean anyEmpty = list.stream().anyMatch(String::isEmpty);
					        boolean allEmpty = list.stream().allMatch(String::isEmpty);
					        boolean noneEmpty = list.stream().noneMatch(String::isEmpty);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        boolean anyEmpty = list.anySatisfy(String::isEmpty);
					        boolean allEmpty = list.allSatisfy(String::isEmpty);
					        boolean noneEmpty = list.noneSatisfy(String::isEmpty);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithImmutableList() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.stream().anyMatch(predicate);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;
					import java.util.function.Predicate;

					class Test {
					    boolean test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.anySatisfy(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithMutableSet() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.set.MutableSet;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.stream().anyMatch(predicate);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.set.MutableSet;
					import java.util.function.Predicate;

					class Test {
					    boolean test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.anySatisfy(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceInIfCondition() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        if (list.stream().anyMatch(String::isEmpty)) {
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
	void doNotReplaceStreamWithIntermediateOperations() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    boolean test(MutableList<String> list) {
					        // Should not replace when there are intermediate operations
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .anyMatch(String::isEmpty);
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceWhenOnlyStream() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;
					import java.util.stream.Stream;

					class Test {
					    Stream<String> test(MutableList<String> list) {
					        // Should not replace when we only call stream without anyMatch/allMatch/noneMatch
					        return list.stream();
					    }
					}
					"""
				)
			);
	}
}
