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

import java.util.Optional;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.ordered.OrderedIterable;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.eclipse.collections.impl.utility.ListIterate;
import org.openrewrite.java.template.RecipeDescriptor;

/**
 * Transforms select+getFirstOptional/getFirst chains to detectOptional/detect.
 *
 * <p>Pattern 1 - RichIterable methods:
 * <pre>{@code
 * // Before
 * list.select(predicate).getFirstOptional();
 * list.select(predicate).getFirst();
 *
 * // After
 * list.detectOptional(predicate);
 * list.detect(predicate);
 * }</pre>
 *
 * <p>Pattern 2 - Static utility methods (ArrayIterate, ListIterate):
 * <pre>{@code
 * // Before
 * ArrayIterate.select(array, predicate).getFirstOptional();
 * ListIterate.select(list, predicate).getFirst();
 *
 * // After
 * ArrayIterate.detectOptional(array, predicate);
 * ListIterate.detect(list, predicate);
 * }</pre>
 *
 * <p>This recipe simplifies code by using the direct detectOptional/detect methods
 * instead of chaining select with getFirstOptional/getFirst.
 *
 * <p>Note: getFirstOptional() is only available on OrderedIterable (lists, sorted sets),
 * not on all RichIterable types (e.g., not on unsorted sets).
 */
@RecipeDescriptor(
	name = "`select(pred).getFirstOptional()` to `detectOptional(pred)`",
	description = "Transforms `collection.select(pred).getFirstOptional()` to `collection.detectOptional(pred)` "
	+ "and `collection.select(pred).getFirst()` to `collection.detect(pred)`. "
	+ "Also handles static utility methods like ArrayIterate.select() and ListIterate.select()."
)
public class ECSelectFirstToDetect {

	@RecipeDescriptor(
		name = "`select(pred).getFirstOptional()` to `detectOptional(pred)`",
		description = "Transforms `collection.select(pred).getFirstOptional()` to `collection.detectOptional(pred)`. "
		+ "Only applies to OrderedIterable types (lists, sorted sets)."
	)
	public static final class SelectGetFirstOptionalToDetectOptional<T> {

		@BeforeTemplate
		Optional<T> before(OrderedIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.select(predicate).getFirstOptional();
		}

		@AfterTemplate
		Optional<T> after(OrderedIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.detectOptional(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`select(pred).getFirst()` to `detect(pred)`",
		description = "Transforms `collection.select(pred).getFirst()` to `collection.detect(pred)`."
	)
	public static final class SelectGetFirstToDetect<T> {

		@BeforeTemplate
		T before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.select(predicate).getFirst();
		}

		@AfterTemplate
		T after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.detect(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ArrayIterate.select(array, pred).getFirstOptional()` to `ArrayIterate.detectOptional(array, pred)`",
		description = "Transforms `ArrayIterate.select(array, pred).getFirstOptional()` to `ArrayIterate.detectOptional(array, pred)`."
	)
	public static final class ArrayIterateSelectGetFirstOptionalToDetectOptional<T> {

		@BeforeTemplate
		Optional<T> before(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.select(array, predicate).getFirstOptional();
		}

		@AfterTemplate
		Optional<T> after(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.detectOptional(array, predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ArrayIterate.select(array, pred).getFirst()` to `ArrayIterate.detect(array, pred)`",
		description = "Transforms `ArrayIterate.select(array, pred).getFirst()` to `ArrayIterate.detect(array, pred)`."
	)
	public static final class ArrayIterateSelectGetFirstToDetect<T> {

		@BeforeTemplate
		T before(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.select(array, predicate).getFirst();
		}

		@AfterTemplate
		T after(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.detect(array, predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ListIterate.select(list, pred).getFirstOptional()` to `ListIterate.detectOptional(list, pred)`",
		description = "Transforms `ListIterate.select(list, pred).getFirstOptional()` to `ListIterate.detectOptional(list, pred)`."
	)
	public static final class ListIterateSelectGetFirstOptionalToDetectOptional<T> {

		@BeforeTemplate
		Optional<T> before(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.select(list, predicate).getFirstOptional();
		}

		@AfterTemplate
		Optional<T> after(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.detectOptional(list, predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ListIterate.select(list, pred).getFirst()` to `ListIterate.detect(list, pred)`",
		description = "Transforms `ListIterate.select(list, pred).getFirst()` to `ListIterate.detect(list, pred)`."
	)
	public static final class ListIterateSelectGetFirstToDetect<T> {

		@BeforeTemplate
		T before(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.select(list, predicate).getFirst();
		}

		@AfterTemplate
		T after(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.detect(list, predicate);
		}
	}
}
