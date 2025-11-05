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

class ECSortedMapConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new ECSortedMapConstructorToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;
                    import java.util.Comparator;
                    import java.util.List;
                    import java.util.SortedMap;
                    import java.util.TreeMap;

                    class Test<T extends Comparable<T>> {
                        private final MutableSortedMap<String, String> fieldInterfaceEmpty = new TreeSortedMap<>();
                        private final MutableSortedMap<String, String> fieldInterfaceComparator = new TreeSortedMap<>(Comparator.naturalOrder());
                        private final SortedMap<String, String> regularSortedMap = new TreeMap<>();
                        private final MutableSortedMap<String, String> fieldInterfaceSortedMap = new TreeSortedMap<>(regularSortedMap);

                        void test() {
                            SortedMap<String, Integer> localSortedMap = new TreeMap<>();
                            MutableSortedMap<String, Integer> diamondMap = new TreeSortedMap<>();
                            MutableSortedMap<String, List<Integer>> nestedGenerics = new TreeSortedMap<>();
                            MutableSortedMap<String, Integer> explicitSimple = new TreeSortedMap<String, Integer>();
                            MutableSortedMap<String, List<Integer>> explicitNested = new TreeSortedMap<String, List<Integer>>();
                            MutableSortedMap<String, MutableSortedMap<T, Integer>> nestedTypeParam = new TreeSortedMap<String, MutableSortedMap<T, Integer>>();
                            org.eclipse.collections.api.map.sorted.MutableSortedMap<String, Integer> fullyQualified = new org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap<>();
                            MutableSortedMap<String, Integer> withComparator = new TreeSortedMap<>(Comparator.naturalOrder());
                            MutableSortedMap<String, Integer> withSortedMap = new TreeSortedMap<>(localSortedMap);
                        }
                    }

                    class A<K extends Comparable<K>, V> {
                        @Override
                        public MutableSortedMap<K, V> newEmpty() {
                            return new TreeSortedMap<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.api.map.sorted.MutableSortedMap;

                    import java.util.Comparator;
                    import java.util.List;
                    import java.util.SortedMap;
                    import java.util.TreeMap;

                    class Test<T extends Comparable<T>> {
                        private final MutableSortedMap<String, String> fieldInterfaceEmpty = SortedMaps.mutable.empty();
                        private final MutableSortedMap<String, String> fieldInterfaceComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
                        private final SortedMap<String, String> regularSortedMap = new TreeMap<>();
                        private final MutableSortedMap<String, String> fieldInterfaceSortedMap = SortedMaps.mutable.withSortedMap(regularSortedMap);

                        void test() {
                            SortedMap<String, Integer> localSortedMap = new TreeMap<>();
                            MutableSortedMap<String, Integer> diamondMap = SortedMaps.mutable.empty();
                            MutableSortedMap<String, List<Integer>> nestedGenerics = SortedMaps.mutable.empty();
                            MutableSortedMap<String, Integer> explicitSimple = SortedMaps.mutable.<String, Integer>empty();
                            MutableSortedMap<String, List<Integer>> explicitNested = SortedMaps.mutable.<String, List<Integer>>empty();
                            MutableSortedMap<String, MutableSortedMap<T, Integer>> nestedTypeParam = SortedMaps.mutable.<String, MutableSortedMap<T, Integer>>empty();
                            org.eclipse.collections.api.map.sorted.MutableSortedMap<String, Integer> fullyQualified = SortedMaps.mutable.empty();
                            MutableSortedMap<String, Integer> withComparator = SortedMaps.mutable.with(Comparator.naturalOrder());
                            MutableSortedMap<String, Integer> withSortedMap = SortedMaps.mutable.withSortedMap(localSortedMap);
                        }
                    }

                    class A<K extends Comparable<K>, V> {
                        @Override
                        public MutableSortedMap<K, V> newEmpty() {
                            return SortedMaps.mutable.empty();
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
                    import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
                    import java.util.Comparator;
                    import java.util.HashMap;
                    import java.util.Map;

                    class Test {
                        private final TreeSortedMap<String, String> fieldConcreteType = new TreeSortedMap<>();

                        void test() {
                            Map<String, Integer> regularMap = new HashMap<>();
                            TreeSortedMap<String, Integer> concreteTypeEmpty = new TreeSortedMap<>();
                            TreeSortedMap<String, Integer> concreteTypeComparator = new TreeSortedMap<>(Comparator.naturalOrder());
                            TreeSortedMap<String, Integer> concreteTypeMap = new TreeSortedMap<>(regularMap);
                        }
                    }
                    """
                )
            );
    }
}
