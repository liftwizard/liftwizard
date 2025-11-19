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

class JCFCollectionToMutableCollectionTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new JCFCollectionToMutableCollection());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collection;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        private final Collection<String> fieldCollection = Lists.mutable.empty();

                        void test() {
                            Collection<String> simpleCollection = Lists.mutable.with("a", "b");
                            java.util.Collection<String> fullyQualifiedCollection = Sets.mutable.empty();
                            Collection rawCollection = Lists.mutable.empty();
                            java.util.Collection fullyQualifiedRawCollection = Sets.mutable.empty();
                            Collection<List<Integer>> nestedGenericsCollection = Lists.mutable.empty();
                            Collection<String> fastList = FastList.newList();
                            Collection<String> collection1 = Lists.mutable.empty(), collection2 = Sets.mutable.with("x");
                            Collection<? extends Number> wildcardGenerics = Lists.mutable.empty();
                        }

                        /**
                         * Tests that {@link Collection#size()} method works correctly.
                         * Also tests {@link List#size()} method.
                         */
                        void javaDocReference() {
                            Collection<String> collection = Lists.mutable.empty();
                        }
                    }

                    class InstanceofExample {
                        void method() {
                            Object obj = Lists.mutable.empty();
                            if (obj instanceof Collection) {
                                Collection<String> collection = Sets.mutable.empty();
                            }
                        }
                    }
                    """,
                    """
                    import java.util.Collection;
                    import java.util.List;

                    import org.eclipse.collections.api.collection.MutableCollection;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        private final MutableCollection<String> fieldCollection = Lists.mutable.empty();

                        void test() {
                            MutableCollection<String> simpleCollection = Lists.mutable.with("a", "b");
                            MutableCollection<String> fullyQualifiedCollection = Sets.mutable.empty();
                            MutableCollection rawCollection = Lists.mutable.empty();
                            MutableCollection fullyQualifiedRawCollection = Sets.mutable.empty();
                            MutableCollection<List<Integer>> nestedGenericsCollection = Lists.mutable.empty();
                            MutableCollection<String> fastList = FastList.newList();
                            MutableCollection<String> collection1 = Lists.mutable.empty(), collection2 = Sets.mutable.with("x");
                            MutableCollection<? extends Number> wildcardGenerics = Lists.mutable.empty();
                        }

                        /**
                         * Tests that {@link Collection#size()} method works correctly.
                         * Also tests {@link List#size()} method.
                         */
                        void javaDocReference() {
                            MutableCollection<String> collection = Lists.mutable.empty();
                        }
                    }

                    class InstanceofExample {
                        void method() {
                            Object obj = Lists.mutable.empty();
                            if (obj instanceof Collection) {
                                MutableCollection<String> collection = Sets.mutable.empty();
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
                    import java.util.ArrayList;
                    import java.util.Collection;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        Collection<String> methodReturnType() {
                            return Lists.mutable.empty();
                        }

                        void methodParameter(Collection<String> collection) {
                            collection.size();
                        }

                        void variableWithNonMutableCollectionInitializer() {
                            Collection<String> arrayList = new ArrayList<>();
                        }

                        void variableWithoutInitializer() {
                            Collection<String> uninitializedCollection;
                        }
                    }

                    interface MyInterface extends Collection<String> {
                    }

                    class ImplementsExample implements Collection<String> {
                    }

                    class GenericBoundsExample<T extends Collection<String>> {
                    }
                    """
                )
            );
    }
}
