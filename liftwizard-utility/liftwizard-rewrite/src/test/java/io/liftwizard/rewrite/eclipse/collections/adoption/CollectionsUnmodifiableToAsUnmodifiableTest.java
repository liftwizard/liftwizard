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

class CollectionsUnmodifiableToAsUnmodifiableTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new CollectionsUnmodifiableToAsUnmodifiableRecipes());
	}

	@Test
	@DocumentExample
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
					import java.util.Collection;
					import java.util.Collections;
					import java.util.List;
					import java.util.Map;
					import java.util.Set;
					import java.util.SortedMap;
					import java.util.SortedSet;

					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.factory.Maps;
					import org.eclipse.collections.api.factory.Sets;
					import org.eclipse.collections.api.factory.SortedMaps;
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.map.MutableMap;
					import org.eclipse.collections.api.map.sorted.MutableSortedMap;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;

					class Test {
					    private final MutableList<String> fieldList = Lists.mutable.with("a", "b");
					    private final List<String> fieldUnmodifiableList = Collections.unmodifiableList(fieldList);
					    private final Set<String> fieldUnmodifiableSet = Collections.unmodifiableSet(Sets.mutable.with("x", "y"));
					    private final Map<String, Integer> fieldUnmodifiableMap = Collections.unmodifiableMap(Maps.mutable.with("key", 1));
					    private final SortedSet<String> fieldUnmodifiableSortedSet = Collections.unmodifiableSortedSet(SortedSets.mutable.with("x", "y"));
					    private final SortedMap<String, Integer> fieldUnmodifiableSortedMap = Collections.unmodifiableSortedMap(SortedMaps.mutable.with("key", 1));
					    private final Collection<String> fieldUnmodifiableCollection = Collections.unmodifiableCollection(Lists.mutable.with("z"));

					    void test() {
					        MutableList<String> list = Lists.mutable.with("a", "b");
					        List<String> unmodifiableList = Collections.unmodifiableList(list);

					        MutableSet<String> set = Sets.mutable.with("x", "y");
					        Set<String> unmodifiableSet = Collections.unmodifiableSet(set);

					        MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
					        Map<String, Integer> unmodifiableMap = Collections.unmodifiableMap(map);

					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
					        SortedMap<String, Integer> unmodifiableSortedMap = Collections.unmodifiableSortedMap(sortedMap);

					        MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
					        SortedSet<String> unmodifiableSortedSet = Collections.unmodifiableSortedSet(sortedSet);

					        Collection<String> unmodifiableCollection = Collections.unmodifiableCollection(list);

					        List<String> inlineExpression = Collections.unmodifiableList(Lists.mutable.with("c", "d"));

					        MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
					        Map<String, Integer> result = Collections.unmodifiableMap(anotherMap);

					        MutableList<String> list2 = Lists.mutable.with("e", "f");
					        List<String> unmodifiableList1 = Collections.unmodifiableList(list2);
					        List<String> unmodifiableList2 = Collections.unmodifiableList(Lists.mutable.with("g"));

					        MutableSet<String> set2 = Sets.mutable.with("z");
					        Set<String> unmodifiableSet2 = Collections.unmodifiableSet(set2);

					        List<String> chainResult = Collections.unmodifiableList(Lists.mutable.with("h", "i", "j"));
					        int size = chainResult.size();
					    }

					    void testExplicitGenerics(Object element, Object key, Object value) {
					        MutableList<String> list = Lists.mutable.with((String) element);
					        List<String> unmodifiableList = Collections.<String>unmodifiableList(list);

					        MutableSet<String> set = Sets.mutable.with((String) element);
					        Set<String> unmodifiableSet = Collections.<String>unmodifiableSet(set);

					        MutableMap<String, Integer> map = Maps.mutable.with((String) key, (Integer) value);
					        Map<String, Integer> unmodifiableMap = Collections.<String, Integer>unmodifiableMap(map);

					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with((String) key, (Integer) value);
					        SortedMap<String, Integer> unmodifiableSortedMap = Collections.<String, Integer>unmodifiableSortedMap(sortedMap);

					        MutableSortedSet<String> sortedSet = SortedSets.mutable.with((String) element);
					        SortedSet<String> unmodifiableSortedSet = Collections.<String>unmodifiableSortedSet(sortedSet);

					        Collection<String> unmodifiableCollection = Collections.<String>unmodifiableCollection(list);
					    }

					    List<String> getList() {
					        MutableList<String> list = Lists.mutable.with("a", "b");
					        return Collections.unmodifiableList(list);
					    }
					}
					""",
					"""
					import java.util.Collection;
					import java.util.List;
					import java.util.Map;
					import java.util.Set;
					import java.util.SortedMap;
					import java.util.SortedSet;

					import org.eclipse.collections.api.factory.Lists;
					import org.eclipse.collections.api.factory.Maps;
					import org.eclipse.collections.api.factory.Sets;
					import org.eclipse.collections.api.factory.SortedMaps;
					import org.eclipse.collections.api.factory.SortedSets;
					import org.eclipse.collections.api.list.MutableList;
					import org.eclipse.collections.api.map.MutableMap;
					import org.eclipse.collections.api.map.sorted.MutableSortedMap;
					import org.eclipse.collections.api.set.MutableSet;
					import org.eclipse.collections.api.set.sorted.MutableSortedSet;

					class Test {
					    private final MutableList<String> fieldList = Lists.mutable.with("a", "b");
					    private final List<String> fieldUnmodifiableList = fieldList.asUnmodifiable();
					    private final Set<String> fieldUnmodifiableSet = Sets.mutable.with("x", "y").asUnmodifiable();
					    private final Map<String, Integer> fieldUnmodifiableMap = Maps.mutable.with("key", 1).asUnmodifiable();
					    private final SortedSet<String> fieldUnmodifiableSortedSet = SortedSets.mutable.with("x", "y").asUnmodifiable();
					    private final SortedMap<String, Integer> fieldUnmodifiableSortedMap = SortedMaps.mutable.with("key", 1).asUnmodifiable();
					    private final Collection<String> fieldUnmodifiableCollection = Lists.mutable.with("z").asUnmodifiable();

					    void test() {
					        MutableList<String> list = Lists.mutable.with("a", "b");
					        List<String> unmodifiableList = list.asUnmodifiable();

					        MutableSet<String> set = Sets.mutable.with("x", "y");
					        Set<String> unmodifiableSet = set.asUnmodifiable();

					        MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
					        Map<String, Integer> unmodifiableMap = map.asUnmodifiable();

					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
					        SortedMap<String, Integer> unmodifiableSortedMap = sortedMap.asUnmodifiable();

					        MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
					        SortedSet<String> unmodifiableSortedSet = sortedSet.asUnmodifiable();

					        Collection<String> unmodifiableCollection = list.asUnmodifiable();

					        List<String> inlineExpression = Lists.mutable.with("c", "d").asUnmodifiable();

					        MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
					        Map<String, Integer> result = anotherMap.asUnmodifiable();

					        MutableList<String> list2 = Lists.mutable.with("e", "f");
					        List<String> unmodifiableList1 = list2.asUnmodifiable();
					        List<String> unmodifiableList2 = Lists.mutable.with("g").asUnmodifiable();

					        MutableSet<String> set2 = Sets.mutable.with("z");
					        Set<String> unmodifiableSet2 = set2.asUnmodifiable();

					        List<String> chainResult = Lists.mutable.with("h", "i", "j").asUnmodifiable();
					        int size = chainResult.size();
					    }

					    void testExplicitGenerics(Object element, Object key, Object value) {
					        MutableList<String> list = Lists.mutable.with((String) element);
					        List<String> unmodifiableList = list.asUnmodifiable();

					        MutableSet<String> set = Sets.mutable.with((String) element);
					        Set<String> unmodifiableSet = set.asUnmodifiable();

					        MutableMap<String, Integer> map = Maps.mutable.with((String) key, (Integer) value);
					        Map<String, Integer> unmodifiableMap = map.asUnmodifiable();

					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with((String) key, (Integer) value);
					        SortedMap<String, Integer> unmodifiableSortedMap = sortedMap.asUnmodifiable();

					        MutableSortedSet<String> sortedSet = SortedSets.mutable.with((String) element);
					        SortedSet<String> unmodifiableSortedSet = sortedSet.asUnmodifiable();

					        Collection<String> unmodifiableCollection = list.asUnmodifiable();
					    }

					    List<String> getList() {
					        MutableList<String> list = Lists.mutable.with("a", "b");
					        return list.asUnmodifiable();
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
					import java.util.ArrayList;
					import java.util.Collections;
					import java.util.List;

					class Test {
					    void test() {
					        List<String> list = new ArrayList<>();
					        List<String> unmodifiableList = Collections.unmodifiableList(list);
					    }
					}
					"""
				)
			);
	}
}
