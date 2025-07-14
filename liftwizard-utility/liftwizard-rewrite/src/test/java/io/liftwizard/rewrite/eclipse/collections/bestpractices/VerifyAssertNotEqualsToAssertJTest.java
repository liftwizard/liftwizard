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
class VerifyAssertNotEqualsToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertNotEqualsToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertNotEquals(String message, Object left, Object right) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                            public static void assertContains(String message, Object object, Iterable<?> iterable) {}
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class Assertions {
                            public static ObjectAssert assertThat(Object actual) { return new ObjectAssert(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class ObjectAssert {
                            public ObjectAssert as(String description) { return this; }
                            public ObjectAssert isNotEqualTo(Object expected) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertNotEqualsToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod() {
                            Verify.assertNotEquals("Values should not be equal", "hello", "world");
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod() {
                            Assertions.assertThat("hello").as("Values should not be equal").isNotEqualTo("world");
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotEqualsWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod() {
                            String message = "Objects should be different";
                            String first = "first";
                            String second = "second";
                            Verify.assertNotEquals(message, first, second);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod() {
                            String message = "Objects should be different";
                            String first = "first";
                            String second = "second";
                            Assertions.assertThat(first).as(message).isNotEqualTo(second);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotEqualsWithNumbers() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod() {
                            Verify.assertNotEquals("Numbers should be different", 42, 100);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod() {
                            Assertions.assertThat(42).as("Numbers should be different").isNotEqualTo(100);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotEqualsWithComplexObjects() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.ArrayList;
                    import java.util.List;

                    class Test {
                        void testMethod() {
                            List<String> list1 = new ArrayList<>();
                            List<String> list2 = new ArrayList<>();
                            Verify.assertNotEquals("Lists should be different instances", list1, list2);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.ArrayList;
                    import java.util.List;

                    class Test {
                        void testMethod() {
                            List<String> list1 = new ArrayList<>();
                            List<String> list2 = new ArrayList<>();
                            Assertions.assertThat(list1).as("Lists should be different instances").isNotEqualTo(list2);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotEqualsWithMethodCalls() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod() {
                            Verify.assertNotEquals("Results should differ", getFirst(), getSecond());
                        }

                        String getFirst() {
                            return "first";
                        }

                        String getSecond() {
                            return "second";
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod() {
                            Assertions.assertThat(getFirst()).as("Results should differ").isNotEqualTo(getSecond());
                        }

                        String getFirst() {
                            return "first";
                        }

                        String getSecond() {
                            return "second";
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertNotEqualsWithNull() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod() {
                            String value = "not null";
                            Verify.assertNotEquals("Value should not be null", value, null);
                            Verify.assertNotEquals("Null should not equal value", null, value);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod() {
                            String value = "not null";
                            Assertions.assertThat(value).as("Value should not be null").isNotEqualTo(null);
                            Assertions.assertThat(null).as("Null should not equal value").isNotEqualTo(value);
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
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;

                    class Test {
                        void testMethod(List<String> list) {
                            Verify.assertEmpty("Should be empty", list);
                            Verify.assertContains("Should contain item", "item", list);
                        }
                    }"""
                )
            );
    }
}
