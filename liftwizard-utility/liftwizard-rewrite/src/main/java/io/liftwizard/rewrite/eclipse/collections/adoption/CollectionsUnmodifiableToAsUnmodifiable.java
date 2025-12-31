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
	name = "Replace Collections.unmodifiable*() with Eclipse Collections asUnmodifiable()",
	description = "Replace `Collections.unmodifiableCollection()`, `Collections.unmodifiableList()`, `Collections.unmodifiableMap()`, `Collections.unmodifiableSet()`, `Collections.unmodifiableSortedMap()`, and `Collections.unmodifiableSortedSet()` with Eclipse Collections `asUnmodifiable()` method."
)
public class CollectionsUnmodifiableToAsUnmodifiable {

	@RecipeDescriptor(
		name = "`Collections.unmodifiableCollection()` → `asUnmodifiable()`",
		description = "Replace `Collections.unmodifiableCollection(collection)` with `collection.asUnmodifiable()`."
	)
	public static class CollectionsUnmodifiableCollectionToAsUnmodifiable<T> {

		@BeforeTemplate
		Collection<T> unmodifiableCollection(MutableCollection<T> collection) {
			return Collections.unmodifiableCollection(collection);
		}

		@AfterTemplate
		MutableCollection<T> asUnmodifiable(MutableCollection<T> collection) {
			return collection.asUnmodifiable();
		}
	}

	@RecipeDescriptor(
		name = "`Collections.unmodifiableList()` → `asUnmodifiable()`",
		description = "Replace `Collections.unmodifiableList(list)` with `list.asUnmodifiable()`."
	)
	public static class CollectionsUnmodifiableListToAsUnmodifiable<T> {

		@BeforeTemplate
		List<T> unmodifiableList(MutableList<T> list) {
			return Collections.unmodifiableList(list);
		}

		@AfterTemplate
		MutableList<T> asUnmodifiable(MutableList<T> list) {
			return list.asUnmodifiable();
		}
	}

	@RecipeDescriptor(
		name = "`Collections.unmodifiableMap()` → `asUnmodifiable()`",
		description = "Replace `Collections.unmodifiableMap(map)` with `map.asUnmodifiable()`."
	)
	public static class CollectionsUnmodifiableMapToAsUnmodifiable<K, V> {

		@BeforeTemplate
		Map<K, V> unmodifiableMap(MutableMap<K, V> map) {
			return Collections.unmodifiableMap(map);
		}

		@AfterTemplate
		MutableMap<K, V> asUnmodifiable(MutableMap<K, V> map) {
			return map.asUnmodifiable();
		}
	}

	@RecipeDescriptor(
		name = "`Collections.unmodifiableSet()` → `asUnmodifiable()`",
		description = "Replace `Collections.unmodifiableSet(set)` with `set.asUnmodifiable()`."
	)
	public static class CollectionsUnmodifiableSetToAsUnmodifiable<T> {

		@BeforeTemplate
		Set<T> unmodifiableSet(MutableSet<T> set) {
			return Collections.unmodifiableSet(set);
		}

		@AfterTemplate
		MutableSet<T> asUnmodifiable(MutableSet<T> set) {
			return set.asUnmodifiable();
		}
	}

	@RecipeDescriptor(
		name = "`Collections.unmodifiableSortedMap()` → `asUnmodifiable()`",
		description = "Replace `Collections.unmodifiableSortedMap(sortedMap)` with `sortedMap.asUnmodifiable()`."
	)
	public static class CollectionsUnmodifiableSortedMapToAsUnmodifiable<K, V> {

		@BeforeTemplate
		SortedMap<K, V> unmodifiableSortedMap(MutableSortedMap<K, V> sortedMap) {
			return Collections.unmodifiableSortedMap(sortedMap);
		}

		@AfterTemplate
		MutableSortedMap<K, V> asUnmodifiable(MutableSortedMap<K, V> sortedMap) {
			return sortedMap.asUnmodifiable();
		}
	}

	@RecipeDescriptor(
		name = "`Collections.unmodifiableSortedSet()` → `asUnmodifiable()`",
		description = "Replace `Collections.unmodifiableSortedSet(sortedSet)` with `sortedSet.asUnmodifiable()`."
	)
	public static class CollectionsUnmodifiableSortedSetToAsUnmodifiable<T> {

		@BeforeTemplate
		SortedSet<T> unmodifiableSortedSet(MutableSortedSet<T> sortedSet) {
			return Collections.unmodifiableSortedSet(sortedSet);
		}

		@AfterTemplate
		MutableSortedSet<T> asUnmodifiable(MutableSortedSet<T> sortedSet) {
			return sortedSet.asUnmodifiable();
		}
	}
}
