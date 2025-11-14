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

package io.liftwizard.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class AssertJMigrationYamlTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipeFromResources("io.liftwizard.rewrite.assertj.AssertJMigration")
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.test;

                        import java.util.Map;
                        import java.util.concurrent.Callable;
                        import org.eclipse.collections.api.block.predicate.Predicate;

                        public final class Verify {
                            public static <T> void assertCount(int expectedCount, Iterable<T> iterable, Predicate<? super T> predicate) {}
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                            public static void assertEmpty(Iterable<?> iterable) {}
                            public static void assertEmpty(String message, Map<?, ?> map) {}
                            public static void assertEmpty(Map<?, ?> map) {}
                            public static void assertNotEmpty(String message, Iterable<?> iterable) {}
                            public static void assertNotEmpty(Iterable<?> iterable) {}
                            public static void assertNotEmpty(String message, Map<?, ?> map) {}
                            public static void assertNotEmpty(Map<?, ?> map) {}
                            public static void assertSize(String message, int expectedSize, Iterable<?> iterable) {}
                            public static void assertSize(int expectedSize, Iterable<?> iterable) {}
                            public static void assertSize(String message, int expectedSize, Object[] array) {}
                            public static void assertSize(int expectedSize, Object[] array) {}
                            public static void assertSize(String mapName, int expectedSize, Map<?, ?> map) {}
                            public static void assertSize(int expectedSize, Map<?, ?> map) {}
                            public static void assertThrows(Class<? extends Throwable> expectedExceptionClass, Runnable code) {}
                            public static void assertThrows(Class<? extends Throwable> expectedExceptionClass, Callable<?> code) {}
                        }
                        """
                    )
                    .classpath("eclipse-collections-api", "eclipse-collections", "assertj-core")
            );
    }

    @Test
    void transformsVerifyAssertCount() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;

                    class Test {
                        void test() {
                            MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
                            Verify.assertCount(2, numbers, each -> each % 2 == 0);

                            MutableList<String> emptyList = Lists.mutable.empty();
                            Verify.assertCount(0, emptyList, s -> s.length() > 0);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;

                    import static org.assertj.core.api.Assertions.assertThat;

                    class Test {
                        void test() {
                            MutableList<Integer> numbers = Lists.mutable.with(1, 2, 3, 4, 5);
                            assertThat(numbers).filteredOn(each -> each % 2 == 0).hasSize(2);

                            MutableList<String> emptyList = Lists.mutable.empty();
                            assertThat(emptyList).filteredOn(s -> s.length() > 0).hasSize(0);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void transformsVerifyAssertEmpty() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.empty();
                            Verify.assertEmpty("list should be empty", list);
                            Verify.assertEmpty(list);

                            Map<String, Integer> map = new HashMap<>();
                            Verify.assertEmpty("map should be empty", map);
                            Verify.assertEmpty(map);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    import static org.assertj.core.api.Assertions.assertThat;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.empty();
                            assertThat(list).as("list should be empty").isEmpty();
                            assertThat(list).isEmpty();

                            Map<String, Integer> map = new HashMap<>();
                            assertThat(map).as("map should be empty").isEmpty();
                            assertThat(map).isEmpty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void transformsVerifyAssertNotEmpty() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            Verify.assertNotEmpty("list should not be empty", list);
                            Verify.assertNotEmpty(list);

                            Map<String, Integer> map = new HashMap<>();
                            map.put("key", 1);
                            Verify.assertNotEmpty("map should not be empty", map);
                            Verify.assertNotEmpty(map);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    import static org.assertj.core.api.Assertions.assertThat;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            assertThat(list).as("list should not be empty").isNotEmpty();
                            assertThat(list).isNotEmpty();

                            Map<String, Integer> map = new HashMap<>();
                            map.put("key", 1);
                            assertThat(map).as("map should not be empty").isNotEmpty();
                            assertThat(map).isNotEmpty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void transformsVerifyAssertSize() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                            Verify.assertSize("list should have size 3", 3, list);
                            Verify.assertSize(3, list);

                            String[] array = new String[]{"a", "b", "c"};
                            Verify.assertSize("array should have size 3", 3, array);
                            Verify.assertSize(3, array);

                            Map<String, Integer> map = new HashMap<>();
                            map.put("a", 1);
                            map.put("b", 2);
                            Verify.assertSize("map should have size 2", 2, map);
                            Verify.assertSize(2, map);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    import static org.assertj.core.api.Assertions.assertThat;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                            assertThat(list).as("list should have size 3").hasSize(3);
                            assertThat(list).hasSize(3);

                            String[] array = new String[]{"a", "b", "c"};
                            assertThat(array).as("array should have size 3").hasSize(3);
                            assertThat(array).hasSize(3);

                            Map<String, Integer> map = new HashMap<>();
                            map.put("a", 1);
                            map.put("b", 2);
                            assertThat(map).as("map should have size 2").hasSize(2);
                            assertThat(map).hasSize(2);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void transformsVerifyAssertThrows() {
        this.rewriteRun(
                spec ->
                    spec.typeValidationOptions(
                        TypeValidation.builder().identifiers(false).methodInvocations(false).build()
                    ),
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    import java.util.concurrent.Callable;

                    class Test {
                        void test() {
                            Verify.assertThrows(IllegalArgumentException.class, () -> {
                                throw new IllegalArgumentException("error");
                            });

                            Verify.assertThrows(NullPointerException.class, () -> {
                                throw new NullPointerException();
                            });

                            Callable<Object> failingCallable = () -> {
                                throw new RuntimeException("error");
                            };
                            Verify.assertThrows(RuntimeException.class, failingCallable);
                        }
                    }
                    """,
                    """
                    import java.util.concurrent.Callable;

                    import static org.assertj.core.api.Assertions.assertThatThrownBy;

                    class Test {
                        void test() {
                            assertThatThrownBy(() -> {
                                throw new IllegalArgumentException("error");
                            }).isInstanceOf(IllegalArgumentException.class);

                            assertThatThrownBy(() -> {
                                throw new NullPointerException();
                            }).isInstanceOf(NullPointerException.class);

                            Callable<Object> failingCallable = () -> {
                                throw new RuntimeException("error");
                            };
                            assertThatThrownBy(failingCallable::call).isInstanceOf(RuntimeException.class);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void appliesStaticImportOptimization() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                            Verify.assertSize(3, list);
                            Verify.assertNotEmpty(list);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;

                    import static org.assertj.core.api.Assertions.assertThat;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                            assertThat(list).hasSize(3);
                            assertThat(list).isNotEmpty();
                        }
                    }
                    """
                )
            );
    }
}
