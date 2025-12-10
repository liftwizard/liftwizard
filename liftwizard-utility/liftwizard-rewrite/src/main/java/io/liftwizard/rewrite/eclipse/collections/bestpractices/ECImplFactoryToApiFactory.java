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
import org.eclipse.collections.api.factory.bag.ImmutableBagFactory;
import org.eclipse.collections.api.factory.bag.MutableBagFactory;
import org.eclipse.collections.api.factory.bag.sorted.ImmutableSortedBagFactory;
import org.eclipse.collections.api.factory.bag.sorted.MutableSortedBagFactory;
import org.eclipse.collections.api.factory.list.FixedSizeListFactory;
import org.eclipse.collections.api.factory.list.ImmutableListFactory;
import org.eclipse.collections.api.factory.list.MutableListFactory;
import org.eclipse.collections.api.factory.map.FixedSizeMapFactory;
import org.eclipse.collections.api.factory.map.ImmutableMapFactory;
import org.eclipse.collections.api.factory.map.MutableMapFactory;
import org.eclipse.collections.api.factory.map.sorted.ImmutableSortedMapFactory;
import org.eclipse.collections.api.factory.map.sorted.MutableSortedMapFactory;
import org.eclipse.collections.api.factory.set.FixedSizeSetFactory;
import org.eclipse.collections.api.factory.set.ImmutableSetFactory;
import org.eclipse.collections.api.factory.set.MutableSetFactory;
import org.eclipse.collections.api.factory.set.sorted.ImmutableSortedSetFactory;
import org.eclipse.collections.api.factory.set.sorted.MutableSortedSetFactory;
import org.eclipse.collections.api.factory.stack.ImmutableStackFactory;
import org.eclipse.collections.api.factory.stack.MutableStackFactory;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Replace impl.factory with api.factory",
    description = "Replace org.eclipse.collections.impl.factory.* factory field access "
        + "with api.factory equivalents. Does not transform static utility methods like "
        + "Sets.union(), Lists.adapt(), etc."
)
public class ECImplFactoryToApiFactory {

    @RecipeDescriptor(
        name = "`impl.factory.Lists.mutable` → `api.factory.Lists.mutable`",
        description = "Replace impl.factory.Lists.mutable with api.factory.Lists.mutable."
    )
    public static class ListsMutable {
        @BeforeTemplate
        MutableListFactory before() {
            return org.eclipse.collections.impl.factory.Lists.mutable;
        }

        @AfterTemplate
        MutableListFactory after() {
            return org.eclipse.collections.api.factory.Lists.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Lists.immutable` → `api.factory.Lists.immutable`",
        description = "Replace impl.factory.Lists.immutable with api.factory.Lists.immutable."
    )
    public static class ListsImmutable {
        @BeforeTemplate
        ImmutableListFactory before() {
            return org.eclipse.collections.impl.factory.Lists.immutable;
        }

        @AfterTemplate
        ImmutableListFactory after() {
            return org.eclipse.collections.api.factory.Lists.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Lists.fixedSize` → `api.factory.Lists.fixedSize`",
        description = "Replace impl.factory.Lists.fixedSize with api.factory.Lists.fixedSize."
    )
    public static class ListsFixedSize {
        @BeforeTemplate
        FixedSizeListFactory before() {
            return org.eclipse.collections.impl.factory.Lists.fixedSize;
        }

        @AfterTemplate
        FixedSizeListFactory after() {
            return org.eclipse.collections.api.factory.Lists.fixedSize;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Sets.mutable` → `api.factory.Sets.mutable`",
        description = "Replace impl.factory.Sets.mutable with api.factory.Sets.mutable."
    )
    public static class SetsMutable {
        @BeforeTemplate
        MutableSetFactory before() {
            return org.eclipse.collections.impl.factory.Sets.mutable;
        }

        @AfterTemplate
        MutableSetFactory after() {
            return org.eclipse.collections.api.factory.Sets.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Sets.immutable` → `api.factory.Sets.immutable`",
        description = "Replace impl.factory.Sets.immutable with api.factory.Sets.immutable."
    )
    public static class SetsImmutable {
        @BeforeTemplate
        ImmutableSetFactory before() {
            return org.eclipse.collections.impl.factory.Sets.immutable;
        }

        @AfterTemplate
        ImmutableSetFactory after() {
            return org.eclipse.collections.api.factory.Sets.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Sets.fixedSize` → `api.factory.Sets.fixedSize`",
        description = "Replace impl.factory.Sets.fixedSize with api.factory.Sets.fixedSize."
    )
    public static class SetsFixedSize {
        @BeforeTemplate
        FixedSizeSetFactory before() {
            return org.eclipse.collections.impl.factory.Sets.fixedSize;
        }

        @AfterTemplate
        FixedSizeSetFactory after() {
            return org.eclipse.collections.api.factory.Sets.fixedSize;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Maps.mutable` → `api.factory.Maps.mutable`",
        description = "Replace impl.factory.Maps.mutable with api.factory.Maps.mutable."
    )
    public static class MapsMutable {
        @BeforeTemplate
        MutableMapFactory before() {
            return org.eclipse.collections.impl.factory.Maps.mutable;
        }

