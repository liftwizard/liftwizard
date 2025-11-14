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

package io.liftwizard.rewrite.eclipse.collections.adoption.unsafe;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "`detect() != null` → `anySatisfy()`",
    description = "Converts `iterable.detect(predicate) != null` to `iterable.anySatisfy(predicate)` and " +
    "`iterable.detect(predicate) == null` to `iterable.noneSatisfy(predicate)` for Eclipse Collections types. " +
    "Warning: This transformation can change semantics if the collection contains null values."
)
public class ECDetectToSatisfies {

    @RecipeDescriptor(
        name = "`detect(predicate) != null` → `anySatisfy(predicate)`",
        description = "Converts `iterable.detect(predicate) != null` and `null != iterable.detect(predicate)` to `iterable.anySatisfy(predicate)`."
    )
    public static final class DetectNotNullToAnySatisfy<T> {

        @BeforeTemplate
        boolean beforeDetectNotNull(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return iterable.detect(predicate) != null;
        }

        @BeforeTemplate
        boolean beforeNullNotEqualsDetect(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return null != iterable.detect(predicate);
        }

        @AfterTemplate
        boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return iterable.anySatisfy(predicate);
        }
    }

    @RecipeDescriptor(
        name = "`detect(predicate) == null` → `noneSatisfy(predicate)`",
        description = "Converts `iterable.detect(predicate) == null` and `null == iterable.detect(predicate)` to `iterable.noneSatisfy(predicate)`."
    )
    public static final class DetectEqualsNullToNoneSatisfy<T> {

        @BeforeTemplate
        boolean beforeDetectEqualsNull(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return iterable.detect(predicate) == null;
        }

        @BeforeTemplate
        boolean beforeNullEqualsDetect(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return null == iterable.detect(predicate);
        }

        @AfterTemplate
        boolean after(RichIterable<T> iterable, Predicate<? super T> predicate) {
            return iterable.noneSatisfy(predicate);
        }
    }
}
