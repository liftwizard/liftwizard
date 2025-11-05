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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import java.util.Collection;
import java.util.List;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.impl.utility.Iterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Collection first element access → `Iterate.getFirst()`",
    description = "Replace iterator().next() and listIterator().next() calls with " +
    "`Iterate.getFirst()` for safer and more expressive first element access."
)
public class IterateGetFirst {

    @RecipeDescriptor(
        name = "`collection.iterator().next()` → " + "`Iterate.getFirst(collection)`",
        description = "Replace iterator().next() with " +
        "`Iterate.getFirst(collection)` for safer first element access."
    )
    public static final class IteratorNextPattern<T, C extends Collection<T>> {

        @BeforeTemplate
        T before(C collection) {
            return collection.iterator().next();
        }

        @AfterTemplate
        T after(C collection) {
            return Iterate.getFirst(collection);
        }
    }

    @RecipeDescriptor(
        name = "`list.listIterator().next()` → " + "`Iterate.getFirst(list)`",
        description = "Replace listIterator().next() with " + "`Iterate.getFirst(list)` for safer first element access."
    )
    public static final class ListIteratorNextPattern<T, L extends List<T>> {

        @BeforeTemplate
        T before(L list) {
            return list.listIterator().next();
        }

        @AfterTemplate
        T after(L list) {
            return Iterate.getFirst(list);
        }
    }
}
