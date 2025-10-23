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

import java.util.Arrays;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.api.factory.Bags;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.bag.mutable.HashBag;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "`FastList.newList(Arrays.asList())` → `Lists.mutable.with()`",
    description = "Replace `FastList.newList(Arrays.asList())`, `UnifiedSet.newSet(Arrays.asList())`, and " +
    "`HashBag.newBag(Arrays.asList())` with Eclipse Collections factory methods using varargs. " +
    "Supports arities 0-5."
)
public class ECArraysAsListToWith {

    @RecipeDescriptor(
        name = "`FastList.newList(Arrays.asList())` → `Lists.mutable.with()` (0 args)",
        description = "Replace `FastList.newList(Arrays.asList())` with `Lists.mutable.with()`."
    )
    public static final class FastListArity0<T> {

        @BeforeTemplate
        MutableList<T> before() {
            return FastList.newList(Arrays.asList());
        }

        @AfterTemplate
        MutableList<T> after() {
            return Lists.mutable.with();
        }
    }

    @RecipeDescriptor(
        name = "`FastList.newList(Arrays.asList(a))` → `Lists.mutable.with(a)` (1 arg)",
        description = "Replace `FastList.newList(Arrays.asList(a))` with `Lists.mutable.with(a)`."
    )
    public static final class FastListArity1<T> {

        @BeforeTemplate
        MutableList<T> before(T a) {
            return FastList.newList(Arrays.asList(a));
        }

        @AfterTemplate
        MutableList<T> after(T a) {
            return Lists.mutable.with(a);
        }
    }

    @RecipeDescriptor(
        name = "`FastList.newList(Arrays.asList(a, b))` → `Lists.mutable.with(a, b)` (2 args)",
        description = "Replace `FastList.newList(Arrays.asList(a, b))` with `Lists.mutable.with(a, b)`."
    )
    public static final class FastListArity2<T> {

        @BeforeTemplate
        MutableList<T> before(T a, T b) {
            return FastList.newList(Arrays.asList(a, b));
        }

        @AfterTemplate
        MutableList<T> after(T a, T b) {
            return Lists.mutable.with(a, b);
        }
    }

    @RecipeDescriptor(
        name = "`FastList.newList(Arrays.asList(a, b, c))` → `Lists.mutable.with(a, b, c)` (3 args)",
        description = "Replace `FastList.newList(Arrays.asList(a, b, c))` with `Lists.mutable.with(a, b, c)`."
    )
    public static final class FastListArity3<T> {

        @BeforeTemplate
        MutableList<T> before(T a, T b, T c) {
            return FastList.newList(Arrays.asList(a, b, c));
        }

        @AfterTemplate
        MutableList<T> after(T a, T b, T c) {
            return Lists.mutable.with(a, b, c);
        }
    }

    @RecipeDescriptor(
        name = "`FastList.newList(Arrays.asList(a, b, c, d))` → `Lists.mutable.with(a, b, c, d)` (4 args)",
        description = "Replace `FastList.newList(Arrays.asList(a, b, c, d))` with `Lists.mutable.with(a, b, c, d)`."
    )
    public static final class FastListArity4<T> {

        @BeforeTemplate
        MutableList<T> before(T a, T b, T c, T d) {
            return FastList.newList(Arrays.asList(a, b, c, d));
        }

        @AfterTemplate
        MutableList<T> after(T a, T b, T c, T d) {
            return Lists.mutable.with(a, b, c, d);
        }
    }

    @RecipeDescriptor(
        name = "`FastList.newList(Arrays.asList(a, b, c, d, e))` → `Lists.mutable.with(a, b, c, d, e)` (5 args)",
        description = "Replace `FastList.newList(Arrays.asList(a, b, c, d, e))` with `Lists.mutable.with(a, b, c, d, e)`."
    )
    public static final class FastListArity5<T> {

        @BeforeTemplate
        MutableList<T> before(T a, T b, T c, T d, T e) {
            return FastList.newList(Arrays.asList(a, b, c, d, e));
        }

