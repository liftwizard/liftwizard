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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.impl.test.Verify;
import org.openrewrite.java.template.RecipeDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

@RecipeDescriptor(
    name = "`Verify.assertCount(expectedCount, iterable, predicate)` → `assertThat(iterable).filteredOn(predicate).hasSize(expectedCount)`",
    description = "Replace `Verify.assertCount(expectedCount, iterable, predicate)` with `assertThat(iterable).filteredOn(predicate).hasSize(expectedCount)`."
)
public class VerifyAssertCountToAssertJ<T> {

    @BeforeTemplate
    void before(int expectedCount, Iterable<T> iterable, Predicate<? super T> predicate) {
        Verify.assertCount(expectedCount, iterable, predicate);
    }

    @AfterTemplate
    void after(int expectedCount, Iterable<T> iterable, Predicate<? super T> predicate) {
        assertThat(iterable).filteredOn(predicate).hasSize(expectedCount);
    }
}
