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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Replace Collections.empty*() with Eclipse Collections factories",
    description = "Replace `Collections.emptyList()`, `Collections.emptySet()`, and `Collections.emptyMap()` with Eclipse Collections factory methods."
)
public class CollectionsEmptyToFactory {

    @RecipeDescriptor(
        name = "`Collections.emptyList()` → `Lists.fixedSize.empty()`",
        description = "Replace `Collections.emptyList()` with `Lists.fixedSize.empty()`."
    )
    public static class CollectionsEmptyListToFactory<T> {

        @BeforeTemplate
        List<T> emptyList() {
            return Collections.emptyList();
        }

        @AfterTemplate
        List<T> listsFixedSizeEmpty() {
            return Lists.fixedSize.empty();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.emptySet()` → `Sets.fixedSize.empty()`",
        description = "Replace `Collections.emptySet()` with `Sets.fixedSize.empty()`."
    )
    public static class CollectionsEmptySetToFactory<T> {

        @BeforeTemplate
        Set<T> emptySet() {
            return Collections.emptySet();
        }

        @AfterTemplate
        Set<T> setsFixedSizeEmpty() {
            return Sets.fixedSize.empty();
        }
    }

    @RecipeDescriptor(
        name = "`Collections.emptyMap()` → `Maps.fixedSize.empty()`",
        description = "Replace `Collections.emptyMap()` with `Maps.fixedSize.empty()`."
    )
    public static class CollectionsEmptyMapToFactory<K, V> {

        @BeforeTemplate
        Map<K, V> emptyMap() {
            return Collections.emptyMap();
        }

        @AfterTemplate
        Map<K, V> mapsFixedSizeEmpty() {
            return Maps.fixedSize.empty();
        }
    }
}
