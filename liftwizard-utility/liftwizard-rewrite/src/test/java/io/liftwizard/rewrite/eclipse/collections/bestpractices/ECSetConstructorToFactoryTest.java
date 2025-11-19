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

class ECSetConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new ECSetConstructorToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import java.util.List;

                    class Test<T> {
                        private final MutableSet<String> fieldInterfaceEmpty = new UnifiedSet<>();
                        private final MutableSet<Integer> fieldInterfaceCapacity = new UnifiedSet<>(16);
                        private final MutableSet<String> fieldInterfaceCollection = new UnifiedSet<>(fieldInterfaceEmpty);

                        void test() {
                            MutableSet<String> diamondSet = new UnifiedSet<>();
                            MutableSet rawSet = new UnifiedSet();
                            MutableSet<List<Integer>> nestedGenerics = new UnifiedSet<>();
                            MutableSet<? extends Number> wildcardGenerics = new UnifiedSet<>();
                            MutableSet<String> explicitSimple = new UnifiedSet<String>();
                            MutableSet<List<String>> explicitNested = new UnifiedSet<List<String>>();
                            MutableSet<MutableSet<T>> nestedTypeParam = new UnifiedSet<MutableSet<T>>();
                            org.eclipse.collections.api.set.MutableSet<String> fullyQualified = new org.eclipse.collections.impl.set.mutable.UnifiedSet<>();
                            MutableSet<String> withCapacity = new UnifiedSet<>(16);
                            MutableSet<Integer> withCapacity32 = new UnifiedSet<>(32);
                            MutableSet<String> setFromOther = new UnifiedSet<>(diamondSet);
                        }
                    }

                    class A<T> {
                        @Override
                        public MutableSet<T> newEmpty() {
                            return new UnifiedSet<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    import java.util.List;

                    class Test<T> {
                        private final MutableSet<String> fieldInterfaceEmpty = Sets.mutable.empty();
                        private final MutableSet<Integer> fieldInterfaceCapacity = Sets.mutable.withInitialCapacity(16);
                        private final MutableSet<String> fieldInterfaceCollection = Sets.mutable.withAll(fieldInterfaceEmpty);

                        void test() {
                            MutableSet<String> diamondSet = Sets.mutable.empty();
                            MutableSet rawSet = Sets.mutable.empty();
                            MutableSet<List<Integer>> nestedGenerics = Sets.mutable.empty();
                            MutableSet<? extends Number> wildcardGenerics = Sets.mutable.empty();
                            MutableSet<String> explicitSimple = Sets.mutable.<String>empty();
                            MutableSet<List<String>> explicitNested = Sets.mutable.<List<String>>empty();
                            MutableSet<MutableSet<T>> nestedTypeParam = Sets.mutable.<MutableSet<T>>empty();
                            org.eclipse.collections.api.set.MutableSet<String> fullyQualified = Sets.mutable.empty();
                            MutableSet<String> withCapacity = Sets.mutable.withInitialCapacity(16);
                            MutableSet<Integer> withCapacity32 = Sets.mutable.withInitialCapacity(32);
                            MutableSet<String> setFromOther = Sets.mutable.withAll(diamondSet);
                        }
                    }

                    class A<T> {
                        @Override
                        public MutableSet<T> newEmpty() {
                            return Sets.mutable.empty();
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
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        private final UnifiedSet<String> fieldConcreteType = new UnifiedSet<>();

                        void test() {
                            UnifiedSet<String> concreteTypeEmpty = new UnifiedSet<>();
                            UnifiedSet<String> concreteTypeCapacity = new UnifiedSet<>(10);
                            UnifiedSet<String> concreteTypeCollection = new UnifiedSet<>(concreteTypeEmpty);
                        }
                    }
                    """
                )
            );
    }
}
