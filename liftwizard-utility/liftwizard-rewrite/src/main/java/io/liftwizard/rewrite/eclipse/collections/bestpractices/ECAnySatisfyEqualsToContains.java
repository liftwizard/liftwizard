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
	name = "`anySatisfy(value::equals)` to `contains(value)`",
	description = "Converts `iterable.anySatisfy(value::equals)` to `iterable.contains(value)` for Eclipse Collections types. "
	+ "The contains() method is simpler and more readable, and may be faster for indexed collections."
)
public class ECAnySatisfyEqualsToContains {

	@RecipeDescriptor(
		name = "`anySatisfy(value::equals)` to `contains(value)`",
		description = "Converts `iterable.anySatisfy(value::equals)` to `iterable.contains(value)`."
	)
	public static final class AnySatisfyEqualsToContains<T, S> {

		@BeforeTemplate
		boolean before(RichIterable<T> iterable, S value) {
			return iterable.anySatisfy(value::equals);
		}

		@AfterTemplate
		boolean after(RichIterable<T> iterable, S value) {
			return iterable.contains(value);
		}
	}
}
