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

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Optional<String> streamFilterFindFirst(MutableList<String> list, Predicate<String> predicate) {
					        return list.stream().filter(predicate).findFirst();
					    }

					    Optional<String> withLambdaPredicate(MutableList<String> list) {
					        return list.stream().filter(s -> s.length() > 5).findFirst();
					    }

					    Optional<String> withMethodReferencePredicate(MutableList<String> list) {
					        return list.stream().filter(String::isEmpty).findFirst();
					    }

					    Optional<String> withImmutableList(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.stream().filter(predicate).findFirst();
					    }

					    Optional<Integer> withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.stream().filter(predicate).findFirst();
					    }

					    void inIfPresent(MutableList<String> list) {
					        list.stream().filter(String::isEmpty).findFirst().ifPresent(System.out::println);
					    }

					    String withOrElse(MutableList<String> list) {
					        return list.stream().filter(s -> s.length() > 5).findFirst().orElse("default");
					    }
					}
					""",
					"""
					import java.util.Optional;
					import java.util.function.Predicate;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Optional<String> streamFilterFindFirst(MutableList<String> list, Predicate<String> predicate) {
					        return list.detectOptional(predicate);
					    }

					    Optional<String> withLambdaPredicate(MutableList<String> list) {
					        return list.detectOptional(s -> s.length() > 5);
					    }

					    Optional<String> withMethodReferencePredicate(MutableList<String> list) {
					        return list.detectOptional(String::isEmpty);
					    }

					    Optional<String> withImmutableList(ImmutableList<String> list, Predicate<String> predicate) {
					        return list.detectOptional(predicate);
					    }

					    Optional<Integer> withMutableSet(MutableSet<Integer> set, Predicate<Integer> predicate) {
					        return set.detectOptional(predicate);
					    }

					    void inIfPresent(MutableList<String> list) {
					        list.detectOptional(String::isEmpty).ifPresent(System.out::println);
					    }

					    String withOrElse(MutableList<String> list) {
					        return list.detectOptional(s -> s.length() > 5).orElse("default");
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
					import java.util.Optional;
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> withMultipleIntermediateOperations(MutableList<String> list) {
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .map(String::toUpperCase)
					            .findFirst();
					    }

					    Stream<String> onlyStream(MutableList<String> list) {
					        return list.stream();
					    }

					    Optional<String> onlyFindFirst(MutableList<String> list) {
					        return list.stream().findFirst();
					    }
					}
					"""
				)
			);
	}
}
