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
	name = "Replace `Verify.assertSize()` with AssertJ",
	description = "Replace Eclipse Collections `Verify.assertSize()` with AssertJ `assertThat().hasSize()`."
)
public class VerifyAssertSizeToAssertJ {

	@RecipeDescriptor(
		name = "`Verify.assertSize(message, expectedSize, iterable)` → `assertThat(iterable).as(message).hasSize(expectedSize)`",
		description = "Replace `Verify.assertSize(message, expectedSize, iterable)` with `assertThat(iterable).as(message).hasSize(expectedSize)`."
	)
	public static class VerifyAssertSizeIterableWithMessage {

		@BeforeTemplate
		void before(String message, int expectedSize, Iterable<?> iterable) {
			Verify.assertSize(message, expectedSize, iterable);
		}

		@AfterTemplate
		void after(String message, int expectedSize, Iterable<?> iterable) {
			assertThat(iterable).as(message).hasSize(expectedSize);
		}
	}

	@RecipeDescriptor(
		name = "`Verify.assertSize(expectedSize, iterable)` → `assertThat(iterable).hasSize(expectedSize)`",
		description = "Replace `Verify.assertSize(expectedSize, iterable)` with `assertThat(iterable).hasSize(expectedSize)`."
	)
	public static class VerifyAssertSizeIterable {

		@BeforeTemplate
		void before(int expectedSize, Iterable<?> iterable) {
			Verify.assertSize(expectedSize, iterable);
		}

		@AfterTemplate
		void after(int expectedSize, Iterable<?> iterable) {
			assertThat(iterable).hasSize(expectedSize);
		}
	}

	@RecipeDescriptor(
		name = "`Verify.assertSize(arrayName, expectedSize, array)` → `assertThat(array).as(arrayName).hasSize(expectedSize)`",
		description = "Replace `Verify.assertSize(arrayName, expectedSize, array)` with `assertThat(array).as(arrayName).hasSize(expectedSize)`."
	)
	public static class VerifyAssertSizeArrayWithMessage {

		@BeforeTemplate
		void before(String arrayName, int expectedSize, Object[] array) {
			Verify.assertSize(arrayName, expectedSize, array);
		}

		@AfterTemplate
		void after(String arrayName, int expectedSize, Object[] array) {
			assertThat(array).as(arrayName).hasSize(expectedSize);
		}
	}

	@RecipeDescriptor(
		name = "`Verify.assertSize(expectedSize, array)` → `assertThat(array).hasSize(expectedSize)`",
		description = "Replace `Verify.assertSize(expectedSize, array)` with `assertThat(array).hasSize(expectedSize)`."
	)
	public static class VerifyAssertSizeArray {

		@BeforeTemplate
		void before(int expectedSize, Object[] array) {
			Verify.assertSize(expectedSize, array);
		}

		@AfterTemplate
		void after(int expectedSize, Object[] array) {
			assertThat(array).hasSize(expectedSize);
		}
	}

	@RecipeDescriptor(
		name = "`Verify.assertSize(mapName, expectedSize, map)` → `assertThat(map).as(mapName).hasSize(expectedSize)`",
		description = "Replace `Verify.assertSize(mapName, expectedSize, map)` with `assertThat(map).as(mapName).hasSize(expectedSize)`."
	)
	public static class VerifyAssertSizeMapWithMessage {

		@BeforeTemplate
		void before(String mapName, int expectedSize, Map<?, ?> map) {
			Verify.assertSize(mapName, expectedSize, map);
		}

		@AfterTemplate
		void after(String mapName, int expectedSize, Map<?, ?> map) {
			assertThat(map).as(mapName).hasSize(expectedSize);
		}
	}

	@RecipeDescriptor(
		name = "`Verify.assertSize(expectedSize, map)` → `assertThat(map).hasSize(expectedSize)`",
		description = "Replace `Verify.assertSize(expectedSize, map)` with `assertThat(map).hasSize(expectedSize)`."
	)
	public static class VerifyAssertSizeMap {

		@BeforeTemplate
		void before(int expectedSize, Map<?, ?> map) {
			Verify.assertSize(expectedSize, map);
		}

		@AfterTemplate
		void after(int expectedSize, Map<?, ?> map) {
			assertThat(map).hasSize(expectedSize);
		}
	}
}
