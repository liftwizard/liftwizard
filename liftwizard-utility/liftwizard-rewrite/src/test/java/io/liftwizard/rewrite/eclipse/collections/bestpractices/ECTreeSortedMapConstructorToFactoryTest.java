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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class ECTreeSortedMapConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECTreeSortedMapConstructorToFactory())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.map.sorted;

                        public interface MutableSortedMap<K, V> extends java.util.SortedMap<K, V> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                        public interface SortedMaps {
                            MutableSortedMapFactory mutable = null;

                            interface MutableSortedMapFactory {
                                <K, V> MutableSortedMap<K, V> empty();
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.map.sorted.mutable;

                        import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                        public class TreeSortedMap<K, V> implements MutableSortedMap<K, V> {
                            public TreeSortedMap() {}
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void sortedMapReplacement() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                    class A {
                        void method() {
                            MutableSortedMap<String, Integer> map = new TreeSortedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                    class A {
                        void method() {
                            MutableSortedMap<String, Integer> map = SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void rawTypeTreeSortedMap() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        void test() {
                            TreeSortedMap map = new TreeSortedMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        void test() {
                            TreeSortedMap map = SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void fieldDeclarationTreeSortedMap() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        private TreeSortedMap<String, String> map = new TreeSortedMap<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        private TreeSortedMap<String, String> map = SortedMaps.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void methodReturnValueTreeSortedMap() {
        this.rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.none()),
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        TreeSortedMap<String, Integer> createMap() {
                            return new TreeSortedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        TreeSortedMap<String, Integer> createMap() {
                            return SortedMaps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void explicitGenericsAreTransformed() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                    class A {
                        void method() {
                            MutableSortedMap<String, Integer> map = new TreeSortedMap<String, Integer>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                    class A {
                        void method() {
                            MutableSortedMap<String, Integer> map = SortedMaps.mutable.<String, Integer>empty();
                        }
                    }
                    """
                )
            );
    }
}
