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

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class JCFMapToMutableMapTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new JCFMapToMutableMap());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        private final Map<String, Integer> fieldMap = Maps.mutable.empty();
                        private final java.util.Map<String, Integer> fullyQualifiedField = Maps.mutable.with("a", 1);

                        void test() {
                            Map<String, Integer> map = Maps.mutable.empty();
                            java.util.Map<String, Integer> fullyQualified = Maps.mutable.empty();
                            Map rawMap = Maps.mutable.empty();
                            java.util.Map rawMapFullyQualified = Maps.mutable.empty();
                            Map<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
                            Map<String, Integer> unifiedMap = UnifiedMap.newMap();
                            Map<String, Integer> map1 = Maps.mutable.empty(), map2 = Maps.mutable.with("a", 1);
                        }
                    }

                    class ConstructorExample {
                        private final Map<String, Integer> fieldMap;

                        ConstructorExample() {
                            Map<String, Integer> localMap = Maps.mutable.empty();
                            this.fieldMap = localMap;
                        }
                    }

                    class WildcardExample {
                        void test() {
                            Map<? extends String, ? extends Integer> extendsMap = Maps.mutable.empty();
                            Map<? super String, ? super Integer> superMap = Maps.mutable.empty();
                            Map<?, ?> unboundedMap = Maps.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        private final MutableMap<String, Integer> fieldMap = Maps.mutable.empty();
                        private final MutableMap<String, Integer> fullyQualifiedField = Maps.mutable.with("a", 1);

                        void test() {
                            MutableMap<String, Integer> map = Maps.mutable.empty();
                            MutableMap<String, Integer> fullyQualified = Maps.mutable.empty();
                            MutableMap rawMap = Maps.mutable.empty();
                            MutableMap rawMapFullyQualified = Maps.mutable.empty();
                            MutableMap<String, List<Integer>> nestedGenerics = Maps.mutable.empty();
                            MutableMap<String, Integer> unifiedMap = UnifiedMap.newMap();
                            MutableMap<String, Integer> map1 = Maps.mutable.empty(), map2 = Maps.mutable.with("a", 1);
                        }
                    }

                    class ConstructorExample {
                        private final Map<String, Integer> fieldMap;

                        ConstructorExample() {
                            MutableMap<String, Integer> localMap = Maps.mutable.empty();
                            this.fieldMap = localMap;
                        }
                    }

                    class WildcardExample {
                        void test() {
                            MutableMap<? extends String, ? extends Integer> extendsMap = Maps.mutable.empty();
                            MutableMap<? super String, ? super Integer> superMap = Maps.mutable.empty();
                            MutableMap<?, ?> unboundedMap = Maps.mutable.empty();
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
                    import java.util.HashMap;
                    import java.util.Map;

                    class Test {
                        Map<String, Integer> getMap() {
                            return Map.of("a", 1);
                        }

                        void processMap(Map<String, Integer> map) {
                            map.get("key");
                        }

                        void test() {
                            Map<String, Integer> hashMap = new HashMap<>();
                            Map<String, Integer> emptyMap = Map.of();
                            Map<String, Integer> mapWithEntry = Map.of("a", 1);
                        }
                    }
                    """
                )
            );
    }
}
