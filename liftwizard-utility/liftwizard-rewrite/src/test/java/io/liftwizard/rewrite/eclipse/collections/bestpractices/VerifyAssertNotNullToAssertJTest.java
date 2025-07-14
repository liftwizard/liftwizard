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
class VerifyAssertNotNullToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertNotNullToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertNotNull(String message, Object object) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                            public static void assertContains(String message, Object object, Iterable<?> iterable) {}
                        }
                        """,
                        """
                        package org.eclipse.collections.api.list;
                        public interface MutableList<T> extends java.lang.Iterable<T> {
                            boolean contains(Object o);
                            T get(int index);
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class Assertions {
                            public static ObjectAssert assertThat(Object actual) { return new ObjectAssert(); }
                            public static <T> IterableAssert<T> assertThat(Iterable<T> actual) { return new IterableAssert<>(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class ObjectAssert {
                            public ObjectAssert as(String description) { return this; }
                            public ObjectAssert isNotNull() { return this; }
                            public ObjectAssert isEqualTo(Object expected) { return this; }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class IterableAssert<T> {
                            public IterableAssert<T> as(String description) { return this; }
                            public IterableAssert<T> contains(Object... values) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertNotNullToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertNotNull("List should not be null", list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("List should not be null").isNotNull();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotNullWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "Expected non-null value";
                            Verify.assertNotNull(message, list.get(0));
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "Expected non-null value";
                            Assertions.assertThat(list.get(0)).as(message).isNotNull();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotNullWithString() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(String value) {
                            Verify.assertNotNull("String should not be null", value);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod(String value) {
                            Assertions.assertThat(value).as("String should not be null").isNotNull();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotNullWithCustomObject() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        static class Person {
                            String name;
                        }

                        void testMethod(Person person) {
                            Verify.assertNotNull("Person object is required", person);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        static class Person {
                            String name;
                        }

                        void testMethod(Person person) {
                            Assertions.assertThat(person).as("Person object is required").isNotNull();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotNullWithMethodCall() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        String getValue() { return "test"; }

                        void testMethod() {
                            Verify.assertNotNull("Method should return non-null", getValue());
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        String getValue() { return "test"; }

                        void testMethod() {
                            Assertions.assertThat(getValue()).as("Method should return non-null").isNotNull();
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotNullWithFieldAccess() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        private String field = "value";

                        void testMethod() {
                            Verify.assertNotNull("Field should be initialized", this.field);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        private String field = "value";

                        void testMethod() {
                            Assertions.assertThat(this.field).as("Field should be initialized").isNotNull();
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
                            Verify.assertContains("Should contain item", "item", list);
                        }
                    }"""
                )
            );
    }
}
