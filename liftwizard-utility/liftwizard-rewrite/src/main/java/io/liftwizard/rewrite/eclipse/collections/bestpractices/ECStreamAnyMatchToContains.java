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

import java.util.Collection;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`stream().anyMatch(value::equals)` to `contains(value)`",
	description = "Transforms `iterable.stream().anyMatch(value::equals)` to `iterable.contains(value)`. "
	+ "The contains() method is simpler and more readable, and avoids the unnecessary stream intermediary."
)
public class ECStreamAnyMatchToContains {

	@RecipeDescriptor(
		name = "`stream().anyMatch(value::equals)` to `contains(value)` for Collection",
		description = "Converts `collection.stream().anyMatch(value::equals)` to `collection.contains(value)`."
	)
	public static final class CollectionStreamAnyMatchToContains<T, S> {

		@BeforeTemplate
		boolean before(Collection<T> collection, S value) {
			return collection.stream().anyMatch(value::equals);
		}

		@AfterTemplate
		boolean after(Collection<T> collection, S value) {
			return collection.contains(value);
		}
	}

	@RecipeDescriptor(
		name = "`stream().anyMatch(value::equals)` to `contains(value)` for ImmutableCollection",
		description = "Converts `collection.stream().anyMatch(value::equals)` to `collection.contains(value)` for Eclipse Collections ImmutableCollection types."
	)
	public static final class ImmutableCollectionStreamAnyMatchToContains<T, S> {

		@BeforeTemplate
		boolean before(ImmutableCollection<T> collection, S value) {
			return collection.stream().anyMatch(value::equals);
		}

		@AfterTemplate
		boolean after(ImmutableCollection<T> collection, S value) {
			return collection.contains(value);
		}
	}
}
