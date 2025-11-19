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

package io.liftwizard.rewrite.bestpractices;

import java.util.Objects;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.RecipeDescriptor;

@RecipeDescriptor(
    name = "Null-safe equality checks → `Objects.equals()`",
    description = "Replace complex null-safe equality checks with `Objects.equals()`."
)
public class NullSafeEquals {

    @RecipeDescriptor(
        name = "Null-safe not-equals patterns → `!Objects.equals(left, right)`",
        description = "Replace null-safe not-equals patterns with `!Objects.equals(left, right)`."
    )
    public static class NotEqualsPatterns<T> {

        @BeforeTemplate
        boolean pattern1(T left, T right) {
            return left == null ? right != null : !left.equals(right);
        }

        @BeforeTemplate
        boolean pattern2(T left, T right) {
            return right == null ? left != null : !right.equals(left);
        }

        @AfterTemplate
        boolean after(T left, T right) {
            return !Objects.equals(left, right);
        }
    }

    @RecipeDescriptor(
        name = "Null-safe equality patterns → `Objects.equals(left, right)`",
        description = "Replace null-safe equality patterns with `Objects.equals(left, right)`."
    )
    public static class EqualsPatterns<T> {

        @BeforeTemplate
        boolean pattern2(T left, T right) {
            return left == null ? right == null : left.equals(right);
        }

        @BeforeTemplate
        boolean pattern3(T left, T right) {
            return left == null ? right == null : left == right || left.equals(right);
        }

        @BeforeTemplate
        boolean pattern4(T left, T right) {
            return left == right || (left != null && left.equals(right));
        }

        @BeforeTemplate
        boolean pattern5(T left, T right) {
            return right == left || (left != null && left.equals(right));
        }

        @BeforeTemplate
        boolean pattern6(T left, T right) {
            return left == null || right == null ? left == right : left.equals(right);
        }

        @AfterTemplate
        boolean after(T left, T right) {
            return Objects.equals(left, right);
        }
    }
}
