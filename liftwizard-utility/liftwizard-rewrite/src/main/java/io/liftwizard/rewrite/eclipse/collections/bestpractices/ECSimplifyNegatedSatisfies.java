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
    name = "`!noneSatisfy()` → `anySatisfy()`",
    description = "Simplifies negated satisfies checks: `!iterable.noneSatisfy(predicate)` to `iterable.anySatisfy(predicate)` and `!iterable.anySatisfy(predicate)` to `iterable.noneSatisfy(predicate)` for Eclipse Collections types."
)
public class ECSimplifyNegatedSatisfies {

    @RecipeDescriptor(
        name = "`!noneSatisfy()` → `anySatisfy()`",
        description = "Converts `!iterable.noneSatisfy(predicate)` to `iterable.anySatisfy(predicate)`."
    )
    public static final class NegatedNoneSatisfyToAnySatisfy<T> {

        @BeforeTemplate
        boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return !iterable.noneSatisfy(predicate);
        }

        @AfterTemplate
        boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return iterable.anySatisfy(predicate);
        }
    }

    @RecipeDescriptor(
        name = "`!anySatisfy()` → `noneSatisfy()`",
        description = "Converts `!iterable.anySatisfy(predicate)` to `iterable.noneSatisfy(predicate)`."
    )
    public static final class NegatedAnySatisfyToNoneSatisfy<T> {

        @BeforeTemplate
        boolean before(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return !iterable.anySatisfy(predicate);
        }

        @AfterTemplate
        boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return iterable.noneSatisfy(predicate);
        }
    }
}