        @AfterTemplate
        MutableList<T> after(T a, T b, T c, T d, T e) {
            return Lists.mutable.with(a, b, c, d, e);
        }
    }

    @RecipeDescriptor(
        name = "`UnifiedSet.newSet(Arrays.asList())` → `Sets.mutable.with()` (0 args)",
        description = "Replace `UnifiedSet.newSet(Arrays.asList())` with `Sets.mutable.with()`."
    )
    public static final class UnifiedSetArity0<T> {

        @BeforeTemplate
        MutableSet<T> before() {
            return UnifiedSet.newSet(Arrays.asList());
        }

        @AfterTemplate
        MutableSet<T> after() {
            return Sets.mutable.with();
        }
    }

    @RecipeDescriptor(
        name = "`UnifiedSet.newSet(Arrays.asList(a))` → `Sets.mutable.with(a)` (1 arg)",
        description = "Replace `UnifiedSet.newSet(Arrays.asList(a))` with `Sets.mutable.with(a)`."
    )
    public static final class UnifiedSetArity1<T> {

        @BeforeTemplate
        MutableSet<T> before(T a) {
            return UnifiedSet.newSet(Arrays.asList(a));
        }

        @AfterTemplate
        MutableSet<T> after(T a) {
            return Sets.mutable.with(a);
        }
    }

    @RecipeDescriptor(
        name = "`UnifiedSet.newSet(Arrays.asList(a, b))` → `Sets.mutable.with(a, b)` (2 args)",
        description = "Replace `UnifiedSet.newSet(Arrays.asList(a, b))` with `Sets.mutable.with(a, b)`."
    )
    public static final class UnifiedSetArity2<T> {

        @BeforeTemplate
        MutableSet<T> before(T a, T b) {
            return UnifiedSet.newSet(Arrays.asList(a, b));
        }

        @AfterTemplate
        MutableSet<T> after(T a, T b) {
            return Sets.mutable.with(a, b);
        }
    }

    @RecipeDescriptor(
        name = "`UnifiedSet.newSet(Arrays.asList(a, b, c))` → `Sets.mutable.with(a, b, c)` (3 args)",
        description = "Replace `UnifiedSet.newSet(Arrays.asList(a, b, c))` with `Sets.mutable.with(a, b, c)`."
    )
    public static final class UnifiedSetArity3<T> {

        @BeforeTemplate
        MutableSet<T> before(T a, T b, T c) {
            return UnifiedSet.newSet(Arrays.asList(a, b, c));
        }

        @AfterTemplate
        MutableSet<T> after(T a, T b, T c) {
            return Sets.mutable.with(a, b, c);
        }
    }

    @RecipeDescriptor(
        name = "`UnifiedSet.newSet(Arrays.asList(a, b, c, d))` → `Sets.mutable.with(a, b, c, d)` (4 args)",
        description = "Replace `UnifiedSet.newSet(Arrays.asList(a, b, c, d))` with `Sets.mutable.with(a, b, c, d)`."
    )
    public static final class UnifiedSetArity4<T> {

        @BeforeTemplate
        MutableSet<T> before(T a, T b, T c, T d) {
            return UnifiedSet.newSet(Arrays.asList(a, b, c, d));
        }

        @AfterTemplate
        MutableSet<T> after(T a, T b, T c, T d) {
            return Sets.mutable.with(a, b, c, d);
        }
    }

    @RecipeDescriptor(
        name = "`UnifiedSet.newSet(Arrays.asList(a, b, c, d, e))` → `Sets.mutable.with(a, b, c, d, e)` (5 args)",
        description = "Replace `UnifiedSet.newSet(Arrays.asList(a, b, c, d, e))` with `Sets.mutable.with(a, b, c, d, e)`."
    )
    public static final class UnifiedSetArity5<T> {

        @BeforeTemplate
        MutableSet<T> before(T a, T b, T c, T d, T e) {
            return UnifiedSet.newSet(Arrays.asList(a, b, c, d, e));
        }

