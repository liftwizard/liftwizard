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
    name = "Replace `Verify.assertNotEmpty()` with AssertJ",
    description = "Replace Eclipse Collections `Verify.assertNotEmpty()` with AssertJ `assertThat().isNotEmpty()`."
)
public class VerifyAssertNotEmptyToAssertJ {

    @RecipeDescriptor(
        name = "`Verify.assertNotEmpty(message, iterable)` → `assertThat(iterable).as(message).isNotEmpty()`",
        description = "Replace `Verify.assertNotEmpty(message, iterable)` with `assertThat(iterable).as(message).isNotEmpty()`."
    )
    public static class VerifyAssertNotEmptyIterableWithMessage {

        @BeforeTemplate
        void before(String message, Iterable<?> iterable) {
            Verify.assertNotEmpty(message, iterable);
        }

        @AfterTemplate
        void after(String message, Iterable<?> iterable) {
            assertThat(iterable).as(message).isNotEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertNotEmpty(iterable)` → `assertThat(iterable).isNotEmpty()`",
        description = "Replace `Verify.assertNotEmpty(iterable)` with `assertThat(iterable).isNotEmpty()`."
    )
    public static class VerifyAssertNotEmptyIterable {

        @BeforeTemplate
        void before(Iterable<?> iterable) {
            Verify.assertNotEmpty(iterable);
        }

        @AfterTemplate
        void after(Iterable<?> iterable) {
            assertThat(iterable).isNotEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertNotEmpty(message, map)` → `assertThat(map).as(message).isNotEmpty()`",
        description = "Replace `Verify.assertNotEmpty(message, map)` with `assertThat(map).as(message).isNotEmpty()`."
    )
    public static class VerifyAssertNotEmptyMapWithMessage {

        @BeforeTemplate
        void before(String message, Map<?, ?> map) {
            Verify.assertNotEmpty(message, map);
        }

        @AfterTemplate
        void after(String message, Map<?, ?> map) {
            assertThat(map).as(message).isNotEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertNotEmpty(map)` → `assertThat(map).isNotEmpty()`",
        description = "Replace `Verify.assertNotEmpty(map)` with `assertThat(map).isNotEmpty()`."
    )
    public static class VerifyAssertNotEmptyMap {

        @BeforeTemplate
        void before(Map<?, ?> map) {
            Verify.assertNotEmpty(map);
        }

        @AfterTemplate
        void after(Map<?, ?> map) {
            assertThat(map).isNotEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertNotEmpty(message, array)` → `assertThat(array).as(message).isNotEmpty()`",
        description = "Replace `Verify.assertNotEmpty(message, array)` with `assertThat(array).as(message).isNotEmpty()`."
    )
    public static class VerifyAssertNotEmptyArrayWithMessage<T> {

        @BeforeTemplate
        void before(String message, T[] array) {
            Verify.assertNotEmpty(message, array);
        }

        @AfterTemplate
        void after(String message, T[] array) {
            assertThat(array).as(message).isNotEmpty();
        }
    }

    @RecipeDescriptor(
        name = "`Verify.assertNotEmpty(array)` → `assertThat(array).isNotEmpty()`",
        description = "Replace `Verify.assertNotEmpty(array)` with `assertThat(array).isNotEmpty()`."
    )
    public static class VerifyAssertNotEmptyArray<T> {

        @BeforeTemplate
        void before(T[] array) {
            Verify.assertNotEmpty(array);
        }

        @AfterTemplate
        void after(T[] array) {
            assertThat(array).isNotEmpty();
        }
    }
}
