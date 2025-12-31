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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class JCFTreeSetConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new JCFTreeSetConstructorToFactory());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import java.util.Collection;
					import java.util.Comparator;
					import java.util.List;
					import java.util.SortedSet;
					import java.util.TreeSet;

					class Test {
					    private final SortedSet<String> fieldInterfaceEmpty = new TreeSet<>();
					    private final SortedSet<String> fieldInterfaceComparator = new TreeSet<>(Comparator.naturalOrder());
					    private final SortedSet<String> fieldInterfaceCollection = new TreeSet<>(Arrays.asList("a", "b"));

					    void test(Collection<String> inputCollection) {
					        Collection<String> collection = new TreeSet<>();
					        SortedSet<String> typeInference = new TreeSet<>();
					        SortedSet<List<String>> nestedGenerics = new TreeSet<>();
					        SortedSet<? extends Number> wildcardGenerics = new TreeSet<>();
					        SortedSet<String> explicitSimple = new TreeSet<String>();
					        SortedSet<List<String>> explicitNested = new TreeSet<List<String>>();
					        java.util.SortedSet<String> fullyQualified = new TreeSet<>();
					        SortedSet<String> interfaceWithComparator = new TreeSet<>(Comparator.naturalOrder());
					        SortedSet<String> interfaceFromCollection = new TreeSet<>(inputCollection);
					        SortedSet<String> fromList = new TreeSet<>(Arrays.asList("x", "y", "z"));
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.SortedSets;

					import java.util.Arrays;
					import java.util.Collection;
					import java.util.Comparator;
					import java.util.List;
					import java.util.SortedSet;

					class Test {
					    private final SortedSet<String> fieldInterfaceEmpty = SortedSets.mutable.empty();
					    private final SortedSet<String> fieldInterfaceComparator = SortedSets.mutable.with(Comparator.naturalOrder());
					    private final SortedSet<String> fieldInterfaceCollection = SortedSets.mutable.withAll(Arrays.asList("a", "b"));

					    void test(Collection<String> inputCollection) {
					        Collection<String> collection = SortedSets.mutable.empty();
					        SortedSet<String> typeInference = SortedSets.mutable.empty();
					        SortedSet<List<String>> nestedGenerics = SortedSets.mutable.empty();
					        SortedSet<? extends Number> wildcardGenerics = SortedSets.mutable.empty();
					        SortedSet<String> explicitSimple = SortedSets.mutable.<String>empty();
					        SortedSet<List<String>> explicitNested = SortedSets.mutable.<List<String>>empty();
					        java.util.SortedSet<String> fullyQualified = SortedSets.mutable.empty();
					        SortedSet<String> interfaceWithComparator = SortedSets.mutable.with(Comparator.naturalOrder());
					        SortedSet<String> interfaceFromCollection = SortedSets.mutable.withAll(inputCollection);
					        SortedSet<String> fromList = SortedSets.mutable.withAll(Arrays.asList("x", "y", "z"));
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
					import java.util.Collection;
					import java.util.Comparator;
					import java.util.TreeSet;

					class Test {
					    private final TreeSet<String> fieldConcreteType = new TreeSet<>();

					    void test(Collection<String> inputCollection) {
					        TreeSet<String> diamondSet = new TreeSet<>();
					        TreeSet rawSet = new TreeSet();
					        TreeSet<String> concreteFromCollection = new TreeSet<>(inputCollection);
					        TreeSet<String> concreteWithComparator = new TreeSet<>(Comparator.naturalOrder());
					    }
					}
					"""
				)
			);
	}
}
