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
class VerifyAssertItemAtIndexToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertItemAtIndexToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertItemAtIndex(String message, Object expectedItem, int index, java.util.List<?> list) {}
                            public static void assertContains(String message, Object object, Iterable<?> iterable) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class Assertions {
                            public static ListAssert assertThat(java.util.List<?> actual) { return new ListAssert(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class ListAssert {
                            public ListAssert as(String description) { return this; }
                            public ObjectAssert element(int index) { return new ObjectAssert(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class ObjectAssert {
                            public ObjectAssert isEqualTo(Object expected) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertItemAtIndexToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;

                    class Test {
                        void testMethod(List<String> list) {
                            Verify.assertItemAtIndex("First item should be 'hello'", "hello", 0, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;

                    class Test {
                        void testMethod(List<String> list) {
                            Assertions.assertThat(list).as("First item should be 'hello'").element(0).isEqualTo("hello");
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertItemAtIndexWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;

                    class Test {
                        void testMethod(List<String> list) {
                            String message = "Element at index should match";
                            String expected = "test";
                            int index = 2;
                            Verify.assertItemAtIndex(message, expected, index, list);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;

                    class Test {
                        void testMethod(List<String> list) {
                            String message = "Element at index should match";
                            String expected = "test";
                            int index = 2;
                            Assertions.assertThat(list).as(message).element(index).isEqualTo(expected);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertItemAtIndexWithComplexExpression() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;

                    class Test {
                        void testMethod(List<Integer> numbers) {
                            Verify.assertItemAtIndex("Last element should be sum", 10 + 5, numbers.size() - 1, numbers);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;

                    class Test {
                        void testMethod(List<Integer> numbers) {
                            Assertions.assertThat(numbers).as("Last element should be sum").element(numbers.size() - 1).isEqualTo(10 + 5);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertItemAtIndexWithDifferentTypes() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;

                    class Test {
                        void testMethod(List<Object> objects) {
                            Verify.assertItemAtIndex("Should be Integer", 42, 0, objects);
                            Verify.assertItemAtIndex("Should be String", "text", 1, objects);
                            Verify.assertItemAtIndex("Should be null", null, 2, objects);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;

                    class Test {
                        void testMethod(List<Object> objects) {
                            Assertions.assertThat(objects).as("Should be Integer").element(0).isEqualTo(42);
                            Assertions.assertThat(objects).as("Should be String").element(1).isEqualTo("text");
                            Assertions.assertThat(objects).as("Should be null").element(2).isEqualTo(null);
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertItemAtIndexWithMethodCalls() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void testMethod() {
                            List<String> list = createList();
                            Verify.assertItemAtIndex("Second item from method", getExpectedValue(), 1, list);
                        }

                        List<String> createList() {
                            return new ArrayList<>();
                        }

                        String getExpectedValue() {
                            return "expected";
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void testMethod() {
                            List<String> list = createList();
                            Assertions.assertThat(list).as("Second item from method").element(1).isEqualTo(getExpectedValue());
                        }

                        List<String> createList() {
                            return new ArrayList<>();
                        }

                        String getExpectedValue() {
                            return "expected";
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
                            Verify.assertContains("Should contain item", "item", list);
                            Verify.assertEmpty("Should be empty", list);
                        }
                    }"""
                )
            );
    }
}
