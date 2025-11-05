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

class JCFHashSetConstructorToFactoryTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new JCFHashSetConstructorToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Arrays;
                    import java.util.Collection;
                    import java.util.HashSet;
                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        private final Set<String> fieldInterfaceEmpty = new HashSet<>();
                        private final Set<Integer> fieldInterfaceCapacity = new HashSet<>(10);
                        private final Set<String> fieldInterfaceCollection = new HashSet<>(Arrays.asList("a", "b"));

                        void test(Collection<String> inputCollection) {
                            Collection<String> collection = new HashSet<>();
                            Set<String> typeInference = new HashSet<>();
                            Set<List<String>> nestedGenerics = new HashSet<>();
                            Set<? extends Number> wildcardGenerics = new HashSet<>();
                            Set<String> explicitSimple = new HashSet<String>();
                            Set<List<String>> explicitNested = new HashSet<List<String>>();
                            java.util.Set<String> fullyQualified = new HashSet<>();
                            Set<String> withCapacity20 = new HashSet<>(20);
                            Set<String> explicit30 = new HashSet<String>(30);
                            Set<String> interfaceFromCollection = new HashSet<>(inputCollection);
                            Set<String> fromList = new HashSet<>(Arrays.asList("x", "y", "z"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.Arrays;
                    import java.util.Collection;
                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        private final Set<String> fieldInterfaceEmpty = Sets.mutable.empty();
                        private final Set<Integer> fieldInterfaceCapacity = Sets.mutable.withInitialCapacity(10);
                        private final Set<String> fieldInterfaceCollection = Sets.mutable.withAll(Arrays.asList("a", "b"));

                        void test(Collection<String> inputCollection) {
                            Collection<String> collection = Sets.mutable.empty();
                            Set<String> typeInference = Sets.mutable.empty();
                            Set<List<String>> nestedGenerics = Sets.mutable.empty();
                            Set<? extends Number> wildcardGenerics = Sets.mutable.empty();
                            Set<String> explicitSimple = Sets.mutable.<String>empty();
                            Set<List<String>> explicitNested = Sets.mutable.<List<String>>empty();
                            java.util.Set<String> fullyQualified = Sets.mutable.empty();
                            Set<String> withCapacity20 = Sets.mutable.withInitialCapacity(20);
                            Set<String> explicit30 = Sets.mutable.<String>withInitialCapacity(30);
                            Set<String> interfaceFromCollection = Sets.mutable.withAll(inputCollection);
                            Set<String> fromList = Sets.mutable.withAll(Arrays.asList("x", "y", "z"));
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
                    import java.util.Collection;
                    import java.util.HashSet;

                    class Test {
                        private final HashSet<String> fieldConcreteType = new HashSet<>();

                        void test(Collection<String> inputCollection) {
                            HashSet<String> diamondSet = new HashSet<>();
                            HashSet rawSet = new HashSet();
                            HashSet<String> withInitialCapacity = new HashSet<>(10);
                            HashSet<String> concreteFromCollection = new HashSet<>(inputCollection);
                        }
                    }
                    """
                )
            );
    }
}
