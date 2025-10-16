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

class ECUnifiedMapConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECUnifiedMapConstructorToFactory())
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
                            public UnifiedMap() {}
                            public UnifiedMap(int initialCapacity) {}
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceUnifiedMapConstructorVariations() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap<String, Integer> map = new UnifiedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );

        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap map = new UnifiedMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceUnifiedMapWithArguments() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap<String, Integer> map = new UnifiedMap<>(10);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleUnifiedMapConstructors() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap<String, Integer> map1 = new UnifiedMap<>();
                            UnifiedMap<Long, String> map2 = new UnifiedMap<>();
                            UnifiedMap map3 = new UnifiedMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        void test() {
                            UnifiedMap<String, Integer> map1 = Maps.mutable.empty();
                            UnifiedMap<Long, String> map2 = Maps.mutable.empty();
                            UnifiedMap map3 = Maps.mutable.empty();
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
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        private UnifiedMap<String, String> map = new UnifiedMap<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        private UnifiedMap<String, String> map = Maps.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceHashMap() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            HashMap<String, Integer> map = new HashMap<>();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacementInMethodReturnValue() {
        this.rewriteRun(
                spec ->
                    spec
                        .typeValidationOptions(TypeValidation.none())
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

                                        interface MutableMapFactory {
                                            <K, V> MutableMap<K, V> empty();
                                        }
                                    }
                                    """,
                                    """
                                    package org.eclipse.collections.impl.map.mutable;

                                    import org.eclipse.collections.api.map.MutableMap;

                                    public class UnifiedMap<K, V> implements MutableMap<K, V> {
                                        public UnifiedMap() {}
                                    }
                                    """
                                )
                        ),
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        UnifiedMap<String, Integer> createMap() {
                            return new UnifiedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        UnifiedMap<String, Integer> createMap() {
                            return Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotAddGenericsForNewEmptyOverride() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class A<K, V> {
                        @Override
                        public MutableMap<K, V> newEmpty() {
                            return new UnifiedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A<K, V> {
                        @Override
                        public MutableMap<K, V> newEmpty() {
                            return Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void diamondOperatorShouldNotAddExplicitGenerics() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A {
                        void method() {
                            MutableMap<String, Integer> map = new UnifiedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A {
                        void method() {
                            MutableMap<String, Integer> map = Maps.mutable.empty();
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
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A {
                        void method() {
                            MutableMap<String, Integer> map = new UnifiedMap<String, Integer>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A {
                        void method() {
                            MutableMap<String, Integer> map = Maps.mutable.<String, Integer>empty();
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
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class A {
                        void method() {
                            org.eclipse.collections.api.map.MutableMap<String, Integer> map = new UnifiedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;

                    class A {
                        void method() {
                            org.eclipse.collections.api.map.MutableMap<String, Integer> map = Maps.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void complexGenericTypesAreTransformed() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    import java.util.List;

                    class A {
                        void method() {
                            MutableMap<String, List<Integer>> map = new UnifiedMap<String, List<Integer>>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    import java.util.List;

                    class A {
                        void method() {
                            MutableMap<String, List<Integer>> map = Maps.mutable.<String, List<Integer>>empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void nestedGenericTypesAreTransformed() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A<T> {
                        void method() {
                            MutableMap<String, MutableMap<T, Integer>> map = new UnifiedMap<String, MutableMap<T, Integer>>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;

                    class A<T> {
                        void method() {
                            MutableMap<String, MutableMap<T, Integer>> map = Maps.mutable.<String, MutableMap<T, Integer>>empty();
                        }
                    }
                    """
                )
            );
    }
}
