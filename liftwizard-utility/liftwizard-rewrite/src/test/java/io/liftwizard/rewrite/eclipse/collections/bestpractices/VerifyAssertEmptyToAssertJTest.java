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
class VerifyAssertEmptyToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertEmptyToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                            public static void assertNotEmpty(String message, Iterable<?> iterable) {}
                            public static void assertEquals(String message, Object expected, Object actual) {}
                        }
                        """,
                        """
                        package org.eclipse.collections.api.list;
                        public interface MutableList<T> extends java.lang.Iterable<T> {
                            boolean isEmpty();
                            int size();
                            MutableList<T> select(java.util.function.Predicate<T> predicate);
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class Assertions {
                            public static <T> ObjectAssert<T> assertThat(T actual) { return new ObjectAssert<>(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class ObjectAssert<T> {
                            public ObjectAssert<T> as(String description) { return this; }
                            public ObjectAssert<T> isEmpty() { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertEmptyToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertEmpty("Expected empty list", list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("Expected empty list").isEmpty();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertEmptyWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should be empty";
                            Verify.assertEmpty(message, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should be empty";
                            Assertions.assertThat(list).as(message).isEmpty();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertEmptyWithComplexCollection() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertEmpty("Filtered list should be empty", list.select(s -> s.length() > 10));
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list.select(s -> s.length() > 10)).as("Filtered list should be empty").isEmpty();
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
                            Verify.assertNotEmpty("Should not be empty", list);
                            Verify.assertEquals("Size check", 5, list.size());
                        }
                    }"""
                )
            );
    }
}
