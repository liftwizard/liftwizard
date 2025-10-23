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
import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Replace `CollectionRemoveProcedure.on()` with method reference",
    description = "Replace `CollectionRemoveProcedure.on(collection)` with `collection::remove` method reference."
)
public class CollectionRemoveProcedureOnToMethodReference {

    @RecipeDescriptor(
        name = "`CollectionRemoveProcedure.on()` → method reference",
        description = "Replace `CollectionRemoveProcedure.on(collection)` with `collection::remove`."
    )
    public static class CollectionRemoveProcedureOnToMethodReferenceRecipe<T> {

        @BeforeTemplate
        Procedure<T> collectionRemoveProcedureOn(Collection<T> collection) {
            return CollectionRemoveProcedure.on(collection);
        }

        @BeforeTemplate
        Procedure<T> collectionRemoveProcedureConstructor(Collection<T> collection) {
            return new CollectionRemoveProcedure<>(collection);
        }

        @AfterTemplate
        Procedure<T> methodReference(Collection<T> collection) {
            return collection::remove;
        }
    }
}
