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
	name = "Null-safe hashCode → `Objects.hashCode()`",
	description = "Replace null-safe hashCode patterns with `Objects.hashCode()`."
)
public class NullSafeHashCode {

	@RecipeDescriptor(
		name = "`object == null ? 0 : object.hashCode()` → `Objects.hashCode(object)`",
		description = "Replace ternary null check with hashCode with `Objects.hashCode(object)`."
	)
	public static class TernaryHashCode<T> {

		@BeforeTemplate
		int before(T object) {
			return object == null ? 0 : object.hashCode();
		}

		@BeforeTemplate
		int beforeInverted(T object) {
			return object != null ? object.hashCode() : 0;
		}

		@AfterTemplate
		int after(T object) {
			return Objects.hashCode(object);
		}
	}
}
