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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class ECSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECSetConstructorToFactory())
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        private MutableSet<String> fieldSet = new UnifiedSet<>();

                        void method() {
                            MutableSet<String> diamondSet = new UnifiedSet<>();
                            MutableSet rawSet = new UnifiedSet();
                            MutableSet<Integer> anotherSet = new UnifiedSet<>();
                            MutableSet<String> localSet = new UnifiedSet<>();
                            // This is a set
                            MutableSet<String> commentedSet = new UnifiedSet<>(); // inline comment
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        private MutableSet<String> fieldSet = Sets.mutable.empty();

                        void method() {
                            MutableSet<String> diamondSet = Sets.mutable.empty();
                            MutableSet rawSet = Sets.mutable.empty();
                            MutableSet<Integer> anotherSet = Sets.mutable.empty();
                            MutableSet<String> localSet = Sets.mutable.empty();
                            // This is a set
                            MutableSet<String> commentedSet = Sets.mutable.empty(); // inline comment
                        }
                    }
                    """
                )
            );
    }

    @Test
    void initialCapacityConstructor() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet<String> setWithCapacity = new UnifiedSet<>(16);
                        MutableSet<Integer> anotherSetWithCapacity = new UnifiedSet<>(32);
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        MutableSet<String> setWithCapacity = Sets.mutable.withInitialCapacity(16);
                        MutableSet<Integer> anotherSetWithCapacity = Sets.mutable.withInitialCapacity(32);
                    }
                    """
                )
            );
    }

    @Test
    void collectionConstructor() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet<String> emptySet = new UnifiedSet<>();
                        MutableSet<String> setFromOther = new UnifiedSet<>(emptySet);

                        void method() {
                            MutableSet<Integer> numbers = new UnifiedSet<>();
                            MutableSet<Integer> setFromNumbers = new UnifiedSet<>(numbers);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        MutableSet<String> emptySet = Sets.mutable.empty();
                        MutableSet<String> setFromOther = Sets.mutable.withAll(emptySet);

                        void method() {
                            MutableSet<Integer> numbers = Sets.mutable.empty();
                            MutableSet<Integer> setFromNumbers = Sets.mutable.withAll(numbers);
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
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.SetAdapter;

                    import java.util.HashSet;

                    class Test {
                        MutableSet<String> adaptedSet = SetAdapter.adapt(new HashSet<>());
                    }
                    """
                )
            );
    }
}
