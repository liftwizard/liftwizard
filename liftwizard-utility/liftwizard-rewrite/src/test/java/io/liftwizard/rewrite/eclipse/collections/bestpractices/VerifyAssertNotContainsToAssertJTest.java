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

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class VerifyAssertNotContainsToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertNotContainsToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertContains(String message, Object object, Iterable<?> iterable) {}
                            public static void assertNotContains(String message, Object object, Iterable<?> iterable) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                        }
                        """,
                        """
                        package org.eclipse.collections.api.list;
                        public interface MutableList<T> extends java.lang.Iterable<T> {
                            boolean contains(Object o);
                            MutableList<T> select(java.util.function.Predicate<T> predicate);
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class Assertions {
                            public static <T> IterableAssert<T> assertThat(Iterable<T> actual) { return new IterableAssert<>(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class IterableAssert<T> {
                            public IterableAssert<T> as(String description) { return this; }
                            public IterableAssert<T> contains(Object... values) { return this; }
                            public IterableAssert<T> doesNotContain(Object... values) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertNotContainsToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertNotContains("Expected item not in list", "item", list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("Expected item not in list").doesNotContain("item");
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotContainsWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should not contain element";
                            String element = "test";
                            Verify.assertNotContains(message, element, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should not contain element";
                            String element = "test";
                            Assertions.assertThat(list).as(message).doesNotContain(element);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotContainsWithComplexCollection() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertNotContains("Filtered list should not contain item", "shortString", list.select(s -> s.length() > 5));
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list.select(s -> s.length() > 5)).as("Filtered list should not contain item").doesNotContain("shortString");
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotContainsWithNumbers() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<Integer> numbers) {
                            Verify.assertNotContains("Should not contain 42", 42, numbers);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<Integer> numbers) {
                            Assertions.assertThat(numbers).as("Should not contain 42").doesNotContain(42);
                        }
                    }"""
                )
            );
    }

    @Test
    void doesNotTransformOtherVerifyMethods() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertContains("Should contain item", "item", list);
                            Verify.assertEmpty("Should be empty", list);
                        }
                    }"""
                )
            );
    }
}
