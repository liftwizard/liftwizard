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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Replace Collections.synchronized*() with Eclipse Collections asSynchronized()",
    description = "Replace `Collections.synchronizedCollection()`, `Collections.synchronizedList()`, `Collections.synchronizedMap()`, `Collections.synchronizedSet()`, `Collections.synchronizedSortedMap()`, and `Collections.synchronizedSortedSet()` with Eclipse Collections `asSynchronized()` method."
)
public class CollectionsSynchronizedToAsSynchronized {

    @RecipeDescriptor(
        name = "`Collections.synchronizedCollection()` → `asSynchronized()`",
        description = "Replace `Collections.synchronizedCollection(collection)` with `collection.asSynchronized()`."
    )
    public static class CollectionsSynchronizedCollectionToAsSynchronized<T> {

        @BeforeTemplate
        Collection<T> synchronizedCollection(MutableCollection<T> collection) {
            return Collections.synchronizedCollection(collection);
        }

        @AfterTemplate
        MutableCollection<T> asSynchronized(MutableCollection<T> collection) {
            return collection.asSynchronized();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.synchronizedList()` → `asSynchronized()`",
        description = "Replace `Collections.synchronizedList(list)` with `list.asSynchronized()`."
    )
    public static class CollectionsSynchronizedListToAsSynchronized<T> {

        @BeforeTemplate
        List<T> synchronizedList(MutableList<T> list) {
            return Collections.synchronizedList(list);
        }

        @AfterTemplate
        MutableList<T> asSynchronized(MutableList<T> list) {
            return list.asSynchronized();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.synchronizedMap()` → `asSynchronized()`",
        description = "Replace `Collections.synchronizedMap(map)` with `map.asSynchronized()`."
    )
    public static class CollectionsSynchronizedMapToAsSynchronized<K, V> {

        @BeforeTemplate
        Map<K, V> synchronizedMap(MutableMap<K, V> map) {
            return Collections.synchronizedMap(map);
        }

        @AfterTemplate
        MutableMap<K, V> asSynchronized(MutableMap<K, V> map) {
            return map.asSynchronized();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.synchronizedSet()` → `asSynchronized()`",
        description = "Replace `Collections.synchronizedSet(set)` with `set.asSynchronized()`."
    )
    public static class CollectionsSynchronizedSetToAsSynchronized<T> {

        @BeforeTemplate
        Set<T> synchronizedSet(MutableSet<T> set) {
            return Collections.synchronizedSet(set);
        }

        @AfterTemplate
        MutableSet<T> asSynchronized(MutableSet<T> set) {
            return set.asSynchronized();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.synchronizedSortedMap()` → `asSynchronized()`",
        description = "Replace `Collections.synchronizedSortedMap(sortedMap)` with `sortedMap.asSynchronized()`."
    )
    public static class CollectionsSynchronizedSortedMapToAsSynchronized<K, V> {

        @BeforeTemplate
        SortedMap<K, V> synchronizedSortedMap(MutableSortedMap<K, V> sortedMap) {
            return Collections.synchronizedSortedMap(sortedMap);
        }

        @AfterTemplate
        MutableSortedMap<K, V> asSynchronized(MutableSortedMap<K, V> sortedMap) {
            return sortedMap.asSynchronized();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.synchronizedSortedSet()` → `asSynchronized()`",
        description = "Replace `Collections.synchronizedSortedSet(sortedSet)` with `sortedSet.asSynchronized()`."
    )
    public static class CollectionsSynchronizedSortedSetToAsSynchronized<T> {

        @BeforeTemplate
        SortedSet<T> synchronizedSortedSet(MutableSortedSet<T> sortedSet) {
            return Collections.synchronizedSortedSet(sortedSet);
        }

        @AfterTemplate
        MutableSortedSet<T> asSynchronized(MutableSortedSet<T> sortedSet) {
            return sortedSet.asSynchronized();
        }
    }
}
