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

class JCFSetToMutableSetTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new JCFSetToMutableSet());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        private final Set<String> fieldSet = Sets.mutable.empty();

                        void test() {
                            Set<String> simpleSet = Sets.mutable.empty();
                            java.util.Set<String> fullyQualifiedSet = Sets.mutable.empty();
                            Set rawSet = Sets.mutable.empty();
                            java.util.Set rawSetFullyQualified = Sets.mutable.empty();
                            Set<List<Integer>> nestedGenerics = Sets.mutable.empty();
                            Set<String> unifiedSet = UnifiedSet.newSet();
                            Set<String> set1 = Sets.mutable.empty(), set2 = Sets.mutable.with("a");
                            Set<? extends Number> wildcardGenerics = Sets.mutable.empty();
                        }

                        /**
                         * Tests that {@link Set#size()} method works correctly.
                         * Also tests {@link List#size()} method.
                         */
                        void javaDocReference() {
                            Set<String> set = Sets.mutable.empty();
                        }
                    }

                    class InstanceofExample {
                        void method() {
                            Object obj = Sets.mutable.empty();
                            if (obj instanceof Set) {
                                Set<String> set = Sets.mutable.empty();
                            }
                        }
                    }
                    """,
                    """
                    import java.util.Set;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        private final MutableSet<String> fieldSet = Sets.mutable.empty();

                        void test() {
                            MutableSet<String> simpleSet = Sets.mutable.empty();
                            MutableSet<String> fullyQualifiedSet = Sets.mutable.empty();
                            MutableSet rawSet = Sets.mutable.empty();
                            MutableSet rawSetFullyQualified = Sets.mutable.empty();
                            MutableSet<List<Integer>> nestedGenerics = Sets.mutable.empty();
                            MutableSet<String> unifiedSet = UnifiedSet.newSet();
                            MutableSet<String> set1 = Sets.mutable.empty(), set2 = Sets.mutable.with("a");
                            MutableSet<? extends Number> wildcardGenerics = Sets.mutable.empty();
                        }

                        /**
                         * Tests that {@link Set#size()} method works correctly.
                         * Also tests {@link List#size()} method.
                         */
                        void javaDocReference() {
                            MutableSet<String> set = Sets.mutable.empty();
                        }
                    }

                    class InstanceofExample {
                        void method() {
                            Object obj = Sets.mutable.empty();
                            if (obj instanceof Set) {
                                MutableSet<String> set = Sets.mutable.empty();
                            }
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
                    import java.util.HashSet;
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        Set<String> methodReturnType() {
                            return Sets.mutable.empty();
                        }

                        void methodParameter(Set<String> set) {
                            set.size();
                        }

                        void variableWithNonMutableSetInitializer() {
                            Set<String> hashSet = new HashSet<>();
                        }

                        void variableWithoutInitializer() {
                            Set<String> uninitializedSet;
                        }

                        void nonFinalField() {
                            class Inner {
                                private Set<String> nonFinalField = Sets.mutable.empty();
                            }
                        }
                    }

                    interface MyInterface extends Set<String> {
                    }

                    class ImplementsExample implements Set<String> {
                    }

                    class GenericBoundsExample<T extends Set<String>> {
                    }
                    """
                )
            );
    }
}
