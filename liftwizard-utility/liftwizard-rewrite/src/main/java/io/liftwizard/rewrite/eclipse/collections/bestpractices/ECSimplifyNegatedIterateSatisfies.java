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
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.utility.Iterate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "`!Iterate.noneSatisfy()` → `Iterate.anySatisfy()`",
    description = "Replace `!Iterate.noneSatisfy()` with `Iterate.anySatisfy()`, `!Iterate.anySatisfy()` with `Iterate.noneSatisfy()`, `Iterate.noneSatisfy(iterable, Predicates.not(predicate))` with `Iterate.anySatisfy(iterable, predicate)`, `Iterate.anySatisfy(iterable, Predicates.not(predicate))` with `Iterate.noneSatisfy(iterable, predicate)`, and double-negated forms like `!Iterate.noneSatisfy(iterable, Predicates.not(predicate))` with `Iterate.noneSatisfy(iterable, predicate)`."
)
public class ECSimplifyNegatedIterateSatisfies {

    @RecipeDescriptor(
        name = "`!Iterate.noneSatisfy()` → `Iterate.anySatisfy()`",
        description = "Converts `!Iterate.noneSatisfy(iterable, predicate)` to `Iterate.anySatisfy(iterable, predicate)`. "
        + "Also handles double negation: `Iterate.anySatisfy(iterable, Predicates.not(predicate))` to `Iterate.anySatisfy(iterable, predicate)`."
    )
    public static final class NegatedNoneSatisfyToAnySatisfy<T> {

        @BeforeTemplate
        boolean beforeNegatedNoneSatisfy(Iterable<T> iterable, Predicate<? super T> predicate) {
            return !Iterate.noneSatisfy(iterable, predicate);
        }

        @BeforeTemplate
        boolean beforeDoubleNegation(Iterable<T> iterable, Predicate<? super T> predicate) {
            return !Iterate.anySatisfy(iterable, Predicates.not(predicate));
        }

        @AfterTemplate
        boolean after(Iterable<T> iterable, Predicate<? super T> predicate) {
            return Iterate.anySatisfy(iterable, predicate);
        }
    }

    @RecipeDescriptor(
        name = "`!Iterate.anySatisfy()` → `Iterate.noneSatisfy()`",
        description = "Converts `!Iterate.anySatisfy(iterable, predicate)` to `Iterate.noneSatisfy(iterable, predicate)`. "
        + "Also handles double negation: `!Iterate.noneSatisfy(iterable, Predicates.not(predicate))` to `Iterate.noneSatisfy(iterable, predicate)`."
    )
    public static final class NegatedAnySatisfyToNoneSatisfy<T> {

        @BeforeTemplate
        boolean beforeNegatedAnySatisfy(Iterable<T> iterable, Predicate<? super T> predicate) {
            return !Iterate.anySatisfy(iterable, predicate);
        }

        @BeforeTemplate
        boolean beforeDoubleNegation(Iterable<T> iterable, Predicate<? super T> predicate) {
            return !Iterate.noneSatisfy(iterable, Predicates.not(predicate));
        }

        @AfterTemplate
        boolean after(Iterable<T> iterable, Predicate<? super T> predicate) {
            return Iterate.noneSatisfy(iterable, predicate);
        }
    }

    @RecipeDescriptor(
        name = "`Iterate.noneSatisfy(iterable, Predicates.not(predicate))` → `Iterate.anySatisfy(iterable, predicate)`",
        description = "Converts `Iterate.noneSatisfy(iterable, Predicates.not(predicate))` to `Iterate.anySatisfy(iterable, predicate)`."
    )
    public static final class NoneSatisfyPredicatesNotToAnySatisfy<T> {

        @BeforeTemplate
        boolean before(Iterable<T> iterable, Predicate<? super T> predicate) {
            return Iterate.noneSatisfy(iterable, Predicates.not(predicate));
        }

        @AfterTemplate
        boolean after(Iterable<T> iterable, Predicate<? super T> predicate) {
            return Iterate.anySatisfy(iterable, predicate);
        }
    }

    @RecipeDescriptor(
        name = "`Iterate.anySatisfy(iterable, Predicates.not(predicate))` → `Iterate.noneSatisfy(iterable, predicate)`",
        description = "Converts `Iterate.anySatisfy(iterable, Predicates.not(predicate))` to `Iterate.noneSatisfy(iterable, predicate)`."
    )
    public static final class AnySatisfyPredicatesNotToNoneSatisfy<T> {

        @BeforeTemplate
        boolean before(Iterable<T> iterable, Predicate<? super T> predicate) {
            return Iterate.anySatisfy(iterable, Predicates.not(predicate));
        }

        @AfterTemplate
        boolean after(Iterable<T> iterable, Predicate<? super T> predicate) {
            return Iterate.noneSatisfy(iterable, predicate);
        }
    }
}
