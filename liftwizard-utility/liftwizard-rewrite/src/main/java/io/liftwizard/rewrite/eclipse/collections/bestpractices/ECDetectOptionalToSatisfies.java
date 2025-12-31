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
	name = "`detectOptional().isPresent()` → `anySatisfy()`",
	description = "Converts `iterable.detectOptional(predicate).isPresent()` to `iterable.anySatisfy(predicate)`, "
	+ "`!iterable.detectOptional(predicate).isPresent()` to `iterable.noneSatisfy(predicate)`, "
	+ "`iterable.detectOptional(predicate).isEmpty()` to `iterable.noneSatisfy(predicate)`, and "
	+ "`!iterable.detectOptional(predicate).isEmpty()` to `iterable.anySatisfy(predicate)` for Eclipse Collections types."
)
public class ECDetectOptionalToSatisfies {

	@RecipeDescriptor(
		name = "`!detectOptional(predicate).isPresent()` → `noneSatisfy(predicate)`",
		description = "Converts `!iterable.detectOptional(predicate).isPresent()` to `iterable.noneSatisfy(predicate)`."
	)
	public static final class NegatedDetectOptionalIsPresentToNoneSatisfy<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return !iterable.detectOptional(predicate).isPresent();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.noneSatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`!detectOptional(predicate).isEmpty()` → `anySatisfy(predicate)`",
		description = "Converts `!iterable.detectOptional(predicate).isEmpty()` to `iterable.anySatisfy(predicate)`."
	)
	public static final class NegatedDetectOptionalIsEmptyToAnySatisfy<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return !iterable.detectOptional(predicate).isEmpty();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.anySatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`detectOptional(predicate).isPresent()` → `anySatisfy(predicate)`",
		description = "Converts `iterable.detectOptional(predicate).isPresent()` to `iterable.anySatisfy(predicate)`."
	)
	public static final class DetectOptionalIsPresentToAnySatisfy<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.detectOptional(predicate).isPresent();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.anySatisfy(predicate);
		}
	}

	@RecipeDescriptor(
		name = "`detectOptional(predicate).isEmpty()` → `noneSatisfy(predicate)`",
		description = "Converts `iterable.detectOptional(predicate).isEmpty()` to `iterable.noneSatisfy(predicate)`."
	)
	public static final class DetectOptionalIsEmptyToNoneSatisfy<T> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.detectOptional(predicate).isEmpty();
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.noneSatisfy(predicate);
		}
	}
}
