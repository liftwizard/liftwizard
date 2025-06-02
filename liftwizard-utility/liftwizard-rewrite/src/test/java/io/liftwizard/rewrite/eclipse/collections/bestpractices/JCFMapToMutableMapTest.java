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
class JCFMapToMutableMapTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFMapToMutableMap())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.map;

                        public interface MutableMap<K, V> extends java.util.Map<K, V> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.map.MutableMap;

                        public interface Maps {
                            MutableMapFactory mutable = null;
                           \s
                            interface MutableMapFactory {
                                <K, V> MutableMap<K, V> empty();
                                <K, V> MutableMap<K, V> with(K key, V value);
                                <K, V> MutableMap<K, V> of(K key, V value);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.map.mutable;

                        import org.eclipse.collections.api.map.MutableMap;

                        public class UnifiedMap<K, V> implements MutableMap<K, V> {
                            public static <K, V> UnifiedMap<K, V> newMap() {
                                return new UnifiedMap<>();
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.list;

                        import java.util.List;
                        import java.util.function.Consumer;

                        public interface MutableList<T> extends List<T> {
                            void forEach(Consumer<? super T> action);
                        }
                        """,
                        """
                        package java.util.function;

                        public interface Function<T, R> {
                            R apply(T t);
                        }
                        """,
                        """
                        package java.util.function;

                        public interface Consumer<T> {
                            void accept(T t);
                        }
                        """,
                        """
                        package com.example;

                        import java.util.Map;

                        public class Verify {
                            public static void assertMapsEqual(String message, Map<?, ?> expected, Map<?, ?> actual) {}
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceJavaUtilMapWithMutableMap() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        void test() {
                            Map<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class Test {
                        void test() {
                            MutableMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceJavaUtilMapWithMutableMapFullyQualified() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        void test() {
                            java.util.Map<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class Test {
                        void test() {
                            MutableMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceRawTypeMap() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        void test() {
                            Map map = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class Test {
                        void test() {
                            MutableMap map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenInitializedWithJavaUtilMap() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            Map<String, Integer> map = new HashMap<>();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenNoInitializer() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;

                    class Test {
                        void test() {
                            Map<String, Integer> map;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithNestedGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        void test() {
                            Map<String, List<Integer>> map = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Map;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class Test {
                        void test() {
                            MutableMap<String, List<Integer>> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithUnifiedMap() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            Map<String, Integer> map = UnifiedMap.newMap();
                        }
                    }
                    """,
                    """
                    import java.util.Map;

                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            MutableMap<String, Integer> map = UnifiedMap.newMap();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void multipleVariablesSameDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        void test() {
                            Map<String, Integer> map1 = Maps.mutable.empty(), map2 = Maps.mutable.with("a", 1);
                        }
                    }
                    """,
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class Test {
                        void test() {
                            MutableMap<String, Integer> map1 = Maps.mutable.empty(), map2 = Maps.mutable.with("a", 1);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void fieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        private Map<String, Integer> map = Maps.mutable.empty();
                    }
                    """,
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class Test {
                        private MutableMap<String, Integer> map = Maps.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceMethodReturn() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        Map<String, Integer> getMap() {
                            return Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceMethodParameter() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class Test {
                        void processMaps(Map<String, Integer> map) {
                            map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeMethodReference() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.list.MutableList;
                    import java.util.function.Function;

                    class A {
                        void method() {
                            MutableList<Map<String, Integer>> maps = null;
                            maps.forEach(Map::clear);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeJavaDocReferences() {
        this.rewriteRun(
                spec -> spec.typeValidationOptions(org.openrewrite.test.TypeValidation.none()),
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class A {
                        /**
                         * Tests that {@link Verify#assertMapsEqual(String, Map, Map)} really throw when they ought to.
                         */
                        void method() {
                            Map<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A {
                        /**
                         * Tests that {@link Verify#assertMapsEqual(String, Map, Map)} really throw when they ought to.
                         */
                        void method() {
                            MutableMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeGenericBounds() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;

                    class A<T extends Map<String, Integer>> {
                        void method() {
                            Map<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A<T extends Map<String, Integer>> {
                        void method() {
                            MutableMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }
}
