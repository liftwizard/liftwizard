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

class ECStreamMinMaxToMinMaxTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamMinMaxToMinMax());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Comparator;
					import java.util.Optional;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Optional<String> streamMinWithMinOptional(MutableList<String> list) {
					        return list.stream().min(Comparator.naturalOrder());
					    }

					    Optional<String> streamMaxWithMaxOptional(MutableList<String> list) {
					        return list.stream().max(Comparator.naturalOrder());
					    }

					    Optional<String> withCustomComparatorMin(MutableList<String> list) {
					        return list.stream().min(Comparator.comparing(String::length));
					    }

					    Optional<String> withCustomComparatorMax(MutableList<String> list) {
					        return list.stream().max(Comparator.comparing(String::length));
					    }

					    Optional<Integer> withImmutableListMin(ImmutableList<Integer> list) {
					        return list.stream().min(Comparator.naturalOrder());
					    }

					    Optional<Integer> withImmutableListMax(ImmutableList<Integer> list) {
					        return list.stream().max(Comparator.naturalOrder());
					    }

					    Optional<String> withMutableSetMin(MutableSet<String> set) {
					        return set.stream().min(Comparator.naturalOrder());
					    }

					    Optional<String> withMutableSetMax(MutableSet<String> set) {
					        return set.stream().max(Comparator.naturalOrder());
					    }

					    void inIfCondition(MutableList<Integer> list) {
					        Optional<Integer> min = list.stream().min(Comparator.naturalOrder());
					        if (list.stream().max(Comparator.naturalOrder()).isPresent()) {
					            this.doWork();
					        }
					    }

					    void doWork() {}
					}
					""",
					"""
					import java.util.Comparator;
					import java.util.Optional;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    Optional<String> streamMinWithMinOptional(MutableList<String> list) {
					        return list.minOptional(Comparator.naturalOrder());
					    }

					    Optional<String> streamMaxWithMaxOptional(MutableList<String> list) {
					        return list.maxOptional(Comparator.naturalOrder());
					    }

					    Optional<String> withCustomComparatorMin(MutableList<String> list) {
					        return list.minOptional(Comparator.comparing(String::length));
					    }

					    Optional<String> withCustomComparatorMax(MutableList<String> list) {
					        return list.maxOptional(Comparator.comparing(String::length));
					    }

					    Optional<Integer> withImmutableListMin(ImmutableList<Integer> list) {
					        return list.minOptional(Comparator.naturalOrder());
					    }

					    Optional<Integer> withImmutableListMax(ImmutableList<Integer> list) {
					        return list.maxOptional(Comparator.naturalOrder());
					    }

					    Optional<String> withMutableSetMin(MutableSet<String> set) {
					        return set.minOptional(Comparator.naturalOrder());
					    }

					    Optional<String> withMutableSetMax(MutableSet<String> set) {
					        return set.maxOptional(Comparator.naturalOrder());
					    }

					    void inIfCondition(MutableList<Integer> list) {
					        Optional<Integer> min = list.minOptional(Comparator.naturalOrder());
					        if (list.maxOptional(Comparator.naturalOrder()).isPresent()) {
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
					import java.util.Comparator;
					import java.util.List;
					import java.util.Optional;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    Optional<String> withIntermediateOperations(MutableList<String> list) {
					        return list.stream()
					            .filter(s -> s.length() > 3)
					            .min(Comparator.naturalOrder());
					    }

					    Optional<String> jcfListMin(List<String> list) {
					        return list.stream().min(Comparator.naturalOrder());
					    }

					    Optional<String> jcfListMax(List<String> list) {
					        return list.stream().max(Comparator.naturalOrder());
					    }
					}
					"""
				)
			);
	}
}
