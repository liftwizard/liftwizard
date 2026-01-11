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
	name = "`count(predicate) == size()` to `allSatisfy(predicate)`",
	description = "Converts count() == size() comparisons to allSatisfy() for Eclipse Collections types. "
	+ "The allSatisfy() method is more readable and can short-circuit on the first non-matching element, "
	+ "while count() == size() must scan the entire collection."
)
public class ECCountEqualsSize {

	@RecipeDescriptor(
		name = "`count(predicate) == size()` to `allSatisfy(predicate)`",
		description = "Converts `iterable.count(predicate) == iterable.size()` and "
		+ "`iterable.size() == iterable.count(predicate)` to `iterable.allSatisfy(predicate)`."
	)
	public static final class CountEqualsSizeToAllSatisfy<T> {

		@BeforeTemplate
		boolean beforeCountEqualsSize(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.count(predicate) == iterable.size();
		}

		@BeforeTemplate
		boolean beforeSizeEqualsCount(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.size() == iterable.count(predicate);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.allSatisfy(predicate);
		}
	}
}
