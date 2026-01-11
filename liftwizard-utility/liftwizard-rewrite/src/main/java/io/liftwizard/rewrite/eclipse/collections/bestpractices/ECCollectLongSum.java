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
import org.eclipse.collections.api.block.function.primitive.LongFunction;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
	name = "`collectLong(fn).sum()` -> `sumOfLong(fn)`",
	description = "Transforms `iterable.collectLong(fn).sum()` to `iterable.sumOfLong(fn)` for Eclipse Collections types. This avoids the intermediate primitive collection allocation."
)
public class ECCollectLongSum {

	@RecipeDescriptor(
		name = "`collectLong(fn).sum()` -> `sumOfLong(fn)`",
		description = "Converts `iterable.collectLong(fn).sum()` to `iterable.sumOfLong(fn)`."
	)
	public static final class CollectLongSumToSumOfLong<T> {

		@BeforeTemplate
		long before(RichIterable<T> iterable, LongFunction<? super T> function) {
			return iterable.collectLong(function).sum();
		}

		@AfterTemplate
		long after(RichIterable<T> iterable, LongFunction<? super T> function) {
			return iterable.sumOfLong(function);
		}
	}
}
