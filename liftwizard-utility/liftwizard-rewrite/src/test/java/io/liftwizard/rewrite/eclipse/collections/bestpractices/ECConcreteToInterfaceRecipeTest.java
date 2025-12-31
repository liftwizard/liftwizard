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

class ECConcreteToInterfaceRecipeTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipes(
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.list.mutable.FastList",
				"org.eclipse.collections.api.list.MutableList"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.mutable.UnifiedSet",
				"org.eclipse.collections.api.set.MutableSet"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.map.mutable.UnifiedMap",
				"org.eclipse.collections.api.map.MutableMap"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.bag.mutable.HashBag",
				"org.eclipse.collections.api.bag.MutableBag"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet",
				"org.eclipse.collections.api.set.sorted.MutableSortedSet"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap",
				"org.eclipse.collections.api.map.sorted.MutableSortedMap"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.stack.mutable.ArrayStack",
				"org.eclipse.collections.api.stack.MutableStack"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.map.mutable.ConcurrentHashMap",
				"org.eclipse.collections.api.map.MutableMap"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet",
				"org.eclipse.collections.api.set.MutableSet"
			),
			new ECConcreteToInterfaceRecipe(
				"org.eclipse.collections.impl.set.sorted.mutable.SortedSetAdapter",
				"org.eclipse.collections.api.set.sorted.MutableSortedSet"
			)
		);
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Map;

					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
					import org.eclipse.collections.impl.map.mutable.UnifiedMap;
					import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
					import org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.set.sorted.mutable.SortedSetAdapter;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.impl.stack.mutable.ArrayStack;

					class Test {
					    private final FastList<String> fieldList = FastList.newList();
					    private final UnifiedSet<String> fieldSet = UnifiedSet.newSet();
					    private final UnifiedMap<String, Integer> fieldMap = UnifiedMap.newMap();
					    private final HashBag<String> fieldBag = HashBag.newBag();
					    private final TreeSortedSet<String> fieldSortedSet = TreeSortedSet.newSet();
					    private final TreeSortedMap<String, Integer> fieldSortedMap = TreeSortedMap.newMap();
					    private final ArrayStack<String> fieldStack = ArrayStack.newStack();
					    private final ConcurrentHashMap<String, Integer> fieldConcurrentMap = ConcurrentHashMap.newMap();
					    private final MultiReaderUnifiedSet<String> fieldMultiReaderSet = MultiReaderUnifiedSet.newSet();
					    private final SortedSetAdapter<String> fieldSortedSetAdapter = getSortedSetAdapter();

					    void testSimpleGeneric() {
					        FastList<String> simpleList = FastList.newList();
					        UnifiedSet<String> simpleSet = UnifiedSet.newSet();
					        UnifiedMap<String, Integer> simpleMap = UnifiedMap.newMap();
					        HashBag<String> simpleBag = HashBag.newBag();
					        TreeSortedSet<String> simpleSortedSet = TreeSortedSet.newSet();
					        TreeSortedMap<String, Integer> simpleSortedMap = TreeSortedMap.newMap();
					        ArrayStack<String> simpleStack = ArrayStack.newStack();
					        ConcurrentHashMap<String, Integer> simpleConcurrentMap = ConcurrentHashMap.newMap();
					        MultiReaderUnifiedSet<String> simpleMultiReaderSet = MultiReaderUnifiedSet.newSet();
					        SortedSetAdapter<String> simpleSortedSetAdapter = getSortedSetAdapter();
					    }

					    void testFullyQualified() {
					        org.eclipse.collections.impl.list.mutable.FastList<String> fullyQualifiedList = FastList.newList();
					        org.eclipse.collections.impl.set.mutable.UnifiedSet<String> fullyQualifiedSet = UnifiedSet.newSet();
					        org.eclipse.collections.impl.map.mutable.UnifiedMap<String, Integer> fullyQualifiedMap = UnifiedMap.newMap();
					        org.eclipse.collections.impl.bag.mutable.HashBag<String> fullyQualifiedBag = HashBag.newBag();
					        org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet<String> fullyQualifiedSortedSet = TreeSortedSet.newSet();
					        org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap<String, Integer> fullyQualifiedSortedMap = TreeSortedMap.newMap();
					        org.eclipse.collections.impl.stack.mutable.ArrayStack<String> fullyQualifiedStack = ArrayStack.newStack();
					        org.eclipse.collections.impl.map.mutable.ConcurrentHashMap<String, Integer> fullyQualifiedConcurrentMap = ConcurrentHashMap.newMap();
					        org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet<String> fullyQualifiedMultiReaderSet = MultiReaderUnifiedSet.newSet();
					    }

					    void testRawTypes() {
					        FastList rawList = FastList.newList();
					        UnifiedSet rawSet = UnifiedSet.newSet();
					        UnifiedMap rawMap = UnifiedMap.newMap();
					        HashBag rawBag = HashBag.newBag();
					        TreeSortedSet rawSortedSet = TreeSortedSet.newSet();
					        TreeSortedMap rawSortedMap = TreeSortedMap.newMap();
					        ArrayStack rawStack = ArrayStack.newStack();
					        ConcurrentHashMap rawConcurrentMap = ConcurrentHashMap.newMap();
					        MultiReaderUnifiedSet rawMultiReaderSet = MultiReaderUnifiedSet.newSet();
					    }

					    void testWildcardGenerics() {
					        FastList<? extends Number> wildcardList = FastList.newList();
					        UnifiedSet<? extends Number> wildcardSet = UnifiedSet.newSet();
					        UnifiedMap<String, ? extends Number> wildcardMap = UnifiedMap.newMap();
					        HashBag<? extends Number> wildcardBag = HashBag.newBag();
					        TreeSortedSet<? extends Number> wildcardSortedSet = TreeSortedSet.newSet();
					        TreeSortedMap<String, ? extends Number> wildcardSortedMap = TreeSortedMap.newMap();
					        ArrayStack<? extends Number> wildcardStack = ArrayStack.newStack();
					    }

					    void testNestedGenerics() {
					        FastList<Map<String, Integer>> nestedGenericsList = FastList.newList();
					    }

					    void testMultipleDeclarations() {
					        FastList<String> list1 = FastList.newList(), list2 = FastList.newListWith("a", "b");
					    }

					    private SortedSetAdapter<String> getSortedSetAdapter() {
					        return null;
					    }
					}
					""",
					"""
					import java.util.Map;

					import org.eclipse.collections.api.bag.MutableBag;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.map.MutableMap;
					import org.eclipse.collections.api.map.sorted.MutableSortedMap;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;
					import org.eclipse.collections.api.stack.MutableStack;
					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
					import org.eclipse.collections.impl.map.mutable.UnifiedMap;
					import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
					import org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.set.sorted.mutable.SortedSetAdapter;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.impl.stack.mutable.ArrayStack;

					class Test {
					    private final MutableList<String> fieldList = FastList.newList();
					    private final MutableSet<String> fieldSet = UnifiedSet.newSet();
					    private final MutableMap<String, Integer> fieldMap = UnifiedMap.newMap();
					    private final MutableBag<String> fieldBag = HashBag.newBag();
					    private final MutableSortedSet<String> fieldSortedSet = TreeSortedSet.newSet();
					    private final MutableSortedMap<String, Integer> fieldSortedMap = TreeSortedMap.newMap();
					    private final MutableStack<String> fieldStack = ArrayStack.newStack();
					    private final MutableMap<String, Integer> fieldConcurrentMap = ConcurrentHashMap.newMap();
					    private final MutableSet<String> fieldMultiReaderSet = MultiReaderUnifiedSet.newSet();
					    private final MutableSortedSet<String> fieldSortedSetAdapter = getSortedSetAdapter();

					    void testSimpleGeneric() {
					        MutableList<String> simpleList = FastList.newList();
					        MutableSet<String> simpleSet = UnifiedSet.newSet();
					        MutableMap<String, Integer> simpleMap = UnifiedMap.newMap();
					        MutableBag<String> simpleBag = HashBag.newBag();
					        MutableSortedSet<String> simpleSortedSet = TreeSortedSet.newSet();
					        MutableSortedMap<String, Integer> simpleSortedMap = TreeSortedMap.newMap();
					        MutableStack<String> simpleStack = ArrayStack.newStack();
					        MutableMap<String, Integer> simpleConcurrentMap = ConcurrentHashMap.newMap();
					        MutableSet<String> simpleMultiReaderSet = MultiReaderUnifiedSet.newSet();
					        MutableSortedSet<String> simpleSortedSetAdapter = getSortedSetAdapter();
					    }

					    void testFullyQualified() {
					        MutableList<String> fullyQualifiedList = FastList.newList();
					        MutableSet<String> fullyQualifiedSet = UnifiedSet.newSet();
					        MutableMap<String, Integer> fullyQualifiedMap = UnifiedMap.newMap();
					        MutableBag<String> fullyQualifiedBag = HashBag.newBag();
					        MutableSortedSet<String> fullyQualifiedSortedSet = TreeSortedSet.newSet();
					        MutableSortedMap<String, Integer> fullyQualifiedSortedMap = TreeSortedMap.newMap();
					        MutableStack<String> fullyQualifiedStack = ArrayStack.newStack();
					        MutableMap<String, Integer> fullyQualifiedConcurrentMap = ConcurrentHashMap.newMap();
					        MutableSet<String> fullyQualifiedMultiReaderSet = MultiReaderUnifiedSet.newSet();
					    }

					    void testRawTypes() {
					        MutableList rawList = FastList.newList();
					        MutableSet rawSet = UnifiedSet.newSet();
					        MutableMap rawMap = UnifiedMap.newMap();
					        MutableBag rawBag = HashBag.newBag();
					        MutableSortedSet rawSortedSet = TreeSortedSet.newSet();
					        MutableSortedMap rawSortedMap = TreeSortedMap.newMap();
					        MutableStack rawStack = ArrayStack.newStack();
					        MutableMap rawConcurrentMap = ConcurrentHashMap.newMap();
					        MutableSet rawMultiReaderSet = MultiReaderUnifiedSet.newSet();
					    }

					    void testWildcardGenerics() {
					        MutableList<? extends Number> wildcardList = FastList.newList();
					        MutableSet<? extends Number> wildcardSet = UnifiedSet.newSet();
					        MutableMap<String, ? extends Number> wildcardMap = UnifiedMap.newMap();
					        MutableBag<? extends Number> wildcardBag = HashBag.newBag();
					        MutableSortedSet<? extends Number> wildcardSortedSet = TreeSortedSet.newSet();
					        MutableSortedMap<String, ? extends Number> wildcardSortedMap = TreeSortedMap.newMap();
					        MutableStack<? extends Number> wildcardStack = ArrayStack.newStack();
					    }

					    void testNestedGenerics() {
					        MutableList<Map<String, Integer>> nestedGenericsList = FastList.newList();
					    }

					    void testMultipleDeclarations() {
					        MutableList<String> list1 = FastList.newList(), list2 = FastList.newListWith("a", "b");
					    }

					    private SortedSetAdapter<String> getSortedSetAdapter() {
					        return null;
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
					import org.eclipse.collections.impl.bag.mutable.HashBag;
					import org.eclipse.collections.impl.list.mutable.FastList;
					import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
					import org.eclipse.collections.impl.map.mutable.UnifiedMap;
					import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
					import org.eclipse.collections.impl.set.mutable.MultiReaderUnifiedSet;
					import org.eclipse.collections.impl.set.mutable.UnifiedSet;
					import org.eclipse.collections.impl.set.sorted.mutable.SortedSetAdapter;
					import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
					import org.eclipse.collections.impl.stack.mutable.ArrayStack;

					class Test {
					    FastList<String> methodReturningList() {
					        return FastList.newList();
					    }

					    UnifiedSet<String> methodReturningSet() {
					        return UnifiedSet.newSet();
					    }

					    UnifiedMap<String, Integer> methodReturningMap() {
					        return UnifiedMap.newMap();
					    }

					    HashBag<String> methodReturningBag() {
					        return HashBag.newBag();
					    }

					    TreeSortedSet<String> methodReturningSortedSet() {
					        return TreeSortedSet.newSet();
					    }

					    TreeSortedMap<String, Integer> methodReturningSortedMap() {
					        return TreeSortedMap.newMap();
					    }

					    ArrayStack<String> methodReturningStack() {
					        return ArrayStack.newStack();
					    }

					    ConcurrentHashMap<String, Integer> methodReturningConcurrentMap() {
					        return ConcurrentHashMap.newMap();
					    }

					    MultiReaderUnifiedSet<String> methodReturningMultiReaderSet() {
					        return MultiReaderUnifiedSet.newSet();
					    }

					    SortedSetAdapter<String> methodReturningSortedSetAdapter() {
					        return null;
					    }

					    void methodWithListParameter(FastList<String> list) {
					        list.size();
					    }

					    void methodWithSetParameter(UnifiedSet<String> set) {
					        set.size();
					    }

					    void methodWithMapParameter(UnifiedMap<String, Integer> map) {
					        map.size();
					    }

					    void methodWithBagParameter(HashBag<String> bag) {
					        bag.size();
					    }

					    void methodWithSortedSetParameter(TreeSortedSet<String> set) {
					        set.size();
					    }

					    void methodWithSortedMapParameter(TreeSortedMap<String, Integer> map) {
					        map.size();
					    }

					    void methodWithStackParameter(ArrayStack<String> stack) {
					        stack.size();
					    }

					    void methodWithConcurrentMapParameter(ConcurrentHashMap<String, Integer> map) {
					        map.size();
					    }

					    void methodWithMultiReaderSetParameter(MultiReaderUnifiedSet<String> set) {
					        set.size();
					    }

					    void methodWithSortedSetAdapterParameter(SortedSetAdapter<String> set) {
					        set.size();
					    }

					    void variablesWithoutInitializer() {
					        FastList<String> uninitializedList;
					        UnifiedSet<String> uninitializedSet;
					        UnifiedMap<String, Integer> uninitializedMap;
					        HashBag<String> uninitializedBag;
					        TreeSortedSet<String> uninitializedSortedSet;
					        TreeSortedMap<String, Integer> uninitializedSortedMap;
					        ArrayStack<String> uninitializedStack;
					        ConcurrentHashMap<String, Integer> uninitializedConcurrentMap;
					        MultiReaderUnifiedSet<String> uninitializedMultiReaderSet;
					        SortedSetAdapter<String> uninitializedSortedSetAdapter;
					    }

					    void nonFinalFields() {
					        class Inner {
					            private FastList<String> nonFinalList = FastList.newList();
					            private UnifiedSet<String> nonFinalSet = UnifiedSet.newSet();
					            private UnifiedMap<String, Integer> nonFinalMap = UnifiedMap.newMap();
					            private HashBag<String> nonFinalBag = HashBag.newBag();
					            private TreeSortedSet<String> nonFinalSortedSet = TreeSortedSet.newSet();
					            private TreeSortedMap<String, Integer> nonFinalSortedMap = TreeSortedMap.newMap();
					            private ArrayStack<String> nonFinalStack = ArrayStack.newStack();
					            private ConcurrentHashMap<String, Integer> nonFinalConcurrentMap = ConcurrentHashMap.newMap();
					            private MultiReaderUnifiedSet<String> nonFinalMultiReaderSet = MultiReaderUnifiedSet.newSet();
					        }
					    }
					}

					interface MyListInterface extends FastList<String> {
					}

					class GenericBoundsExample<T extends FastList<String>> {
					}
					"""
				)
			);
	}
}
