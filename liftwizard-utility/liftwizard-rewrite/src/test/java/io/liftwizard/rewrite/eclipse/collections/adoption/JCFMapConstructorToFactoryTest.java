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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class JCFMapConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFMapConstructorToFactory())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.map;

                        public interface MutableMap<K, V> extends java.util.Map<K, V> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.map.sorted;

                        public interface MutableSortedMap<K, V> extends java.util.SortedMap<K, V> {
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
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                        public interface SortedMaps {
                            MutableSortedMapFactory mutable = null;
                           \s
                            interface MutableSortedMapFactory {
                                <K, V> MutableSortedMap<K, V> empty();
                                <K, V> MutableSortedMap<K, V> with(K key, V value);
                                <K, V> MutableSortedMap<K, V> of(K key, V value);
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceHashMapConstructorVariations() {
        // With diamond operator
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            HashMap<String, Integer> map = new HashMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.HashMap;

                    class Test {
                        void test() {
                            HashMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );

        // Without generics (raw type)
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            HashMap map = new HashMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.HashMap;

                    class Test {
                        void test() {
                            HashMap map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceTreeMapConstructorVariations() {
        // With diamond operator
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeMap;

                    class Test {
                        void test() {
                            TreeMap<String, Integer> map = new TreeMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;

                    import java.util.TreeMap;

                    class Test {
                        void test() {
                            TreeMap<String, Integer> map = SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );

        // Without generics (raw type)
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeMap;

                    class Test {
                        void test() {
                            TreeMap map = new TreeMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;

                    import java.util.TreeMap;

                    class Test {
                        void test() {
                            TreeMap map = SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceConstructorsWithArguments() {
        // HashMap with initial capacity
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            HashMap<String, Integer> map = new HashMap<>(10);
                        }
                    }
                    """
                )
            );

        // TreeMap with comparator
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeMap;
                    import java.util.Comparator;

                    class Test {
                        void test() {
                            TreeMap<String, Integer> map = new TreeMap<>(Comparator.naturalOrder());
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleJCFMapConstructors() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;
                    import java.util.TreeMap;

                    class Test {
                        void test() {
                            HashMap<String, Integer> map1 = new HashMap<>();
                            TreeMap<String, Integer> map2 = new TreeMap<>();
                            HashMap map3 = new HashMap();
                            TreeMap map4 = new TreeMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.SortedMaps;

                    import java.util.HashMap;
                    import java.util.TreeMap;

                    class Test {
                        void test() {
                            HashMap<String, Integer> map1 = Maps.mutable.empty();
                            TreeMap<String, Integer> map2 = SortedMaps.mutable.empty();
                            HashMap map3 = Maps.mutable.empty();
                            TreeMap map4 = SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacementInFieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;

                    class Test {
                        private HashMap<String, String> map = new HashMap<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.HashMap;

                    class Test {
                        private HashMap<String, String> map = Maps.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void replacementInTreeMapFieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeMap;

                    class Test {
                        private TreeMap<String, String> map = new TreeMap<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;

                    import java.util.TreeMap;

                    class Test {
                        private TreeMap<String, String> map = SortedMaps.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void hashMapDiamondOperatorShouldNotAddExplicitGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;
                    import java.util.Map;

                    class A {
                        void method() {
                            Map<String, Integer> map = new HashMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.Map;

                    class A {
                        void method() {
                            Map<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void treeMapDiamondOperatorShouldNotAddExplicitGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeMap;
                    import java.util.Map;

                    class A {
                        void method() {
                            Map<String, Integer> map = new TreeMap<>();
                        }
                    }
                    """,
                    """
                    import java.util.Map;

                    import org.eclipse.collections.api.factory.SortedMaps;

                    class A {
                        void method() {
                            Map<String, Integer> map = SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void explicitGenericsAreNotTransformed() {
        // Unlike diamond operators, explicit generics are not transformed to avoid complexity
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;
                    import java.util.Map;

                    class A {
                        void method() {
                            Map<String, Integer> map = new HashMap<String, Integer>();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldAddImportAndNotUseFullyQualifiedName() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;

                    class A {
                        void method() {
                            java.util.Map<String, Integer> map = new HashMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    class A {
                        void method() {
                            java.util.Map<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void nestedGenericsWithDiamondOperator() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;
                    import java.util.Map;
                    import java.util.List;

                    class A {
                        void method() {
                            Map<String, List<Integer>> map = new HashMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.Map;
                    import java.util.List;

                    class A {
                        void method() {
                            Map<String, List<Integer>> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void wildcardGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;
                    import java.util.Map;

                    class A {
                        void method() {
                            Map<String, ? extends Number> map = new HashMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.Map;

                    class A {
                        void method() {
                            Map<String, ? extends Number> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }
}
