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
class VerifyAssertContainsAllToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertContainsAllToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertContainsAll(String message, Iterable<?> iterable, Object... items) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                            public static void assertContains(String message, Object object, Iterable<?> iterable) {}
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;
                        public class Lists {
                            public static MutableListFactory mutable = new MutableListFactory();
                            public static class MutableListFactory {
                                public <T> org.eclipse.collections.api.list.MutableList<T> with(T... items) { return null; }
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.list;
                        import java.util.Collection;
                        public interface MutableList<T> extends Collection<T> {
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
                        import java.util.Collection;
                        public class IterableAssert<T> {
                            public IterableAssert<T> as(String description) { return this; }
                            public IterableAssert<T> contains(Object... values) { return this; }
                            public IterableAssert<T> containsAll(Collection<?> collection) { return this; }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertContainsAllToAssertJ() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertContainsAll("List should contain all items", list, "item1", "item2", "item3");
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("List should contain all items").containsAll(Lists.mutable.with("item1", "item2", "item3"));
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertContainsAllWithSingleItem() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertContainsAll("List should contain item", list, "item1");
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list).as("List should contain item").containsAll(Lists.mutable.with("item1"));
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertContainsAllWithVariableMessage() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should contain all expected elements";
                            String first = "first";
                            String second = "second";
                            Verify.assertContainsAll(message, list, first, second);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            String message = "List should contain all expected elements";
                            String first = "first";
                            String second = "second";
                            Assertions.assertThat(list).as(message).containsAll(Lists.mutable.with(first, second));
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertContainsAllWithNumbers() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<Integer> numbers) {
                            Verify.assertContainsAll("Should contain all numbers", numbers, 1, 2, 3, 4, 5);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<Integer> numbers) {
                            Assertions.assertThat(numbers).as("Should contain all numbers").containsAll(Lists.mutable.with(1, 2, 3, 4, 5));
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertContainsAllWithComplexCollection() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Verify.assertContainsAll("Filtered list should contain all long strings",
                                list.select(s -> s.length() > 5),
                                "longString1",
                                "longString2");
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<String> list) {
                            Assertions.assertThat(list.select(s -> s.length() > 5)).as("Filtered list should contain all long strings").containsAll(Lists.mutable.with("longString1", "longString2"));
                        }
                    }"""
                )
            );
    }

    @Test
    void transformsVerifyAssertContainsAllWithMixedTypes() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.test.Verify;

                    class Test {
                        void testMethod(MutableList<Object> list) {
                            Verify.assertContainsAll("Should contain mixed types", list, "string", 42, true);
                        }
                    }""",
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMethod(MutableList<Object> list) {
                            Assertions.assertThat(list).as("Should contain mixed types").containsAll(Lists.mutable.with("string", 42, true));
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
