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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "Replace Collections.singleton*() with Eclipse Collections factories",
	description = "Replace `Collections.singletonList()`, `Collections.singleton()`, and `Collections.singletonMap()` with Eclipse Collections factory methods."
)
public class CollectionsSingletonToFactory {

	@RecipeDescriptor(
		name = "`Collections.singletonList()` → `Lists.fixedSize.with()`",
		description = "Replace `Collections.singletonList(x)` with `Lists.fixedSize.with(x)`."
	)
	public static class CollectionsSingletonListToFactory<T> {

		@BeforeTemplate
		List<T> singletonList(T element) {
			return Collections.singletonList(element);
		}

		@AfterTemplate
		List<T> listsFixedSizeOf(T element) {
			return Lists.fixedSize.with(element);
		}
	}

	@RecipeDescriptor(
		name = "`Collections.singleton()` → `Sets.fixedSize.with()`",
		description = "Replace `Collections.singleton(x)` with `Sets.fixedSize.with(x)`."
	)
	public static class CollectionsSingletonSetToFactory<T> {

		@BeforeTemplate
		Set<T> singleton(T element) {
			return Collections.singleton(element);
		}

		@AfterTemplate
		Set<T> setsFixedSizeOf(T element) {
			return Sets.fixedSize.with(element);
		}
	}

	@RecipeDescriptor(
		name = "`Collections.singletonMap()` → `Maps.fixedSize.with()`",
		description = "Replace `Collections.singletonMap(k, v)` with `Maps.fixedSize.with(k, v)`."
	)
	public static class CollectionsSingletonMapToFactory<K, V> {

		@BeforeTemplate
		Map<K, V> singletonMap(K key, V value) {
			return Collections.singletonMap(key, value);
		}

		@AfterTemplate
		Map<K, V> mapsFixedSizeOf(K key, V value) {
			return Maps.fixedSize.with(key, value);
		}
	}
}
