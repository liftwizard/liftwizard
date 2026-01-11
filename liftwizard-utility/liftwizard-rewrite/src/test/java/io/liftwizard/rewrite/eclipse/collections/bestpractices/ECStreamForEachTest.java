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

class ECStreamForEachTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECStreamForEachRecipes());
	}

	@Test
	@DocumentExample
	void replaceStreamForEachWithForEach() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        list.stream().forEach(System.out::println);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list) {
					        list.forEach(System.out::println);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamForEachWithLambda() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, java.util.List<String> target) {
					        list.stream().forEach(s -> target.add(s));
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void test(MutableList<String> list, java.util.List<String> target) {
					        list.forEach(s -> target.add(s));
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamForEachWithImmutableList() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    void test(ImmutableList<String> list) {
					        list.stream().forEach(System.out::println);
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.list.ImmutableList;

					class Test {
					    void test(ImmutableList<String> list) {
					        list.forEach(System.out::println);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replaceStreamForEachWithMutableSet() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.api.set.MutableSet;
					import java.util.List;

					class Test {
					    void test(MutableSet<Integer> set, List<Integer> target) {
					        set.stream().forEach(i -> target.add(i * 2));
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.set.MutableSet;
					import java.util.List;

					class Test {
					    void test(MutableSet<Integer> set, List<Integer> target) {
					        set.forEach(i -> target.add(i * 2));
					    }
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
					    void test(MutableList<String> list) {
					        // Should not replace when there are intermediate operations
					        list.stream()
					            .filter(s -> s.length() > 5)
					            .forEach(System.out::println);
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
					        // Should not replace when we only call stream without forEach
					        return list.stream();
					    }
					}
					"""
				)
			);
	}
}
