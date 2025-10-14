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
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                    class Test {
                        private TreeSortedMap<String, String> fieldMap = new TreeSortedMap<>();

                        void test() {
                            TreeSortedMap<String, Integer> diamondMap = new TreeSortedMap<>();
                            TreeSortedMap rawMap = new TreeSortedMap();
                            MutableSortedMap<String, Integer> typeInference = new TreeSortedMap<>();
                            TreeSortedMap<String, Integer> explicitSimple = new TreeSortedMap<String, Integer>();
                        }

                        TreeSortedMap<String, Integer> createMap() {
                            return new TreeSortedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;

                    class Test {
                        private TreeSortedMap<String, String> fieldMap = new TreeSortedMap<>();

                        void test() {
                            TreeSortedMap<String, Integer> diamondMap = new TreeSortedMap<>();
                            TreeSortedMap rawMap = new TreeSortedMap();
                            MutableSortedMap<String, Integer> typeInference = SortedMaps.mutable.empty();
                            TreeSortedMap<String, Integer> explicitSimple = new TreeSortedMap<String, Integer>();
                        }

                        TreeSortedMap<String, Integer> createMap() {
                            return new TreeSortedMap<>();
                        }
                    }
                    """
                )
            );
    }
}
