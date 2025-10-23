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

class JCFHashSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new JCFHashSetConstructorToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;
                    import java.util.Set;
                    import java.util.List;

                    class Test {
                        private HashSet<String> fieldSet = new HashSet<>();

                        void test() {
                            HashSet<String> diamondSet = new HashSet<>();
                            HashSet rawSet = new HashSet();
                            Set<String> typeInference = new HashSet<>();
                            Set<List<String>> nestedGenerics = new HashSet<>();
                            Set<? extends Number> wildcardGenerics = new HashSet<>();
                            Set<String> explicitSimple = new HashSet<String>();
                            Set<List<String>> explicitNested = new HashSet<List<String>>();
                            java.util.Set<String> fullyQualified = new HashSet<>();
                            HashSet<String> withInitialCapacity = new HashSet<>(10);
                            Set<String> withCapacity20 = new HashSet<>(20);
                            Set<String> explicit30 = new HashSet<String>(30);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.HashSet;
                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        private HashSet<String> fieldSet = new HashSet<>();

                        void test() {
                            HashSet<String> diamondSet = new HashSet<>();
                            HashSet rawSet = new HashSet();
                            Set<String> typeInference = Sets.mutable.empty();
                            Set<List<String>> nestedGenerics = Sets.mutable.empty();
                            Set<? extends Number> wildcardGenerics = Sets.mutable.empty();
                            Set<String> explicitSimple = Sets.mutable.<String>empty();
                            Set<List<String>> explicitNested = Sets.mutable.<List<String>>empty();
                            java.util.Set<String> fullyQualified = Sets.mutable.empty();
                            HashSet<String> withInitialCapacity = new HashSet<>(10);
                            Set<String> withCapacity20 = Sets.mutable.withInitialCapacity(20);
                            Set<String> explicit30 = Sets.mutable.<String>withInitialCapacity(30);
                        }
                    }
                    """
                )
            );
    }
}