        @AfterTemplate
        MutableMapFactory after() {
            return org.eclipse.collections.api.factory.Maps.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Maps.immutable` → `api.factory.Maps.immutable`",
        description = "Replace impl.factory.Maps.immutable with api.factory.Maps.immutable."
    )
    public static class MapsImmutable {
        @BeforeTemplate
        ImmutableMapFactory before() {
            return org.eclipse.collections.impl.factory.Maps.immutable;
        }

        @AfterTemplate
        ImmutableMapFactory after() {
            return org.eclipse.collections.api.factory.Maps.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Maps.fixedSize` → `api.factory.Maps.fixedSize`",
        description = "Replace impl.factory.Maps.fixedSize with api.factory.Maps.fixedSize."
    )
    public static class MapsFixedSize {
        @BeforeTemplate
        FixedSizeMapFactory before() {
            return org.eclipse.collections.impl.factory.Maps.fixedSize;
        }

        @AfterTemplate
        FixedSizeMapFactory after() {
            return org.eclipse.collections.api.factory.Maps.fixedSize;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Bags.mutable` → `api.factory.Bags.mutable`",
        description = "Replace impl.factory.Bags.mutable with api.factory.Bags.mutable."
    )
    public static class BagsMutable {
        @BeforeTemplate
        MutableBagFactory before() {
            return org.eclipse.collections.impl.factory.Bags.mutable;
        }

        @AfterTemplate
        MutableBagFactory after() {
            return org.eclipse.collections.api.factory.Bags.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Bags.immutable` → `api.factory.Bags.immutable`",
        description = "Replace impl.factory.Bags.immutable with api.factory.Bags.immutable."
    )
    public static class BagsImmutable {
        @BeforeTemplate
        ImmutableBagFactory before() {
            return org.eclipse.collections.impl.factory.Bags.immutable;
        }

        @AfterTemplate
        ImmutableBagFactory after() {
            return org.eclipse.collections.api.factory.Bags.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Stacks.mutable` → `api.factory.Stacks.mutable`",
        description = "Replace impl.factory.Stacks.mutable with api.factory.Stacks.mutable."
    )
    public static class StacksMutable {
        @BeforeTemplate
        MutableStackFactory before() {
            return org.eclipse.collections.impl.factory.Stacks.mutable;
        }

        @AfterTemplate
        MutableStackFactory after() {
            return org.eclipse.collections.api.factory.Stacks.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.Stacks.immutable` → `api.factory.Stacks.immutable`",
        description = "Replace impl.factory.Stacks.immutable with api.factory.Stacks.immutable."
    )
    public static class StacksImmutable {
        @BeforeTemplate
        ImmutableStackFactory before() {
            return org.eclipse.collections.impl.factory.Stacks.immutable;
        }

        @AfterTemplate
        ImmutableStackFactory after() {
            return org.eclipse.collections.api.factory.Stacks.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.SortedSets.mutable` → `api.factory.SortedSets.mutable`",
        description = "Replace impl.factory.SortedSets.mutable with api.factory.SortedSets.mutable."
    )
    public static class SortedSetsMutable {
        @BeforeTemplate
        MutableSortedSetFactory before() {
            return org.eclipse.collections.impl.factory.SortedSets.mutable;
        }

        @AfterTemplate
        MutableSortedSetFactory after() {
            return org.eclipse.collections.api.factory.SortedSets.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.SortedSets.immutable` → `api.factory.SortedSets.immutable`",
        description = "Replace impl.factory.SortedSets.immutable with api.factory.SortedSets.immutable."
    )
    public static class SortedSetsImmutable {
        @BeforeTemplate
        ImmutableSortedSetFactory before() {
            return org.eclipse.collections.impl.factory.SortedSets.immutable;
        }

        @AfterTemplate
        ImmutableSortedSetFactory after() {
            return org.eclipse.collections.api.factory.SortedSets.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.SortedMaps.mutable` → `api.factory.SortedMaps.mutable`",
        description = "Replace impl.factory.SortedMaps.mutable with api.factory.SortedMaps.mutable."
    )
    public static class SortedMapsMutable {
        @BeforeTemplate
        MutableSortedMapFactory before() {
            return org.eclipse.collections.impl.factory.SortedMaps.mutable;
        }

        @AfterTemplate
        MutableSortedMapFactory after() {
            return org.eclipse.collections.api.factory.SortedMaps.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.SortedMaps.immutable` → `api.factory.SortedMaps.immutable`",
        description = "Replace impl.factory.SortedMaps.immutable with api.factory.SortedMaps.immutable."
    )
    public static class SortedMapsImmutable {
        @BeforeTemplate
        ImmutableSortedMapFactory before() {
            return org.eclipse.collections.impl.factory.SortedMaps.immutable;
        }

        @AfterTemplate
        ImmutableSortedMapFactory after() {
            return org.eclipse.collections.api.factory.SortedMaps.immutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.SortedBags.mutable` → `api.factory.SortedBags.mutable`",
        description = "Replace impl.factory.SortedBags.mutable with api.factory.SortedBags.mutable."
    )
    public static class SortedBagsMutable {
        @BeforeTemplate
        MutableSortedBagFactory before() {
            return org.eclipse.collections.impl.factory.SortedBags.mutable;
        }

        @AfterTemplate
        MutableSortedBagFactory after() {
            return org.eclipse.collections.api.factory.SortedBags.mutable;
        }
    }

    @RecipeDescriptor(
        name = "`impl.factory.SortedBags.immutable` → `api.factory.SortedBags.immutable`",
        description = "Replace impl.factory.SortedBags.immutable with api.factory.SortedBags.immutable."
    )
    public static class SortedBagsImmutable {
        @BeforeTemplate
        ImmutableSortedBagFactory before() {
            return org.eclipse.collections.impl.factory.SortedBags.immutable;
        }

        @AfterTemplate
        ImmutableSortedBagFactory after() {
            return org.eclipse.collections.api.factory.SortedBags.immutable;
        }
    }
}
