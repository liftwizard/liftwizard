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

package io.liftwizard.junit.parameterized;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that JUnit components have compatible versions.
 *
 * <p>When junit-jupiter-params is upgraded to a different major version than
 * junit-jupiter-api, @ParameterizedTest with @MethodSource will fail at runtime
 * with NoClassDefFoundError for classes like TemplateInvocationValidationException.
 *
 * <p>This test catches such version mismatches by using @ParameterizedTest with @MethodSource.
 */
class JUnitVersionCompatibilityTest {

    private static Stream<Arguments> testDataProvider() {
        return Stream.of(Arguments.of("first", 1), Arguments.of("second", 2), Arguments.of("third", 3));
    }

    @ParameterizedTest
    @MethodSource("testDataProvider")
    void parameterizedMethodSourceCompatibility(String name, int value) {
        assertThat(name).isNotBlank();
        assertThat(value).isPositive();
    }
}
