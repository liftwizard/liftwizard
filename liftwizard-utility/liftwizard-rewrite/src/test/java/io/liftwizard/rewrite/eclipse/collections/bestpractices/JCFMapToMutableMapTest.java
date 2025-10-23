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

class JCFMapToMutableMapTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFMapToMutableMap())
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
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
                        private Map<String, Integer> fieldMap = Maps.mutable.empty();

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
                    """,
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.impl.map.mutable.UnifiedMap;

                    class Test {
                        private MutableMap<String, Integer> fieldMap = Maps.mutable.empty();

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
                    """
                )
            );
    }
}
