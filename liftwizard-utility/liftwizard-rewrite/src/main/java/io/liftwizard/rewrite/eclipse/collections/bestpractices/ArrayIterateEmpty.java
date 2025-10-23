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
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Array empty checks → `ArrayIterate`",
    description = "Replace manual array null and length checks with " +
    "`ArrayIterate.isEmpty()` and `ArrayIterate.notEmpty()`."
)
public class ArrayIterateEmpty {

    @RecipeDescriptor(
        name = "`array == null || array.length == 0` → " + "`ArrayIterate.isEmpty(array)`",
        description = "Replace manual null or empty check with " + "`ArrayIterate.isEmpty(array)`."
    )
    public static final class IsEmptyPattern<T> {

        @BeforeTemplate
        boolean before(T[] array) {
            return array == null || array.length == 0;
        }

        @AfterTemplate
        boolean after(T[] array) {
            return ArrayIterate.isEmpty(array);
        }
    }

    @RecipeDescriptor(
        name = "`array != null && array.length > 0` → " + "`ArrayIterate.notEmpty(array)`",
        description = "Replace manual not-null and not-empty check with " + "`ArrayIterate.notEmpty(array)`."
    )
    public static final class NotEmptyPattern<T> {

        @BeforeTemplate
        boolean before(T[] array) {
            return array != null && array.length > 0;
        }

        @AfterTemplate
        boolean after(T[] array) {
            return ArrayIterate.notEmpty(array);
        }
    }
}
