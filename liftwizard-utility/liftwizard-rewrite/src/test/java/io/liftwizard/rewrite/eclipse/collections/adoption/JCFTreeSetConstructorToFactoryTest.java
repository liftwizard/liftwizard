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

import static org.openrewrite.java.Assertions.java;

class JCFTreeSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new JCFTreeSetConstructorToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeSet;
                    import java.util.SortedSet;
                    import java.util.List;
                    import java.util.Comparator;

                    class Test {
                        private TreeSet<String> fieldSet = new TreeSet<>();

                        void test() {
                            TreeSet<String> diamondSet = new TreeSet<>();
                            TreeSet rawSet = new TreeSet();
                            SortedSet<String> typeInference = new TreeSet<>();
                            SortedSet<List<String>> nestedGenerics = new TreeSet<>();
                            SortedSet<? extends Number> wildcardGenerics = new TreeSet<>();
                            SortedSet<String> explicitSimple = new TreeSet<String>();
                            SortedSet<List<String>> explicitNested = new TreeSet<List<String>>();
                            java.util.SortedSet<String> fullyQualified = new TreeSet<>();
                            TreeSet<String> withComparator = new TreeSet<>(Comparator.naturalOrder());
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.Comparator;
                    import java.util.List;
                    import java.util.SortedSet;
                    import java.util.TreeSet;

                    class Test {
                        private TreeSet<String> fieldSet = new TreeSet<>();

                        void test() {
                            TreeSet<String> diamondSet = new TreeSet<>();
                            TreeSet rawSet = new TreeSet();
                            SortedSet<String> typeInference = SortedSets.mutable.empty();
                            SortedSet<List<String>> nestedGenerics = SortedSets.mutable.empty();
                            SortedSet<? extends Number> wildcardGenerics = SortedSets.mutable.empty();
                            SortedSet<String> explicitSimple = SortedSets.mutable.<String>empty();
                            SortedSet<List<String>> explicitNested = SortedSets.mutable.<List<String>>empty();
                            java.util.SortedSet<String> fullyQualified = SortedSets.mutable.empty();
                            TreeSet<String> withComparator = new TreeSet<>(Comparator.naturalOrder());
                        }
                    }
                    """
                )
            );
    }
}
