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
    name = "`!isEmpty()` → `notEmpty()`",
    description = "Simplifies negated empty checks: `!iterable.isEmpty()` to `iterable.notEmpty()` and `!iterable.notEmpty()` to `iterable.isEmpty()` for Eclipse Collections types. Note: This Refaster version does not prevent transformations inside isEmpty() or notEmpty() method implementations, which could cause infinite recursion if those methods are implemented using negated empty checks."
)
public class ECSimplifyNegatedEmptyChecks {

    @RecipeDescriptor(
        name = "`!isEmpty()` → `notEmpty()`",
        description = "Converts `!iterable.isEmpty()` to `iterable.notEmpty()`."
    )
    public static final class NegatedIsEmptyToNotEmpty<T> {

        @BeforeTemplate
        boolean before(RichIterable<T> iterable) {
            return !iterable.isEmpty();
        }

        @AfterTemplate
        boolean after(RichIterable<T> iterable) {
            return iterable.notEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`!notEmpty()` → `isEmpty()`",
        description = "Converts `!iterable.notEmpty()` to `iterable.isEmpty()`."
    )
    public static final class NegatedNotEmptyToIsEmpty<T> {

        @BeforeTemplate
        boolean before(RichIterable<T> iterable) {
            return !iterable.notEmpty();
        }

        @AfterTemplate
        boolean after(RichIterable<T> iterable) {
            return iterable.isEmpty();
        }
    }
}
