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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.eclipse.collections.impl.utility.ListIterate;
import org.openrewrite.java.template.RecipeDescriptor;

/**
 * Transforms select+notEmpty chains to anySatisfy.
 *
 * <p>Pattern 1 - RichIterable methods:
 * <pre>{@code
 * // Before
 * list.select(predicate).notEmpty();
 *
 * // After
 * list.anySatisfy(predicate);
 * }</pre>
 *
 * <p>Pattern 2 - Static utility methods (ArrayIterate, ListIterate):
 * <pre>{@code
 * // Before
 * ArrayIterate.select(array, predicate).notEmpty();
 * ListIterate.select(list, predicate).notEmpty();
 *
 * // After
 * ArrayIterate.anySatisfy(array, predicate);
 * ListIterate.anySatisfy(list, predicate);
 * }</pre>
 *
 * <p>This recipe simplifies code by using the direct anySatisfy method
 * instead of chaining select with notEmpty. The semantics are equivalent:
 * select().notEmpty() returns true if any element satisfies the predicate.
 */
@RecipeDescriptor(
	name = "`select(pred).notEmpty()` to `anySatisfy(pred)`",
	description = "Transforms `collection.select(pred).notEmpty()` to `collection.anySatisfy(pred)`. "
	+ "Also handles static utility methods like ArrayIterate.select() and ListIterate.select()."
)
public class ECSelectNotEmptyToAnySatisfy {

	@RecipeDescriptor(
		name = "`select(pred).notEmpty()` to `anySatisfy(pred)`",
		description = "Transforms `collection.select(pred).notEmpty()` to `collection.anySatisfy(pred)`."
	)
	public static final class SelectNotEmptyToAnySatisfy<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.select(predicate).notEmpty();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.anySatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ArrayIterate.select(array, pred).notEmpty()` to `ArrayIterate.anySatisfy(array, pred)`",
		description = "Transforms `ArrayIterate.select(array, pred).notEmpty()` to `ArrayIterate.anySatisfy(array, pred)`."
	)
	public static final class ArrayIterateSelectNotEmptyToAnySatisfy<T> {

		@BeforeTemplate
		boolean before(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.select(array, predicate).notEmpty();
		}

		@AfterTemplate
		boolean after(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.anySatisfy(array, predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ListIterate.select(list, pred).notEmpty()` to `ListIterate.anySatisfy(list, pred)`",
		description = "Transforms `ListIterate.select(list, pred).notEmpty()` to `ListIterate.anySatisfy(list, pred)`."
	)
	public static final class ListIterateSelectNotEmptyToAnySatisfy<T> {

		@BeforeTemplate
		boolean before(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.select(list, predicate).notEmpty();
		}

		@AfterTemplate
		boolean after(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.anySatisfy(list, predicate);
		}
	}
}
