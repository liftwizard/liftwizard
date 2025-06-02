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
class JCFCollectionsToFactoriesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFCollectionsToFactories())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.list;

                        public interface FixedSizeList<T> extends java.util.List<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.set;

                        public interface FixedSizeSet<T> extends java.util.Set<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.map;

                        public interface FixedSizeMap<K, V> extends java.util.Map<K, V> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.list.FixedSizeList;

                        public interface Lists {
                            FixedSizeListFactory fixedSize = null;
                           \s
                            interface FixedSizeListFactory {
                                <T> FixedSizeList<T> empty();
                                <T> FixedSizeList<T> of(T element);
                                <T> FixedSizeList<T> with(T... elements);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.set.FixedSizeSet;

                        public interface Sets {
                            FixedSizeSetFactory fixedSize = null;
                           \s
                            interface FixedSizeSetFactory {
                                <T> FixedSizeSet<T> empty();
                                <T> FixedSizeSet<T> of(T element);
                                <T> FixedSizeSet<T> with(T... elements);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.map.FixedSizeMap;

                        public interface Maps {
                            FixedSizeMapFactory fixedSize = null;
                           \s
                            interface FixedSizeMapFactory {
                                <K, V> FixedSizeMap<K, V> empty();
                                <K, V> FixedSizeMap<K, V> of(K key, V value);
                                <K, V> FixedSizeMap<K, V> with(K key, V value);
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceCollectionsEmptyListWithListsFactory() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> list = Collections.emptyList();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> list = Lists.fixedSize.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceCollectionsEmptySetWithSetsFactory() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.Set;

                    class Test {
                        void test() {
                            Set<String> set = Collections.emptySet();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.Set;

                    class Test {
                        void test() {
                            Set<String> set = Sets.fixedSize.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceCollectionsEmptyMapWithMapsFactory() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.Map;

                    class Test {
                        void test() {
                            Map<String, Integer> map = Collections.emptyMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.Map;

                    class Test {
                        void test() {
                            Map<String, Integer> map = Maps.fixedSize.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceCollectionsSingletonListWithListsFactory() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> list = Collections.singletonList("hello");
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> list = Lists.fixedSize.of("hello");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceCollectionsSingletonWithSetsFactory() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.Set;

                    class Test {
                        void test() {
                            Set<String> set = Collections.singleton("hello");
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.Set;

                    class Test {
                        void test() {
                            Set<String> set = Sets.fixedSize.of("hello");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceCollectionsSingletonMapWithMapsFactory() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.Map;

                    class Test {
                        void test() {
                            Map<String, Integer> map = Collections.singletonMap("key", 42);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    import java.util.Map;

                    class Test {
                        void test() {
                            Map<String, Integer> map = Maps.fixedSize.of("key", 42);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleCollectionUtilities() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;
                    import java.util.Set;
                    import java.util.Map;

                    class Test {
                        void test() {
                            List<String> emptyList = Collections.emptyList();
                            Set<String> emptySet = Collections.emptySet();
                            Map<String, Integer> emptyMap = Collections.emptyMap();
                            List<String> singletonList = Collections.singletonList("hello");
                            Set<String> singleton = Collections.singleton("world");
                            Map<String, Integer> singletonMap = Collections.singletonMap("key", 42);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.List;
                    import java.util.Set;
                    import java.util.Map;

                    class Test {
                        void test() {
                            List<String> emptyList = Lists.fixedSize.empty();
                            Set<String> emptySet = Sets.fixedSize.empty();
                            Map<String, Integer> emptyMap = Maps.fixedSize.empty();
                            List<String> singletonList = Lists.fixedSize.of("hello");
                            Set<String> singleton = Sets.fixedSize.of("world");
                            Map<String, Integer> singletonMap = Maps.fixedSize.of("key", 42);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacementInFieldDeclarations() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;
                    import java.util.Set;
                    import java.util.Map;

                    class Test {
                        private List<String> emptyList = Collections.emptyList();
                        private Set<String> emptySet = Collections.emptySet();
                        private Map<String, Integer> emptyMap = Collections.emptyMap();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.List;
                    import java.util.Set;
                    import java.util.Map;

                    class Test {
                        private List<String> emptyList = Lists.fixedSize.empty();
                        private Set<String> emptySet = Sets.fixedSize.empty();
                        private Map<String, Integer> emptyMap = Maps.fixedSize.empty();
                    }
                    """
                )
            );
    }

    @Test
    void replacementWithVariableExpressions() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;

                    class Test {
                        void test() {
                            String value = "hello";
                            List<String> list = Collections.singletonList(value);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class Test {
                        void test() {
                            String value = "hello";
                            List<String> list = Lists.fixedSize.of(value);
                        }
                    }
                    """
                )
            );
    }
}