        @AfterTemplate
        MutableSet<T> after(T a, T b, T c, T d, T e) {
            return Sets.mutable.with(a, b, c, d, e);
        }
    }

    @RecipeDescriptor(
        name = "`HashBag.newBag(Arrays.asList())` → `Bags.mutable.with()` (0 args)",
        description = "Replace `HashBag.newBag(Arrays.asList())` with `Bags.mutable.with()`."
    )
    public static final class HashBagArity0<T> {

        @BeforeTemplate
        MutableBag<T> before() {
            return HashBag.newBag(Arrays.asList());
        }

        @AfterTemplate
        MutableBag<T> after() {
            return Bags.mutable.with();
        }
    }

    @RecipeDescriptor(
        name = "`HashBag.newBag(Arrays.asList(a))` → `Bags.mutable.with(a)` (1 arg)",
        description = "Replace `HashBag.newBag(Arrays.asList(a))` with `Bags.mutable.with(a)`."
    )
    public static final class HashBagArity1<T> {

        @BeforeTemplate
        MutableBag<T> before(T a) {
            return HashBag.newBag(Arrays.asList(a));
        }

        @AfterTemplate
        MutableBag<T> after(T a) {
            return Bags.mutable.with(a);
        }
    }

    @RecipeDescriptor(
        name = "`HashBag.newBag(Arrays.asList(a, b))` → `Bags.mutable.with(a, b)` (2 args)",
        description = "Replace `HashBag.newBag(Arrays.asList(a, b))` with `Bags.mutable.with(a, b)`."
    )
    public static final class HashBagArity2<T> {

        @BeforeTemplate
        MutableBag<T> before(T a, T b) {
            return HashBag.newBag(Arrays.asList(a, b));
        }

        @AfterTemplate
        MutableBag<T> after(T a, T b) {
            return Bags.mutable.with(a, b);
        }
    }

    @RecipeDescriptor(
        name = "`HashBag.newBag(Arrays.asList(a, b, c))` → `Bags.mutable.with(a, b, c)` (3 args)",
        description = "Replace `HashBag.newBag(Arrays.asList(a, b, c))` with `Bags.mutable.with(a, b, c)`."
    )
    public static final class HashBagArity3<T> {

        @BeforeTemplate
        MutableBag<T> before(T a, T b, T c) {
            return HashBag.newBag(Arrays.asList(a, b, c));
        }

        @AfterTemplate
        MutableBag<T> after(T a, T b, T c) {
            return Bags.mutable.with(a, b, c);
        }
    }

    @RecipeDescriptor(
        name = "`HashBag.newBag(Arrays.asList(a, b, c, d))` → `Bags.mutable.with(a, b, c, d)` (4 args)",
        description = "Replace `HashBag.newBag(Arrays.asList(a, b, c, d))` with `Bags.mutable.with(a, b, c, d)`."
    )
    public static final class HashBagArity4<T> {

        @BeforeTemplate
        MutableBag<T> before(T a, T b, T c, T d) {
            return HashBag.newBag(Arrays.asList(a, b, c, d));
        }

        @AfterTemplate
        MutableBag<T> after(T a, T b, T c, T d) {
            return Bags.mutable.with(a, b, c, d);
        }
    }

    @RecipeDescriptor(
        name = "`HashBag.newBag(Arrays.asList(a, b, c, d, e))` → `Bags.mutable.with(a, b, c, d, e)` (5 args)",
        description = "Replace `HashBag.newBag(Arrays.asList(a, b, c, d, e))` with `Bags.mutable.with(a, b, c, d, e)`."
    )
    public static final class HashBagArity5<T> {

        @BeforeTemplate
        MutableBag<T> before(T a, T b, T c, T d, T e) {
            return HashBag.newBag(Arrays.asList(a, b, c, d, e));
        }

        @AfterTemplate
        MutableBag<T> after(T a, T b, T c, T d, T e) {
            return Bags.mutable.with(a, b, c, d, e);
        }
    }
}
