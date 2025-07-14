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
class VerifyAssertInstanceOfToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertInstanceOfToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertInstanceOf(String message, Object actualObject, Class<?> expectedClass) {}
                            public static void assertContains(String message, Object object, Iterable<?> iterable) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
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
                            public ObjectAssert isInstanceOf(Class<?> expectedClass) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertInstanceOfToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(Object obj) {
                            Verify.assertInstanceOf("Should be String", obj, String.class);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod(Object obj) {
                            Assertions.assertThat(obj).as("Should be String").isInstanceOf(String.class);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertInstanceOfWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(Object obj) {
                            String message = "Object should be of correct type";
                            Verify.assertInstanceOf(message, obj, Integer.class);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;

                    class Test {
                        void testMethod(Object obj) {
                            String message = "Object should be of correct type";
                            Assertions.assertThat(obj).as(message).isInstanceOf(Integer.class);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertInstanceOfWithComplexExpression() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;

                    class Test {
                        void testMethod(List<Object> list) {
                            Verify.assertInstanceOf("First element should be String", list.get(0), String.class);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;

                    class Test {
                        void testMethod(List<Object> list) {
                            Assertions.assertThat(list.get(0)).as("First element should be String").isInstanceOf(String.class);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertInstanceOfWithDifferentTypes() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.Map;
                    import java.util.List;

                    class Test {
                        void testMethod(Object obj) {
                            Verify.assertInstanceOf("Should be Map", obj, Map.class);
                            Verify.assertInstanceOf("Should be List", obj, List.class);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.Map;
                    import java.util.List;

                    class Test {
                        void testMethod(Object obj) {
                            Assertions.assertThat(obj).as("Should be Map").isInstanceOf(Map.class);
                            Assertions.assertThat(obj).as("Should be List").isInstanceOf(List.class);
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
                        void testMethod(List<String> list, Object obj) {
                            Verify.assertContains("Should contain item", "item", list);
                            Verify.assertEmpty("Should be empty", list);
                        }
                    }"""
                )
            );
    }
}
