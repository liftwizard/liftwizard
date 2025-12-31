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
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`count() == 0` → `noneSatisfy()`",
	description = "Converts count() comparisons to more efficient satisfies methods for Eclipse Collections types. "
	+ "Handles patterns like `count(predicate) == 0` -> `noneSatisfy(predicate)`, `count(predicate) > 0` -> `anySatisfy(predicate)`, "
	+ "`count(predicate) != 0` -> `anySatisfy(predicate)`, `count(predicate) <= 0` -> `noneSatisfy(predicate)`, "
	+ "and `count(predicate) >= 1` -> `anySatisfy(predicate)`."
)
public class ECCountToSatisfies {

	@RecipeDescriptor(
		name = "`count(predicate) == 0` → `noneSatisfy(predicate)`",
		description = "Converts `iterable.count(predicate) == 0` and `0 == iterable.count(predicate)` to `iterable.noneSatisfy(predicate)`."
	)
	public static final class CountEqualsZeroToNoneSatisfy<T> {

		@BeforeTemplate
		boolean beforeCountEqualsZero(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate) == 0;
		}

		@BeforeTemplate
		boolean beforeZeroEqualsCount(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return 0 == iterable.count(predicate);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.noneSatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`count(predicate) > 0` → `anySatisfy(predicate)`",
		description = "Converts `iterable.count(predicate) > 0` and `0 < iterable.count(predicate)` to `iterable.anySatisfy(predicate)`."
	)
	public static final class CountGreaterThanZeroToAnySatisfy<T> {

		@BeforeTemplate
		boolean beforeCountGreaterThanZero(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate) > 0;
		}

		@BeforeTemplate
		boolean beforeZeroLessThanCount(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return 0 < iterable.count(predicate);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.anySatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`count(predicate) != 0` → `anySatisfy(predicate)`",
		description = "Converts `iterable.count(predicate) != 0` and `0 != iterable.count(predicate)` to `iterable.anySatisfy(predicate)`."
	)
	public static final class CountNotEqualsZeroToAnySatisfy<T> {

		@BeforeTemplate
		boolean beforeCountNotEqualsZero(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate) != 0;
		}

		@BeforeTemplate
		boolean beforeZeroNotEqualsCount(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return 0 != iterable.count(predicate);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.anySatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`count(predicate) <= 0` → `noneSatisfy(predicate)`",
		description = "Converts `iterable.count(predicate) <= 0` and `0 >= iterable.count(predicate)` to `iterable.noneSatisfy(predicate)`."
	)
	public static final class CountLessThanOrEqualZeroToNoneSatisfy<T> {

		@BeforeTemplate
		boolean beforeCountLessThanOrEqualZero(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate) <= 0;
		}

		@BeforeTemplate
		boolean beforeZeroGreaterThanOrEqualCount(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return 0 >= iterable.count(predicate);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.noneSatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`count(predicate) >= 1` → `anySatisfy(predicate)`",
		description = "Converts `iterable.count(predicate) >= 1` and `1 <= iterable.count(predicate)` to `iterable.anySatisfy(predicate)`."
	)
	public static final class CountGreaterThanOrEqualOneToAnySatisfy<T> {

		@BeforeTemplate
		boolean beforeCountGreaterThanOrEqualOne(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate) >= 1;
		}

		@BeforeTemplate
		boolean beforeOneLessThanOrEqualCount(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return 1 <= iterable.count(predicate);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.anySatisfy(predicate);
		}
	}
}
