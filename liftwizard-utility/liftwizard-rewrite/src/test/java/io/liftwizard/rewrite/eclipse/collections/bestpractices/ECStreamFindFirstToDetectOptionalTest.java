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

class ECStreamFindFirstToDetectOptionalTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamFindFirstToDetectOptional());
	}

	@Test
	@DocumentExample
	void replaceStreamFilterFindFirstWithDetectOptional() {
		this.rewriteRun(
				java(
					"""
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().filter(predicate).findFirst();
					    }
					}
					""",
					"""
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list, Predicate<String> predicate) {
					        return list.detectOptional(predicate);
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
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list) {
					        return list.stream().filter(s -> s.length() > 5).findFirst();
					    }
					}
					""",
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list) {
					        return list.detectOptional(s -> s.length() > 5);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithMethodReferencePredicate() {
		this.rewriteRun(
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list) {
					        return list.stream().filter(String::isEmpty).findFirst();
					    }
					}
					""",
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list) {
					        return list.detectOptional(String::isEmpty);
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
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    Optional<String> test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.stream().filter(predicate).findFirst();
					    }
					}
					""",
					"""
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    Optional<String> test(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.detectOptional(predicate);
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
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Optional<Integer> test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.stream().filter(predicate).findFirst();
					    }
					}
					""",
					"""
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Optional<Integer> test(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.detectOptional(predicate);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceInIfPresent() {
		this.rewriteRun(
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        list.stream().filter(String::isEmpty).findFirst().ifPresent(System.out::println);
					    }
					}
					""",
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        list.detectOptional(String::isEmpty).ifPresent(System.out::println);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceWithOrElse() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    String test(MutableList<String> list) {
					        return list.stream().filter(s -> s.length() > 5).findFirst().orElse("default");
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    String test(MutableList<String> list) {
					        return list.detectOptional(s -> s.length() > 5).orElse("default");
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceWithMultipleIntermediateOperations() {
		this.rewriteRun(
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list) {
					        // Should not replace when there are multiple intermediate operations
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .map(String::toUpperCase)
					            .findFirst();
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
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Stream<String> test(MutableList<String> list) {
					        // Should not replace when we only call stream without filter+findFirst
					        return list.stream();
					    }
					}
					"""
				)
			);
	}

	@Test
	void doNotReplaceWhenOnlyFindFirst() {
		this.rewriteRun(
				java(
					"""
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> test(MutableList<String> list) {
					        // Should not replace when only findFirst without filter
					        return list.stream().findFirst();
					    }
					}
					"""
				)
			);
	}
}
