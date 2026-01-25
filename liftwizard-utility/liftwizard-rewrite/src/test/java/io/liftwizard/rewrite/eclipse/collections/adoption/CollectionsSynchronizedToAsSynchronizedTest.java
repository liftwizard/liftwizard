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

class CollectionsSynchronizedToAsSynchronizedTest extends AbstractEclipseCollectionsTest {

	@Override
	public void defaults(RecipeSpec spec) {
		super.defaults(spec);
		spec.recipe(new CollectionsSynchronizedToAsSynchronizedRecipes());
	}

	@DocumentExample
	@Test
	void replacePatterns() {
		this.rewriteRun(
				java(
					"""
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
					    private final List<String> fieldSynchronizedList = Collections.synchronizedList(fieldList);
					    private final Set<String> fieldSynchronizedSet = Collections.synchronizedSet(Sets.mutable.with("x", "y"));
					    private final Map<String, Integer> fieldSynchronizedMap = Collections.synchronizedMap(Maps.mutable.with("key", 1));
					    private final SortedSet<String> fieldSynchronizedSortedSet = Collections.synchronizedSortedSet(SortedSets.mutable.with("x", "y"));
					    private final SortedMap<String, Integer> fieldSynchronizedSortedMap = Collections.synchronizedSortedMap(SortedMaps.mutable.with("key", 1));

					    void test(Object obj) {
					        MutableList<String> list = Lists.mutable.with("a", "b");
					        List<String> synchronizedList = Collections.synchronizedList(list);

					        MutableSet<String> set = Sets.mutable.with("x", "y");
					        Set<String> synchronizedSet = Collections.synchronizedSet(set);

					        MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
					        Map<String, Integer> synchronizedMap = Collections.synchronizedMap(map);

					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
					        SortedMap<String, Integer> synchronizedSortedMap = Collections.synchronizedSortedMap(sortedMap);

					        MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
					        SortedSet<String> synchronizedSortedSet = Collections.synchronizedSortedSet(sortedSet);

					        List<String> inlineExpression = Collections.synchronizedList(Lists.mutable.with("c", "d"));

					        MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
					        Map<String, Integer> result = Collections.synchronizedMap(anotherMap);
					        result.put("another", 2);

					        MutableList<String> explicitList = Lists.mutable.with((String) obj);
					        List<String> explicitSynchronizedList = Collections.<String>synchronizedList(explicitList);

					        MutableSet<String> explicitSet = Sets.mutable.with((String) obj);
					        Set<String> explicitSynchronizedSet = Collections.<String>synchronizedSet(explicitSet);

					        MutableMap<String, Integer> explicitMap = Maps.mutable.with((String) obj, (Integer) obj);
					        Map<String, Integer> explicitSynchronizedMap = Collections.<String, Integer>synchronizedMap(explicitMap);

					        MutableSortedMap<String, Integer> explicitSortedMap = SortedMaps.mutable.with((String) obj, (Integer) obj);
					        SortedMap<String, Integer> explicitSynchronizedSortedMap = Collections.<String, Integer>synchronizedSortedMap(explicitSortedMap);

					        MutableSortedSet<String> explicitSortedSet = SortedSets.mutable.with((String) obj);
					        SortedSet<String> explicitSynchronizedSortedSet = Collections.<String>synchronizedSortedSet(explicitSortedSet);
					    }
					}
					""",
					"""
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
					    private final List<String> fieldSynchronizedList = fieldList.asSynchronized();
					    private final Set<String> fieldSynchronizedSet = Sets.mutable.with("x", "y").asSynchronized();
					    private final Map<String, Integer> fieldSynchronizedMap = Maps.mutable.with("key", 1).asSynchronized();
					    private final SortedSet<String> fieldSynchronizedSortedSet = SortedSets.mutable.with("x", "y").asSynchronized();
					    private final SortedMap<String, Integer> fieldSynchronizedSortedMap = SortedMaps.mutable.with("key", 1).asSynchronized();

					    void test(Object obj) {
					        MutableList<String> list = Lists.mutable.with("a", "b");
					        List<String> synchronizedList = list.asSynchronized();

					        MutableSet<String> set = Sets.mutable.with("x", "y");
					        Set<String> synchronizedSet = set.asSynchronized();

					        MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
					        Map<String, Integer> synchronizedMap = map.asSynchronized();

					        MutableSortedMap<String, Integer> sortedMap = SortedMaps.mutable.with("key", 1);
					        SortedMap<String, Integer> synchronizedSortedMap = sortedMap.asSynchronized();

					        MutableSortedSet<String> sortedSet = SortedSets.mutable.with("x", "y");
					        SortedSet<String> synchronizedSortedSet = sortedSet.asSynchronized();

					        List<String> inlineExpression = Lists.mutable.with("c", "d").asSynchronized();

					        MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
					        Map<String, Integer> result = anotherMap.asSynchronized();
					        result.put("another", 2);

					        MutableList<String> explicitList = Lists.mutable.with((String) obj);
					        List<String> explicitSynchronizedList = explicitList.asSynchronized();

					        MutableSet<String> explicitSet = Sets.mutable.with((String) obj);
					        Set<String> explicitSynchronizedSet = explicitSet.asSynchronized();

					        MutableMap<String, Integer> explicitMap = Maps.mutable.with((String) obj, (Integer) obj);
					        Map<String, Integer> explicitSynchronizedMap = explicitMap.asSynchronized();

					        MutableSortedMap<String, Integer> explicitSortedMap = SortedMaps.mutable.with((String) obj, (Integer) obj);
					        SortedMap<String, Integer> explicitSynchronizedSortedMap = explicitSortedMap.asSynchronized();

					        MutableSortedSet<String> explicitSortedSet = SortedSets.mutable.with((String) obj);
					        SortedSet<String> explicitSynchronizedSortedSet = explicitSortedSet.asSynchronized();
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
					        List<String> synchronizedList = Collections.synchronizedList(list);
					    }
					}
					"""
				)
			);
	}
}
