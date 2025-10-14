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
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;
                    import java.util.List;

                    class Test<T> {
                        private UnifiedMap<String, String> fieldMap = new UnifiedMap<>();

                        void test() {
                            UnifiedMap<String, Integer> diamondMap = new UnifiedMap<>();
                            UnifiedMap rawMap = new UnifiedMap();
                            MutableMap<String, Integer> typeInference = new UnifiedMap<>();
                            MutableMap<String, List<Integer>> nestedGenerics = new UnifiedMap<>();
                            MutableMap<String, ?> wildcardGenerics = new UnifiedMap<>();
                            MutableMap<String, ? extends Number> boundedWildcards = new UnifiedMap<>();
                            MutableMap<String, Integer> explicitSimple = new UnifiedMap<String, Integer>();
                            MutableMap<String, List<Integer>> explicitNested = new UnifiedMap<String, List<Integer>>();
                            MutableMap<String, MutableMap<T, Integer>> nestedTypeParam = new UnifiedMap<String, MutableMap<T, Integer>>();
                            org.eclipse.collections.api.map.MutableMap<String, Integer> fullyQualified = new UnifiedMap<>();
                        }

                        UnifiedMap<String, Integer> createMap() {
                            return new UnifiedMap<>();
                        }
                    }

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
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    import java.util.List;

                    class Test<T> {
                        private UnifiedMap<String, String> fieldMap = new UnifiedMap<>();

                        void test() {
                            UnifiedMap<String, Integer> diamondMap = new UnifiedMap<>();
                            UnifiedMap rawMap = new UnifiedMap();
                            MutableMap<String, Integer> typeInference = Maps.mutable.empty();
                            MutableMap<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
                            MutableMap<String, ?> wildcardGenerics = Maps.mutable.empty();
                            MutableMap<String, ? extends Number> boundedWildcards = Maps.mutable.empty();
                            MutableMap<String, Integer> explicitSimple = Maps.mutable.<String, Integer>empty();
                            MutableMap<String, List<Integer>> explicitNested = Maps.mutable.<String, List<Integer>>empty();
                            MutableMap<String, MutableMap<T, Integer>> nestedTypeParam = Maps.mutable.<String, MutableMap<T, Integer>>empty();
                            org.eclipse.collections.api.map.MutableMap<String, Integer> fullyQualified = Maps.mutable.empty();
                        }

                        UnifiedMap<String, Integer> createMap() {
                            return new UnifiedMap<>();
                        }
                    }

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
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            UnifiedMap<String, Integer> mapWithCapacity = new UnifiedMap<>(10);
                            HashMap<String, Integer> jdkMap = new HashMap<>();
                        }
                    }
                    """
                )
            );
    }
}
