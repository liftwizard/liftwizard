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

class ECArraysAsListToWithTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new ECArraysAsListToWith());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import org.eclipse.collections.api.list.MutableList;
					import java.util.Arrays;

					class Test {
					    private final MutableList<String> fieldList = FastList.newList(Arrays.asList("a", "b", "c"));

					    void test() {
					        String a = "a";
					        String b = "b";
					        var list = FastList.newList(Arrays.asList("a", "b", "c"));
					        var set = UnifiedSet.newSet(Arrays.asList("a", "b", "c"));
					        var bag = HashBag.newBag(Arrays.asList("a", "b", "c"));
					        var sortedSet = TreeSortedSet.newSet(Arrays.asList("a", "b", "c"));
					        var sortedBag = TreeBag.newBag(Arrays.asList("a", "b", "c"));
					        var singleElement = FastList.newList(Arrays.asList("single"));
					        var numbers = UnifiedSet.newSet(Arrays.asList(1, 2, 3, 4, 5));
					        var variables = FastList.newList(Arrays.asList(a, b));
					        var multipleList = FastList.newList(Arrays.asList("x", "y"));
					        var multipleSet = UnifiedSet.newSet(Arrays.asList(1, 2, 3));
					        var multipleBag = HashBag.newBag(Arrays.asList("p", "q", "r"));
					        var multipleSortedSet = TreeSortedSet.newSet(Arrays.asList("x", "y"));
					        var multipleSortedBag = TreeBag.newBag(Arrays.asList("p", "q", "r"));
					        MutableList<String> typed = FastList.newList(Arrays.asList("d", "e", "f"));
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import org.eclipse.collections.api.factory.Bags;
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.factory.Sets;
					import org.eclipse.collections.api.factory.SortedBags;
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.api.list.MutableList;

					class Test {
					    private final MutableList<String> fieldList = Lists.mutable.with("a", "b", "c");

					    void test() {
					        String a = "a";
					        String b = "b";
					        var list = Lists.mutable.with("a", "b", "c");
					        var set = Sets.mutable.with("a", "b", "c");
					        var bag = Bags.mutable.with("a", "b", "c");
					        var sortedSet = SortedSets.mutable.with("a", "b", "c");
					        var sortedBag = SortedBags.mutable.with("a", "b", "c");
					        var singleElement = Lists.mutable.with("single");
					        var numbers = Sets.mutable.with(1, 2, 3, 4, 5);
					        var variables = Lists.mutable.with(a, b);
					        var multipleList = Lists.mutable.with("x", "y");
					        var multipleSet = Sets.mutable.with(1, 2, 3);
					        var multipleBag = Bags.mutable.with("p", "q", "r");
					        var multipleSortedSet = SortedSets.mutable.with("x", "y");
					        var multipleSortedBag = SortedBags.mutable.with("p", "q", "r");
					        MutableList<String> typed = Lists.mutable.with("d", "e", "f");
					    }
					}
					"""
				)
			);
	}

	@Test
	void replacePatternsFields() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import org.eclipse.collections.api.bag.MutableBag;
					import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;
					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

					class Test {
					    private MutableList<String> fieldList = FastList.newList(Arrays.asList("a", "b", "c"));
					    private final MutableList<String> fieldListFinal = FastList.newList(Arrays.asList("x", "y"));
					    private MutableSet<Integer> fieldSet = UnifiedSet.newSet(Arrays.asList(1, 2, 3));
					    private final MutableSet<String> fieldSetFinal = UnifiedSet.newSet(Arrays.asList("p", "q"));
					    private MutableBag<String> fieldBag = HashBag.newBag(Arrays.asList("one", "two"));
					    private final MutableBag<Integer> fieldBagFinal = HashBag.newBag(Arrays.asList(10, 20));
					    private MutableSortedSet<String> fieldSortedSet = TreeSortedSet.newSet(Arrays.asList("alpha", "beta"));
					    private final MutableSortedSet<String> fieldSortedSetFinal = TreeSortedSet.newSet(Arrays.asList("gamma", "delta"));
					    private MutableSortedBag<String> fieldSortedBag = TreeBag.newBag(Arrays.asList("first", "second"));
					    private final MutableSortedBag<Integer> fieldSortedBagFinal = TreeBag.newBag(Arrays.asList(100, 200));
					    private static MutableList<String> fieldStaticList = FastList.newList(Arrays.asList("static", "list"));
					    private static final MutableSet<String> fieldStaticSetFinal = UnifiedSet.newSet(Arrays.asList("static", "set"));
					    protected MutableList<String> fieldProtectedList = FastList.newList(Arrays.asList("protected", "list"));
					    public MutableSet<String> fieldPublicSet = UnifiedSet.newSet(Arrays.asList("public", "set"));
					    MutableBag<String> fieldPackagePrivateBag = HashBag.newBag(Arrays.asList("package", "bag"));
					    private MutableList<String> fieldSingleElement = FastList.newList(Arrays.asList("single"));
					}
					""",
					"""
					import org.eclipse.collections.api.bag.MutableBag;
					import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
					import org.eclipse.collections.api.factory.Bags;
					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.factory.Sets;
					import org.eclipse.collections.api.factory.SortedBags;
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;
					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

					class Test {
					    private MutableList<String> fieldList = Lists.mutable.with("a", "b", "c");
					    private final MutableList<String> fieldListFinal = Lists.mutable.with("x", "y");
					    private MutableSet<Integer> fieldSet = Sets.mutable.with(1, 2, 3);
					    private final MutableSet<String> fieldSetFinal = Sets.mutable.with("p", "q");
					    private MutableBag<String> fieldBag = Bags.mutable.with("one", "two");
					    private final MutableBag<Integer> fieldBagFinal = Bags.mutable.with(10, 20);
					    private MutableSortedSet<String> fieldSortedSet = SortedSets.mutable.with("alpha", "beta");
					    private final MutableSortedSet<String> fieldSortedSetFinal = SortedSets.mutable.with("gamma", "delta");
					    private MutableSortedBag<String> fieldSortedBag = SortedBags.mutable.with("first", "second");
					    private final MutableSortedBag<Integer> fieldSortedBagFinal = SortedBags.mutable.with(100, 200);
					    private static MutableList<String> fieldStaticList = Lists.mutable.with("static", "list");
					    private static final MutableSet<String> fieldStaticSetFinal = Sets.mutable.with("static", "set");
					    protected MutableList<String> fieldProtectedList = Lists.mutable.with("protected", "list");
					    public MutableSet<String> fieldPublicSet = Sets.mutable.with("public", "set");
					    MutableBag<String> fieldPackagePrivateBag = Bags.mutable.with("package", "bag");
					    private MutableList<String> fieldSingleElement = Lists.mutable.with("single");
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
					import org.eclipse.collections.impl.list.mutable.FastList;
					import java.util.List;

					class Test {
					    void test(List<String> source) {
					        var list = FastList.newList(source);
					    }
					}
					"""
				)
			);
	}

	@Test
	void replacePatternsWithComparator() {
		this.rewriteRun(
				java(
					"""
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import java.util.Arrays;
					import java.util.Comparator;

					class Test {
					    void test() {
					        var sortedSetWithComparator = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c"));
					        var sortedBagWithComparator = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("x", "y", "z"));
					        var singleElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("single"));
					        var singleElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("single"));
					        var twoElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("first", "second"));
					        var twoElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("first", "second"));
					        var fourElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c", "d"));
					        var fourElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("a", "b", "c", "d"));
					        var fiveElementSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c", "d", "e"));
					        var fiveElementBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("a", "b", "c", "d", "e"));
					    }
					}
					""",
					"""
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.api.factory.SortedBags;
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import java.util.Comparator;

					class Test {
					    void test() {
					        var sortedSetWithComparator = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c");
					        var sortedBagWithComparator = SortedBags.mutable.with(Comparator.reverseOrder(), "x", "y", "z");
					        var singleElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "single");
					        var singleElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "single");
					        var twoElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "first", "second");
					        var twoElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "first", "second");
					        var fourElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c", "d");
					        var fourElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "a", "b", "c", "d");
					        var fiveElementSet = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c", "d", "e");
					        var fiveElementBag = SortedBags.mutable.with(Comparator.reverseOrder(), "a", "b", "c", "d", "e");
					    }
					}
					"""
				)
			);
	}

	@Test
	void replacePatternsWithComparatorFields() {
		this.rewriteRun(
				java(
					"""
					import java.util.Arrays;
					import java.util.Comparator;
					import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

					class Test {
					    private MutableSortedSet<String> fieldSortedSetWithComparator = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("a", "b", "c"));
					    private final MutableSortedSet<String> fieldSortedSetWithComparatorFinal = TreeSortedSet.newSet(Comparator.reverseOrder(), Arrays.asList("x", "y", "z"));
					    private MutableSortedBag<String> fieldSortedBagWithComparator = TreeBag.newBag(Comparator.naturalOrder(), Arrays.asList("one", "two"));
					    private final MutableSortedBag<Integer> fieldSortedBagWithComparatorFinal = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList(10, 20, 30));
					    private static MutableSortedSet<String> fieldStaticSortedSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("static", "set"));
					    private static final MutableSortedBag<String> fieldStaticSortedBagFinal = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("static", "bag"));
					    protected MutableSortedSet<String> fieldProtectedSortedSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("protected", "set"));
					    public MutableSortedBag<String> fieldPublicSortedBag = TreeBag.newBag(Comparator.reverseOrder(), Arrays.asList("public", "bag"));
					    MutableSortedSet<String> fieldPackagePrivateSortedSet = TreeSortedSet.newSet(Comparator.naturalOrder(), Arrays.asList("package", "set"));
					}
					""",
					"""
					import java.util.Comparator;
					import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
					import org.eclipse.collections.api.factory.SortedBags;
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;
					import org.eclipse.collections.impl.bag.sorted.mutable.TreeBag;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

					class Test {
					    private MutableSortedSet<String> fieldSortedSetWithComparator = SortedSets.mutable.with(Comparator.naturalOrder(), "a", "b", "c");
					    private final MutableSortedSet<String> fieldSortedSetWithComparatorFinal = SortedSets.mutable.with(Comparator.reverseOrder(), "x", "y", "z");
					    private MutableSortedBag<String> fieldSortedBagWithComparator = SortedBags.mutable.with(Comparator.naturalOrder(), "one", "two");
					    private final MutableSortedBag<Integer> fieldSortedBagWithComparatorFinal = SortedBags.mutable.with(Comparator.reverseOrder(), 10, 20, 30);
					    private static MutableSortedSet<String> fieldStaticSortedSet = SortedSets.mutable.with(Comparator.naturalOrder(), "static", "set");
					    private static final MutableSortedBag<String> fieldStaticSortedBagFinal = SortedBags.mutable.with(Comparator.reverseOrder(), "static", "bag");
					    protected MutableSortedSet<String> fieldProtectedSortedSet = SortedSets.mutable.with(Comparator.naturalOrder(), "protected", "set");
					    public MutableSortedBag<String> fieldPublicSortedBag = SortedBags.mutable.with(Comparator.reverseOrder(), "public", "bag");
					    MutableSortedSet<String> fieldPackagePrivateSortedSet = SortedSets.mutable.with(Comparator.naturalOrder(), "package", "set");
					}
					"""
				)
			);
	}
}
