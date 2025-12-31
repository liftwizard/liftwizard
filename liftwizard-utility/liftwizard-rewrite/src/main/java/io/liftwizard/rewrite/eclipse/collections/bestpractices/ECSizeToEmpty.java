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
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`size() == 0` → `isEmpty()`",
	description = "Converts size() comparisons to more idiomatic isEmpty() and notEmpty() method calls for Eclipse Collections types. Handles patterns like `size() == 0` -> `isEmpty()`, `size() > 0` -> `notEmpty()`, `size() >= 1` -> `notEmpty()`, etc. Note: This Refaster version does not prevent transformations inside isEmpty() or notEmpty() method implementations, which could cause infinite recursion if those methods are implemented using size() comparisons."
)
public class ECSizeToEmpty {

	@RecipeDescriptor(
		name = "`size() == 0` → `isEmpty()`",
		description = "Converts `iterable.size() == 0` to `iterable.isEmpty()`."
	)
	public static final class SizeEqualsZeroToIsEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return iterable.size() == 0;
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.isEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`0 == size()` → `isEmpty()`",
		description = "Converts `0 == iterable.size()` to `iterable.isEmpty()`."
	)
	public static final class ZeroEqualsSizeToIsEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return 0 == iterable.size();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.isEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`size() < 1` → `isEmpty()`",
		description = "Converts `iterable.size() < 1` to `iterable.isEmpty()`."
	)
	public static final class SizeLessThanOneToIsEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return iterable.size() < 1;
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.isEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`1 > size()` → `isEmpty()`",
		description = "Converts `1 > iterable.size()` to `iterable.isEmpty()`."
	)
	public static final class OneGreaterThanSizeToIsEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return 1 > iterable.size();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.isEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`size() <= 0` → `isEmpty()`",
		description = "Converts `iterable.size() <= 0` to `iterable.isEmpty()`."
	)
	public static final class SizeLessThanOrEqualZeroToIsEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return iterable.size() <= 0;
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.isEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`0 >= size()` → `isEmpty()`",
		description = "Converts `0 >= iterable.size()` to `iterable.isEmpty()`."
	)
	public static final class ZeroGreaterThanOrEqualSizeToIsEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return 0 >= iterable.size();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.isEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`size() > 0` → `notEmpty()`",
		description = "Converts `iterable.size() > 0` to `iterable.notEmpty()`."
	)
	public static final class SizeGreaterThanZeroToNotEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return iterable.size() > 0;
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.notEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`0 < size()` → `notEmpty()`",
		description = "Converts `0 < iterable.size()` to `iterable.notEmpty()`."
	)
	public static final class ZeroLessThanSizeToNotEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return 0 < iterable.size();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.notEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`size() != 0` → `notEmpty()`",
		description = "Converts `iterable.size() != 0` to `iterable.notEmpty()`."
	)
	public static final class SizeNotEqualsZeroToNotEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return iterable.size() != 0;
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.notEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`0 != size()` → `notEmpty()`",
		description = "Converts `0 != iterable.size()` to `iterable.notEmpty()`."
	)
	public static final class ZeroNotEqualsSizeToNotEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return 0 != iterable.size();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.notEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`size() >= 1` → `notEmpty()`",
		description = "Converts `iterable.size() >= 1` to `iterable.notEmpty()`."
	)
	public static final class SizeGreaterThanOrEqualOneToNotEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return iterable.size() >= 1;
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.notEmpty();
		}
	}

	@RecipeDescriptor(
		name = "`1 <= size()` → `notEmpty()`",
		description = "Converts `1 <= iterable.size()` to `iterable.notEmpty()`."
	)
	public static final class OneLessThanOrEqualSizeToNotEmpty<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable) {
			return 1 <= iterable.size();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable) {
			return iterable.notEmpty();
		}
	}
}
