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

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.List;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void streamForEachWithForEach(MutableList<String> list) {
					        list.stream().forEach(System.out::println);
					    }

					    void streamForEachWithLambda(MutableList<String> list, List<String> target) {
					        list.stream().forEach(s -> target.add(s));
					    }

					    void streamForEachWithImmutableList(ImmutableList<String> list) {
					        list.stream().forEach(System.out::println);
					    }

					    void streamForEachWithMutableSet(MutableSet<Integer> set, List<Integer> target) {
					        set.stream().forEach(i -> target.add(i * 2));
					    }
					}
					""",
					"""
					import java.util.List;

					import org.eclipse.collections.api.list.ImmutableList;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;

					class Test {
					    void streamForEachWithForEach(MutableList<String> list) {
					        list.forEach(System.out::println);
					    }

					    void streamForEachWithLambda(MutableList<String> list, List<String> target) {
					        list.forEach(s -> target.add(s));
					    }

					    void streamForEachWithImmutableList(ImmutableList<String> list) {
					        list.forEach(System.out::println);
					    }

					    void streamForEachWithMutableSet(MutableSet<Integer> set, List<Integer> target) {
					        set.forEach(i -> target.add(i * 2));
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
					import java.util.stream.Stream;

					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    void withIntermediateOperations(MutableList<String> list) {
					        list.stream()
					            .filter(s -> s.length() > 5)
					            .forEach(System.out::println);
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
