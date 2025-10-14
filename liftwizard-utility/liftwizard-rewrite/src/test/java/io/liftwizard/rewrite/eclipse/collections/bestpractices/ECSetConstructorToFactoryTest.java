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

import static org.openrewrite.java.Assertions.java;

class ECSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECSetConstructorToFactory())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api", "eclipse-collections"));
    }

    @Test
    @DocumentExample
    void unifiedSetNoArgumentConstructor() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet<String> set = new UnifiedSet<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.set.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        MutableSet<String> set = Sets.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void unifiedSetNonParameterized() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet set = new UnifiedSet();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.set.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        MutableSet set = Sets.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void doesNotChangeConstructorWithArguments() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet<String> anotherSet = new UnifiedSet<>();
                        MutableSet<String> set1 = new UnifiedSet<>(10);
                        MutableSet<String> set2 = new UnifiedSet<>(anotherSet);
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.set.Sets;
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet<String> anotherSet = Sets.mutable.empty();
                        MutableSet<String> set1 = new UnifiedSet<>(10);
                        MutableSet<String> set2 = new UnifiedSet<>(anotherSet);
                    }
                    """
                )
            );
    }

    @Test
    void preservesComments() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        // This is a set
                        MutableSet<String> set = new UnifiedSet<>(); // inline comment
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.set.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        // This is a set
                        MutableSet<String> set = Sets.mutable.empty(); // inline comment
                    }
                    """
                )
            );
    }

    @Test
    void multipleReplacementsInSameFile() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        MutableSet<String> set1 = new UnifiedSet<>();
                        MutableSet<Integer> set2 = new UnifiedSet<>();
                        MutableSet set3 = new UnifiedSet();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.set.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        MutableSet<String> set1 = Sets.mutable.empty();
                        MutableSet<Integer> set2 = Sets.mutable.empty();
                        MutableSet set3 = Sets.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void preservesTypeInLocalVariable() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        void method() {
                            MutableSet<String> localSet = new UnifiedSet<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.set.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void method() {
                            MutableSet<String> localSet = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doesNotChangeOtherSetImplementations() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.SetAdapter;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    import java.util.HashSet;

                    class Test {
                        MutableSet<String> set1 = SetAdapter.adapt(new HashSet<>());
                        MutableSet<String> set2 = new UnifiedSet<>(5);
                    }
                    """
                )
            );
    }
}
