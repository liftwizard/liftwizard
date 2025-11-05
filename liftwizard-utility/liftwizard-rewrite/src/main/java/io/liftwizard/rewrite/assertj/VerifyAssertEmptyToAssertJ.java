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

package io.liftwizard.rewrite.assertj;

import java.util.Map;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.impl.test.Verify;
import org.openrewrite.java.template.RecipeDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

@RecipeDescriptor(
    name = "Replace `Verify.assertEmpty()` with AssertJ",
    description = "Replace Eclipse Collections `Verify.assertEmpty()` with AssertJ `assertThat().isEmpty()`."
)
public class VerifyAssertEmptyToAssertJ {

    @RecipeDescriptor(
        name = "`Verify.assertEmpty(message, iterable)` → `assertThat(iterable).as(message).isEmpty()`",
        description = "Replace `Verify.assertEmpty(message, iterable)` with `assertThat(iterable).as(message).isEmpty()`."
    )
    public static class VerifyAssertEmptyIterableWithMessage {

        @BeforeTemplate
        void before(String message, Iterable<?> iterable) {
            Verify.assertEmpty(message, iterable);
        }

        @AfterTemplate
        void after(String message, Iterable<?> iterable) {
            assertThat(iterable).as(message).isEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertEmpty(iterable)` → `assertThat(iterable).isEmpty()`",
        description = "Replace `Verify.assertEmpty(iterable)` with `assertThat(iterable).isEmpty()`."
    )
    public static class VerifyAssertEmptyIterable {

        @BeforeTemplate
        void before(Iterable<?> iterable) {
            Verify.assertEmpty(iterable);
        }

        @AfterTemplate
        void after(Iterable<?> iterable) {
            assertThat(iterable).isEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertEmpty(message, map)` → `assertThat(map).as(message).isEmpty()`",
        description = "Replace `Verify.assertEmpty(message, map)` with `assertThat(map).as(message).isEmpty()`."
    )
    public static class VerifyAssertEmptyMapWithMessage {

        @BeforeTemplate
        void before(String message, Map<?, ?> map) {
            Verify.assertEmpty(message, map);
        }

        @AfterTemplate
        void after(String message, Map<?, ?> map) {
            assertThat(map).as(message).isEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertEmpty(map)` → `assertThat(map).isEmpty()`",
        description = "Replace `Verify.assertEmpty(map)` with `assertThat(map).isEmpty()`."
    )
    public static class VerifyAssertEmptyMap {

        @BeforeTemplate
        void before(Map<?, ?> map) {
            Verify.assertEmpty(map);
        }

        @AfterTemplate
        void after(Map<?, ?> map) {
            assertThat(map).isEmpty();
        }
    }
}
