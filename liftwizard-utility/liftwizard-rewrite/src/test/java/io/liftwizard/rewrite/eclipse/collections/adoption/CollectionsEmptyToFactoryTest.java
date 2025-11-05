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

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class CollectionsEmptyToFactoryTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new CollectionsEmptyToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;
                    import java.util.Map;
                    import java.util.Set;
                    import java.util.SortedMap;
                    import java.util.SortedSet;

                    class Test {
                        private final List<String> emptyListField = Collections.emptyList();
                        private final Set<Integer> emptySetField = Collections.emptySet();
                        private final Map<String, Integer> emptyMapField = Collections.emptyMap();
                        private final SortedSet<String> emptySortedSetField = Collections.emptySortedSet();
                        private final SortedMap<String, Integer> emptySortedMapField = Collections.emptySortedMap();

                        private final List<String> emptyListConstructor;
                        private final Set<Integer> emptySetConstructor;
                        private final Map<String, Integer> emptyMapConstructor;
                        private final SortedSet<String> emptySortedSetConstructor;
                        private final SortedMap<String, Integer> emptySortedMapConstructor;

                        Test() {
                            this.emptyListConstructor = Collections.emptyList();
                            this.emptySetConstructor = Collections.emptySet();
                            this.emptyMapConstructor = Collections.emptyMap();
                            this.emptySortedSetConstructor = Collections.emptySortedSet();
                            this.emptySortedMapConstructor = Collections.emptySortedMap();
                        }

                        void test() {
                            List<String> emptyList = Collections.emptyList();
                            List<String> emptyListExplicit = Collections.<String>emptyList();
                            Set<String> emptySet = Collections.emptySet();
                            Set<String> emptySetExplicit = Collections.<String>emptySet();
                            Map<String, Integer> emptyMap = Collections.emptyMap();
                            Map<String, Integer> emptyMapExplicit = Collections.<String, Integer>emptyMap();
                            SortedSet<String> emptySortedSet = Collections.emptySortedSet();
                            SortedSet<String> emptySortedSetExplicit = Collections.<String>emptySortedSet();
                            SortedMap<String, Integer> emptySortedMap = Collections.emptySortedMap();
                            SortedMap<String, Integer> emptySortedMapExplicit = Collections.<String, Integer>emptySortedMap();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.factory.SortedMaps;
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.List;
                    import java.util.Map;
                    import java.util.Set;
                    import java.util.SortedMap;
                    import java.util.SortedSet;

                    class Test {
                        private final List<String> emptyListField = Lists.fixedSize.empty();
                        private final Set<Integer> emptySetField = Sets.fixedSize.empty();
                        private final Map<String, Integer> emptyMapField = Maps.fixedSize.empty();
                        private final SortedSet<String> emptySortedSetField = SortedSets.mutable.empty();
                        private final SortedMap<String, Integer> emptySortedMapField = SortedMaps.mutable.empty();

                        private final List<String> emptyListConstructor;
                        private final Set<Integer> emptySetConstructor;
                        private final Map<String, Integer> emptyMapConstructor;
                        private final SortedSet<String> emptySortedSetConstructor;
                        private final SortedMap<String, Integer> emptySortedMapConstructor;

                        Test() {
                            this.emptyListConstructor = Lists.fixedSize.empty();
                            this.emptySetConstructor = Sets.fixedSize.empty();
                            this.emptyMapConstructor = Maps.fixedSize.empty();
                            this.emptySortedSetConstructor = SortedSets.mutable.empty();
                            this.emptySortedMapConstructor = SortedMaps.mutable.empty();
                        }

                        void test() {
                            List<String> emptyList = Lists.fixedSize.empty();
                            List<String> emptyListExplicit = Lists.fixedSize.<String>empty();
                            Set<String> emptySet = Sets.fixedSize.empty();
                            Set<String> emptySetExplicit = Sets.fixedSize.<String>empty();
                            Map<String, Integer> emptyMap = Maps.fixedSize.empty();
                            Map<String, Integer> emptyMapExplicit = Maps.fixedSize.<String, Integer>empty();
                            SortedSet<String> emptySortedSet = SortedSets.mutable.empty();
                            SortedSet<String> emptySortedSetExplicit = SortedSets.mutable.<String>empty();
                            SortedMap<String, Integer> emptySortedMap = SortedMaps.mutable.empty();
                            SortedMap<String, Integer> emptySortedMapExplicit = SortedMaps.mutable.<String, Integer>empty();
                        }
                    }
                    """
                )
            );
    }
}
