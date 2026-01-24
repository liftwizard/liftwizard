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
 * Transforms select().size() chains to count().
 *
 * <p>Pattern 1 - RichIterable methods:
 * <pre>{@code
 * // Before
 * list.select(predicate).size();
 *
 * // After
 * list.count(predicate);
 * }</pre>
 *
 * <p>Pattern 2 - Static utility methods (ArrayIterate, ListIterate):
 * <pre>{@code
 * // Before
 * ArrayIterate.select(array, predicate).size();
 * ListIterate.select(list, predicate).size();
 *
 * // After
 * ArrayIterate.count(array, predicate);
 * ListIterate.count(list, predicate);
 * }</pre>
 *
 * <p>This recipe simplifies code by using the direct count() method
 * instead of chaining select() with size(). The count() method is more
 * efficient as it avoids creating an intermediate collection.
 */
@RecipeDescriptor(
	name = "`select(pred).size()` to `count(pred)`",
	description = "Transforms `collection.select(pred).size()` to `collection.count(pred)`. "
	+ "Also handles static utility methods like ArrayIterate.select() and ListIterate.select()."
)
public class ECSelectSizeToCount {

	@RecipeDescriptor(
		name = "`select(pred).size()` to `count(pred)`",
		description = "Transforms `collection.select(pred).size()` to `collection.count(pred)`."
	)
	public static final class SelectSizeToCount<T> {

		@BeforeTemplate
		int before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.select(predicate).size();
		}

		@AfterTemplate
		int after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ArrayIterate.select(array, pred).size()` to `ArrayIterate.count(array, pred)`",
		description = "Transforms `ArrayIterate.select(array, pred).size()` to `ArrayIterate.count(array, pred)`."
	)
	public static final class ArrayIterateSelectSizeToCount<T> {

		@BeforeTemplate
		int before(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.select(array, predicate).size();
		}

		@AfterTemplate
		int after(T[] array, Predicate<? super T> predicate) {
			return ArrayIterate.count(array, predicate);
		}
	}

	@RecipeDescriptor(
		name = "`ListIterate.select(list, pred).size()` to `ListIterate.count(list, pred)`",
		description = "Transforms `ListIterate.select(list, pred).size()` to `ListIterate.count(list, pred)`."
	)
	public static final class ListIterateSelectSizeToCount<T> {

		@BeforeTemplate
		int before(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.select(list, predicate).size();
		}

		@AfterTemplate
		int after(java.util.List<T> list, Predicate<? super T> predicate) {
			return ListIterate.count(list, predicate);
		}
	}
}
