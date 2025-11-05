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

package io.liftwizard.rewrite.eclipse.collections.removal;

import java.util.Collection;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Replace `CollectionAddProcedure.on()` with method reference",
    description = "Replace `CollectionAddProcedure.on(collection)` with `collection::add` method reference."
)
public class CollectionAddProcedureOnToMethodReference {

    @RecipeDescriptor(
        name = "`CollectionAddProcedure.on()` → method reference",
        description = "Replace `CollectionAddProcedure.on(collection)` with `collection::add`."
    )
    public static class CollectionAddProcedureOnToMethodReferenceRecipe<T> {

        @BeforeTemplate
        Procedure<T> collectionAddProcedureOn(Collection<T> collection) {
            return CollectionAddProcedure.on(collection);
        }

        @BeforeTemplate
        Procedure<T> collectionAddProcedureConstructor(Collection<T> collection) {
            return new CollectionAddProcedure<>(collection);
        }

        @AfterTemplate
        Procedure<T> methodReference(Collection<T> collection) {
            return collection::add;
        }
    }
}
