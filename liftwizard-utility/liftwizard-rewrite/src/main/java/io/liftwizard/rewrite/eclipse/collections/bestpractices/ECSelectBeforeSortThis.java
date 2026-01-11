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

import java.util.Comparator;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.list.MutableList;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`sortThis().select(pred)` -> `select(pred).sortThis()`",
	description = "Transforms `list.sortThis().select(pred)` to `list.select(pred).sortThis()` for Eclipse Collections MutableList types. "
	+ "This optimization filters before sorting, reducing the number of elements that need to be sorted. "
	+ "Note: This is only safe when the select predicate does not depend on sorted order."
)
public class ECSelectBeforeSortThis {

	@RecipeDescriptor(
		name = "`sortThis().select(pred)` -> `select(pred).sortThis()`",
		description = "Converts `list.sortThis().select(pred)` to `list.select(pred).sortThis()` to sort fewer elements."
	)
	public static final class SortThisSelectToSelectSortThis<T> {

		@BeforeTemplate
		MutableList<T> before(MutableList<T> list, Predicate<? super T> predicate) {
			return list.sortThis().select(predicate);
		}

		@AfterTemplate
		MutableList<T> after(MutableList<T> list, Predicate<? super T> predicate) {
			return list.select(predicate).sortThis();
		}
	}

	@RecipeDescriptor(
		name = "`sortThis(comparator).select(pred)` -> `select(pred).sortThis(comparator)`",
		description = "Converts `list.sortThis(comparator).select(pred)` to `list.select(pred).sortThis(comparator)` to sort fewer elements."
	)
	public static final class SortThisWithComparatorSelectToSelectSortThis<T> {

		@BeforeTemplate
		MutableList<T> before(MutableList<T> list, Comparator<? super T> comparator, Predicate<? super T> predicate) {
			return list.sortThis(comparator).select(predicate);
		}

		@AfterTemplate
		MutableList<T> after(MutableList<T> list, Comparator<? super T> comparator, Predicate<? super T> predicate) {
			return list.select(predicate).sortThis(comparator);
		}
	}

	@RecipeDescriptor(
		name = "`sortThis().reject(pred)` -> `reject(pred).sortThis()`",
		description = "Converts `list.sortThis().reject(pred)` to `list.reject(pred).sortThis()` to sort fewer elements."
	)
	public static final class SortThisRejectToRejectSortThis<T> {

		@BeforeTemplate
		MutableList<T> before(MutableList<T> list, Predicate<? super T> predicate) {
			return list.sortThis().reject(predicate);
		}

		@AfterTemplate
		MutableList<T> after(MutableList<T> list, Predicate<? super T> predicate) {
			return list.reject(predicate).sortThis();
		}
	}

	@RecipeDescriptor(
		name = "`sortThis(comparator).reject(pred)` -> `reject(pred).sortThis(comparator)`",
		description = "Converts `list.sortThis(comparator).reject(pred)` to `list.reject(pred).sortThis(comparator)` to sort fewer elements."
	)
	public static final class SortThisWithComparatorRejectToRejectSortThis<T> {

		@BeforeTemplate
		MutableList<T> before(MutableList<T> list, Comparator<? super T> comparator, Predicate<? super T> predicate) {
			return list.sortThis(comparator).reject(predicate);
		}

		@AfterTemplate
		MutableList<T> after(MutableList<T> list, Comparator<? super T> comparator, Predicate<? super T> predicate) {
			return list.reject(predicate).sortThis(comparator);
		}
	}
}
