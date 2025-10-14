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

class JCFSetToMutableSetTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFSetToMutableSet())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.set;

                        public interface MutableSet<T> extends java.util.Set<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.set.MutableSet;

                        public interface Sets {
                            MutableSetFactory mutable = null;
                           \s
                            interface MutableSetFactory {
                                <T> MutableSet<T> empty();
                                <T> MutableSet<T> with(T element);
                                <T> MutableSet<T> of(T element);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.set.mutable;

                        import org.eclipse.collections.api.set.MutableSet;

                        public class UnifiedSet<T> implements MutableSet<T> {
                            public static <T> UnifiedSet<T> newSet() {
                                return new UnifiedSet<>();
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceJavaUtilSetWithMutableSet() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            Set<String> set = Sets.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableSet<String> set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceJavaUtilSetWithMutableSetFullyQualified() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            java.util.Set<String> set = Sets.mutable.empty();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableSet<String> set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceRawTypeSet() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            Set set = Sets.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableSet set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenInitializedWithJavaUtilSet() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            Set<String> set = new HashSet<>();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenNoInitializer() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;

                    class Test {
                        void test() {
                            Set<String> set;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithNestedGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            Set<List<Integer>> set = Sets.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.Set;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableSet<List<Integer>> set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithUnifiedSet() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        void test() {
                            Set<String> set = UnifiedSet.newSet();
                        }
                    }
                    """,
                    """
                    import java.util.Set;

                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        void test() {
                            MutableSet<String> set = UnifiedSet.newSet();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void multipleVariablesSameDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            Set<String> set1 = Sets.mutable.empty(), set2 = Sets.mutable.with("a");
                        }
                    }
                    """,
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableSet<String> set1 = Sets.mutable.empty(), set2 = Sets.mutable.with("a");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void fieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        private Set<String> set = Sets.mutable.empty();
                    }
                    """,
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        private MutableSet<String> set = Sets.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceMethodReturn() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        Set<String> getSet() {
                            return Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceMethodParameter() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void processSets(Set<String> set) {
                            set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }
}
