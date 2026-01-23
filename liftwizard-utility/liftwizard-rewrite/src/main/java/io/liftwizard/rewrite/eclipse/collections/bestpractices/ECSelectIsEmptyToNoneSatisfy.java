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
 * Transforms select+isEmpty chains to noneSatisfy.
 *
 * <p>Pattern 1 - RichIterable methods:
 * <pre>{@code
 * // Before
 * list.select(predicate).isEmpty();
 *
 * // After
 * list.noneSatisfy(predicate);
 * }</pre>
 *
 * <p>Pattern 2 - Static utility methods (ArrayIterate, ListIterate):
 * <pre>{@code
 * // Before
 * ArrayIterate.select(array, predicate).isEmpty();
 * ListIterate.select(list, predicate).isEmpty();
 *
 * // After
 * ArrayIterate.noneSatisfy(array, predicate);
 * ListIterate.noneSatisfy(list, predicate);
 * }</pre>
 *
 * <p>This recipe simplifies code by using the direct noneSatisfy method
 * instead of chaining select with isEmpty. The semantics are equivalent:
 * select().isEmpty() returns true if no element satisfies the predicate.
 */
@RecipeDescriptor(
	name = "`select(pred).isEmpty()` to `noneSatisfy(pred)`",
	description = "Transforms select().isEmpty() to noneSatisfy(). " + "Also handles ArrayIterate and ListIterate."
)
public class ECSelectIsEmptyToNoneSatisfy {

	@RecipeDescriptor(
		name = "`select(pred).isEmpty()` to `noneSatisfy(pred)`",
		description = "Transforms select(pred).isEmpty() to noneSatisfy(pred)."
	)
	public static final class SelectIsEmptyToNoneSatisfy<T> {

		@BeforeTemplate
		boolean before(final RichIterable<T> iterable, final Predicate<? super T> predicate) {
			return iterable.select(predicate).isEmpty();
		}

		@AfterTemplate
		boolean after(final RichIterable<T> iterable, final Predicate<? super T> predicate) {
			return iterable.noneSatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ArrayIterate.select(array, pred).isEmpty()` " + "to `ArrayIterate.noneSatisfy(array, pred)`",
		description = "Transforms ArrayIterate.select().isEmpty() " + "to ArrayIterate.noneSatisfy()."
	)
	public static final class ArrayIterateSelectIsEmptyToNoneSatisfy<T> {

		@BeforeTemplate
		boolean before(final T[] array, final Predicate<? super T> predicate) {
			return ArrayIterate.select(array, predicate).isEmpty();
		}

		@AfterTemplate
		boolean after(final T[] array, final Predicate<? super T> predicate) {
			return ArrayIterate.noneSatisfy(array, predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ListIterate.select(list, pred).isEmpty()` " + "to `ListIterate.noneSatisfy(list, pred)`",
		description = "Transforms ListIterate.select().isEmpty() " + "to ListIterate.noneSatisfy()."
	)
	public static final class ListIterateSelectIsEmptyToNoneSatisfy<T> {

		@BeforeTemplate
		boolean before(final java.util.List<T> list, final Predicate<? super T> predicate) {
			return ListIterate.select(list, predicate).isEmpty();
		}

		@AfterTemplate
		boolean after(final java.util.List<T> list, final Predicate<? super T> predicate) {
			return ListIterate.noneSatisfy(list, predicate);
		}
	}
}
