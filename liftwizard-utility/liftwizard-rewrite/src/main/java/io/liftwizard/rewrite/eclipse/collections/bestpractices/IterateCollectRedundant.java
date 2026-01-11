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
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.impl.utility.Iterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`Iterate.collect(richIterable, function)` -> `richIterable.collect(function)`",
	description = "Transforms `Iterate.collect(richIterable, function)` to `richIterable.collect(function)` when the iterable is already an Eclipse Collections RichIterable. The Iterate utility is for JCF interop; it is redundant when used with EC types."
)
public class IterateCollectRedundant {

	@RecipeDescriptor(
		name = "`Iterate.collect(richIterable, function)` -> `richIterable.collect(function)`",
		description = "Converts `Iterate.collect(richIterable, function)` to `richIterable.collect(function)` when the iterable is a RichIterable."
	)
	public static final class IterateCollectToRichIterableCollect<T, V> {

		@BeforeTemplate
		Collection<V> before(RichIterable<T> iterable, Function<? super T, ? extends V> function) {
			return Iterate.collect(iterable, function);
		}

		@AfterTemplate
		RichIterable<V> after(RichIterable<T> iterable, Function<? super T, ? extends V> function) {
			return iterable.collect(function);
		}
	}
}
