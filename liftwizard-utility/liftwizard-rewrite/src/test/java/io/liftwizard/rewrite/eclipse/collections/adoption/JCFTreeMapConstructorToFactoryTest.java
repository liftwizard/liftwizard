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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class JCFTreeMapConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new JCFTreeMapConstructorToFactory()).typeValidationOptions(TypeValidation.none());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeMap;
                    import java.util.SortedMap;
                    import java.util.List;
                    import java.util.Comparator;

                    class Test {
                        private TreeMap<String, String> fieldMap = new TreeMap<>();

                        void test() {
                            TreeMap<String, Integer> diamondMap = new TreeMap<>();
                            TreeMap rawMap = new TreeMap();
                            SortedMap<String, Integer> typeInference = new TreeMap<>();
                            SortedMap<String, List<Integer>> nestedGenerics = new TreeMap<>();
                            SortedMap<String, ? extends Number> wildcardGenerics = new TreeMap<>();
                            SortedMap<String, Integer> explicitSimple = new TreeMap<String, Integer>();
                            SortedMap<String, List<Integer>> explicitNested = new TreeMap<String, List<Integer>>();
                            java.util.SortedMap<String, Integer> fullyQualified = new TreeMap<>();
                            TreeMap<String, Integer> withComparator = new TreeMap<>(Comparator.naturalOrder());
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;

                    import java.util.Comparator;
                    import java.util.List;
                    import java.util.SortedMap;
                    import java.util.TreeMap;

                    class Test {
                        private TreeMap<String, String> fieldMap = new TreeMap<>();

                        void test() {
                            TreeMap<String, Integer> diamondMap = new TreeMap<>();
                            TreeMap rawMap = new TreeMap();
                            SortedMap<String, Integer> typeInference = SortedMaps.mutable.empty();
                            SortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
                            SortedMap<String, ? extends Number> wildcardGenerics = SortedMaps.mutable.empty();
                            SortedMap<String, Integer> explicitSimple = SortedMaps.mutable.<String, Integer>empty();
                            SortedMap<String, List<Integer>> explicitNested = SortedMaps.mutable.<String, List<Integer>>empty();
                            java.util.SortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
                            TreeMap<String, Integer> withComparator = new TreeMap<>(Comparator.naturalOrder());
                        }
                    }
                    """
                )
            );
    }
}
