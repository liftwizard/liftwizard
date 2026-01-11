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
import java.util.function.Consumer;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`stream().forEach(action)` -> `forEach(action)`",
	description = "Transforms `iterable.stream().forEach(action)` to `iterable.forEach(action)` for Eclipse Collections types. This eliminates the unnecessary stream intermediary since Eclipse Collections has forEach directly."
)
public class ECStreamForEach {

	@RecipeDescriptor(
		name = "`stream().forEach(action)` -> `forEach(action)`",
		description = "Converts `iterable.stream().forEach(action)` to `iterable.forEach(action)`."
	)
	public static final class CollectionStreamForEachToForEach<T> {

		@BeforeTemplate
		void before(Collection<T> collection, Consumer<? super T> action) {
			collection.stream().forEach(action);
		}

		@AfterTemplate
		void after(Collection<T> collection, Consumer<? super T> action) {
			collection.forEach(action);
		}
	}

	@RecipeDescriptor(
		name = "`stream().forEach(action)` -> `forEach(action)` for ImmutableCollection",
		description = "Converts `collection.stream().forEach(action)` to `collection.forEach(action)` for Eclipse Collections ImmutableCollection types."
	)
	public static final class ImmutableCollectionStreamForEachToForEach<T> {

		@BeforeTemplate
		void before(ImmutableCollection<T> collection, Consumer<? super T> action) {
			collection.stream().forEach(action);
		}

		@AfterTemplate
		void after(ImmutableCollection<T> collection, Consumer<? super T> action) {
			collection.forEach(action);
		}
	}
}
