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

class JCFSortedSetToMutableSortedSetTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new JCFSortedSetToMutableSortedSet());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.SortedSet;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.SortedSets;
                    import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

                    class Test {
                        private final SortedSet<String> fieldSortedSet = SortedSets.mutable.empty();

                        void test() {
                            SortedSet<String> simpleSortedSet = SortedSets.mutable.empty();
                            java.util.SortedSet<String> fullyQualifiedSortedSet = SortedSets.mutable.empty();
                            SortedSet rawSortedSet = SortedSets.mutable.empty();
                            java.util.SortedSet rawSortedSetFullyQualified = SortedSets.mutable.empty();
                            SortedSet<List<Integer>> nestedGenerics = SortedSets.mutable.empty();
                            SortedSet<String> treeSortedSet = TreeSortedSet.newSet();
                            SortedSet<String> set1 = SortedSets.mutable.empty(), set2 = SortedSets.mutable.with("a");
                        }
                    }
                    """,
                    """
                    import java.util.SortedSet;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.SortedSets;
                    import org.eclipse.collections.api.set.sorted.MutableSortedSet;
                    import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;

                    class Test {
                        private final MutableSortedSet<String> fieldSortedSet = SortedSets.mutable.empty();

                        void test() {
                            MutableSortedSet<String> simpleSortedSet = SortedSets.mutable.empty();
                            MutableSortedSet<String> fullyQualifiedSortedSet = SortedSets.mutable.empty();
                            MutableSortedSet rawSortedSet = SortedSets.mutable.empty();
                            MutableSortedSet rawSortedSetFullyQualified = SortedSets.mutable.empty();
                            MutableSortedSet<List<Integer>> nestedGenerics = SortedSets.mutable.empty();
                            MutableSortedSet<String> treeSortedSet = TreeSortedSet.newSet();
                            MutableSortedSet<String> set1 = SortedSets.mutable.empty(), set2 = SortedSets.mutable.with("a");
                        }
                    }
                    """
                )
            );
    }
}
