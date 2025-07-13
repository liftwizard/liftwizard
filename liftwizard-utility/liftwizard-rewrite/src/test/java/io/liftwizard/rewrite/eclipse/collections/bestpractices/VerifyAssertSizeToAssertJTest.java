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
class VerifyAssertSizeToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertSizeToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertSize(String message, int expectedSize, Iterable<?> iterable) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                        }
                        """,
                        """
                        package org.eclipse.collections.api.list;
                        public interface MutableList<T> extends java.lang.Iterable<T> {
                            int size();
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
                            public IterableAssert<T> hasSize(int expected) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertSizeToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertSize("Expected list to have 3 elements", 3, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("Expected list to have 3 elements").hasSize(3);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertSizeWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should have expected size";
                            int expectedSize = 5;
                            Verify.assertSize(message, expectedSize, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should have expected size";
                            int expectedSize = 5;
                            Assertions.assertThat(list).as(message).hasSize(expectedSize);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertSizeWithComplexCollection() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertSize("Filtered list should have 2 elements", 2, list.select(s -> s.length() > 5));
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list.select(s -> s.length() > 5)).as("Filtered list should have 2 elements").hasSize(2);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertSizeWithZero() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertSize("List should be empty", 0, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("List should be empty").hasSize(0);
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
                            Verify.assertEmpty("Should be empty", list);
                        }
                    }"""
                )
            );
    }
}
