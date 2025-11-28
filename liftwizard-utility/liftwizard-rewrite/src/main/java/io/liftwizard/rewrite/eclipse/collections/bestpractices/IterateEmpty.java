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
import org.eclipse.collections.impl.utility.Iterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Collection empty checks → `Iterate`",
    description = "Replace manual collection null and isEmpty checks with "
    + "`Iterate.isEmpty()` and `Iterate.notEmpty()`."
)
public class IterateEmpty {

    @RecipeDescriptor(
        name = "`collection == null || collection.isEmpty()` → " + "`Iterate.isEmpty(collection)`",
        description = "Replace manual null or empty check with " + "`Iterate.isEmpty(collection)`."
    )
    public static final class IsEmptyPattern<T extends Collection<?>> {

        @BeforeTemplate
        boolean before(T collection) {
            return collection == null || collection.isEmpty();
        }

        @AfterTemplate
        boolean after(T collection) {
            return Iterate.isEmpty(collection);
        }
    }

    @RecipeDescriptor(
        name = "`collection != null && !collection.isEmpty()` → " + "`Iterate.notEmpty(collection)`",
        description = "Replace manual not-null and not-empty check with " + "`Iterate.notEmpty(collection)`."
    )
    public static final class NotEmptyPattern<T extends Collection<?>> {

        @BeforeTemplate
        boolean before(T collection) {
            return collection != null && !collection.isEmpty();
        }

        @AfterTemplate
        boolean after(T collection) {
            return Iterate.notEmpty(collection);
        }
    }

    @RecipeDescriptor(
        name = "`!Iterate.isEmpty()` → `Iterate.notEmpty()`",
        description = "Converts `!Iterate.isEmpty(iterable)` to `Iterate.notEmpty(iterable)`."
    )
    public static final class NegatedIterateIsEmptyToNotEmpty<T> {

        @BeforeTemplate
        boolean before(Iterable<T> iterable) {
            return !Iterate.isEmpty(iterable);
        }

        @AfterTemplate
        boolean after(Iterable<T> iterable) {
            return Iterate.notEmpty(iterable);
        }
    }

    @RecipeDescriptor(
        name = "`!Iterate.notEmpty()` → `Iterate.isEmpty()`",
        description = "Converts `!Iterate.notEmpty(iterable)` to `Iterate.isEmpty(iterable)`."
    )
    public static final class NegatedIterateNotEmptyToIsEmpty<T> {

        @BeforeTemplate
        boolean before(Iterable<T> iterable) {
            return !Iterate.notEmpty(iterable);
        }

        @AfterTemplate
        boolean after(Iterable<T> iterable) {
            return Iterate.isEmpty(iterable);
        }
    }
}
