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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.utility.Iterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`Iterate.reject(richIterable, predicate)` -> `richIterable.reject(predicate)`",
	description = "Transforms `Iterate.reject(richIterable, predicate)` to `richIterable.reject(predicate)` when the iterable is already an Eclipse Collections RichIterable. The Iterate utility is for JCF interop; it is redundant when used with EC types."
)
public class IterateRejectRedundant {

	@RecipeDescriptor(
		name = "`Iterate.reject(richIterable, predicate)` -> `richIterable.reject(predicate)`",
		description = "Converts `Iterate.reject(richIterable, predicate)` to `richIterable.reject(predicate)` when the iterable is a RichIterable."
	)
	public static final class IterateRejectToRichIterableReject<T> {

		@BeforeTemplate
		Collection<T> before(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return Iterate.reject(iterable, predicate);
		}

		@AfterTemplate
		RichIterable<T> after(RichIterable<T> iterable, Predicate<? super T> predicate) {
			return iterable.reject(predicate);
		}
	}
}
