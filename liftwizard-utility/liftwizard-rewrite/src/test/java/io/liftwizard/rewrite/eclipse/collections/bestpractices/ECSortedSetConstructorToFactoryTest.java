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

class ECSortedSetConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECSortedSetConstructorToFactory());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;
					import java.util.Comparator;
					import java.util.List;

					class Test<T extends Comparable<T>> {
					    private final MutableSortedSet<String> fieldInterfaceEmpty = new TreeSortedSet<>();
					    private final MutableSortedSet<String> fieldInterfaceComparator = new TreeSortedSet<>(Comparator.naturalOrder());
					    private final MutableSortedSet<String> fieldInterfaceIterable = new TreeSortedSet<>(Comparator.naturalOrder(), fieldInterfaceEmpty);

					    void test() {
					        MutableSortedSet<String> diamondSet = new TreeSortedSet<>();
					        MutableSortedSet<String> explicitSimple = new TreeSortedSet<String>();
					        MutableSortedSet<List<String>> explicitNested = new TreeSortedSet<List<String>>();
					        MutableSortedSet<MutableSortedSet<T>> nestedTypeParam = new TreeSortedSet<MutableSortedSet<T>>();
					        org.eclipse.collections.api.set.sorted.MutableSortedSet<String> fullyQualified = new org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet<>();
					        MutableSortedSet<String> withComparator = new TreeSortedSet<>(Comparator.naturalOrder());
					        MutableSortedSet<String> withComparatorAndIterable = new TreeSortedSet<>(Comparator.reverseOrder(), diamondSet);
					    }
					}

					class A<T extends Comparable<T>> {
					    @Override
					    public MutableSortedSet<T> newEmpty() {
					        return new TreeSortedSet<>();
					    }
					}
					""",
					"""
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;

					import java.util.Comparator;
					import java.util.List;

					class Test<T extends Comparable<T>> {
					    private final MutableSortedSet<String> fieldInterfaceEmpty = SortedSets.mutable.empty();
					    private final MutableSortedSet<String> fieldInterfaceComparator = SortedSets.mutable.with(Comparator.naturalOrder());
					    private final MutableSortedSet<String> fieldInterfaceIterable = SortedSets.mutable.withAll(Comparator.naturalOrder(), fieldInterfaceEmpty);

					    void test() {
					        MutableSortedSet<String> diamondSet = SortedSets.mutable.empty();
					        MutableSortedSet<String> explicitSimple = SortedSets.mutable.<String>empty();
					        MutableSortedSet<List<String>> explicitNested = SortedSets.mutable.<List<String>>empty();
					        MutableSortedSet<MutableSortedSet<T>> nestedTypeParam = SortedSets.mutable.<MutableSortedSet<T>>empty();
					        org.eclipse.collections.api.set.sorted.MutableSortedSet<String> fullyQualified = SortedSets.mutable.empty();
					        MutableSortedSet<String> withComparator = SortedSets.mutable.with(Comparator.naturalOrder());
					        MutableSortedSet<String> withComparatorAndIterable = SortedSets.mutable.withAll(Comparator.reverseOrder(), diamondSet);
					    }
					}

					class A<T extends Comparable<T>> {
					    @Override
					    public MutableSortedSet<T> newEmpty() {
					        return SortedSets.mutable.empty();
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
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import java.util.Comparator;
					import java.util.HashSet;
					import java.util.Set;

					class Test {
					    private final TreeSortedSet<String> fieldConcreteType = new TreeSortedSet<>();

					    void test() {
					        Set<Integer> regularSet = new HashSet<>();
					        TreeSortedSet<Integer> concreteTypeEmpty = new TreeSortedSet<>();
					        TreeSortedSet<Integer> concreteTypeComparator = new TreeSortedSet<>(Comparator.naturalOrder());
					        TreeSortedSet<Integer> concreteTypeSet = new TreeSortedSet<>(regularSet);
					    }
					}
					"""
				)
			);
	}
}
