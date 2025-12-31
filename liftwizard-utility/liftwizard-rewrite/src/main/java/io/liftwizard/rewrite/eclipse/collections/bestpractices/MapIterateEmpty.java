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

import java.util.Map;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.impl.utility.MapIterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "Map empty checks → `MapIterate`",
	description = "Replace manual map null and isEmpty checks with "
	+ "`MapIterate.isEmpty()` and `MapIterate.notEmpty()`."
)
public class MapIterateEmpty {

	@RecipeDescriptor(
		name = "`map == null || map.isEmpty()` → " + "`MapIterate.isEmpty(map)`",
		description = "Replace manual null or empty check with " + "`MapIterate.isEmpty(map)`."
	)
	public static final class IsEmptyPattern<K, V> {

		@BeforeTemplate
		boolean before(Map<K, V> map) {
			return map == null || map.isEmpty();
		}

		@AfterTemplate
		boolean after(Map<K, V> map) {
			return MapIterate.isEmpty(map);
		}
	}

	@RecipeDescriptor(
		name = "`map != null && !map.isEmpty()` → " + "`MapIterate.notEmpty(map)`",
		description = "Replace manual not-null and not-empty check with " + "`MapIterate.notEmpty(map)`."
	)
	public static final class NotEmptyPattern<K, V> {

		@BeforeTemplate
		boolean before(Map<K, V> map) {
			return map != null && !map.isEmpty();
		}

		@AfterTemplate
		boolean after(Map<K, V> map) {
			return MapIterate.notEmpty(map);
		}
	}

	@RecipeDescriptor(
		name = "`!MapIterate.isEmpty()` → `MapIterate.notEmpty()`",
		description = "Converts `!MapIterate.isEmpty(map)` to `MapIterate.notEmpty(map)`."
	)
	public static final class NegatedMapIterateIsEmptyToNotEmpty<K, V> {

		@BeforeTemplate
		boolean before(Map<K, V> map) {
			return !MapIterate.isEmpty(map);
		}

		@AfterTemplate
		boolean after(Map<K, V> map) {
			return MapIterate.notEmpty(map);
		}
	}

	@RecipeDescriptor(
		name = "`!MapIterate.notEmpty()` → `MapIterate.isEmpty()`",
		description = "Converts `!MapIterate.notEmpty(map)` to `MapIterate.isEmpty(map)`."
	)
	public static final class NegatedMapIterateNotEmptyToIsEmpty<K, V> {

		@BeforeTemplate
		boolean before(Map<K, V> map) {
			return !MapIterate.notEmpty(map);
		}

		@AfterTemplate
		boolean after(Map<K, V> map) {
			return MapIterate.isEmpty(map);
		}
	}
}
