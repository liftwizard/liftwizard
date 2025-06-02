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

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class JCFSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFSetConstructorToFactory())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.set;

                        public interface MutableSet<T> extends java.util.Set<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.set.sorted;

                        public interface MutableSortedSet<T> extends java.util.SortedSet<T> {
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
                                <T> MutableSet<T> with(T... elements);
                                <T> MutableSet<T> of(T... elements);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.set.sorted.MutableSortedSet;

                        public interface SortedSets {
                            MutableSortedSetFactory mutable = null;
                           \s
                            interface MutableSortedSetFactory {
                                <T> MutableSortedSet<T> empty();
                                <T> MutableSortedSet<T> with(T... elements);
                                <T> MutableSortedSet<T> of(T... elements);
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceHashSetConstructorVariations() {
        // With diamond operator
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet<String> set = new HashSet<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet<String> set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );

        // Without generics (raw type)
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet set = new HashSet();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet set = Sets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceTreeSetConstructorVariations() {
        // With diamond operator
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            TreeSet<String> set = new TreeSet<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            TreeSet<String> set = SortedSets.mutable.empty();
                        }
                    }
                    """
                )
            );

        // Without generics (raw type)
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            TreeSet set = new TreeSet();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            TreeSet set = SortedSets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceConstructorsWithArguments() {
        // HashSet with initial capacity
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet<String> set = new HashSet<>(10);
                        }
                    }
                    """
                )
            );

        // TreeSet with comparator
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeSet;
                    import java.util.Comparator;

                    class Test {
                        void test() {
                            TreeSet<String> set = new TreeSet<>(Comparator.naturalOrder());
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleJCFSetConstructors() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;
                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            HashSet<String> set1 = new HashSet<>();
                            TreeSet<Integer> set2 = new TreeSet<>();
                            HashSet set3 = new HashSet();
                            TreeSet set4 = new TreeSet();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.HashSet;
                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            HashSet<String> set1 = Sets.mutable.empty();
                            TreeSet<Integer> set2 = SortedSets.mutable.empty();
                            HashSet set3 = Sets.mutable.empty();
                            TreeSet set4 = SortedSets.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacementInFieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;

                    class Test {
                        private HashSet<String> set = new HashSet<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.HashSet;

                    class Test {
                        private HashSet<String> set = Sets.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void replacementInTreeSetFieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeSet;

                    class Test {
                        private TreeSet<String> set = new TreeSet<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.TreeSet;

                    class Test {
                        private TreeSet<String> set = SortedSets.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void hashSetDiamondOperatorShouldNotAddExplicitGenerics() {
        rewriteRun(
            java(
                "import java.util.HashSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = new HashSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.Sets;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = Sets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void treeSetDiamondOperatorShouldNotAddExplicitGenerics() {
        rewriteRun(
            java(
                "import java.util.TreeSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = new TreeSet<>();\n" +
                "    }\n" +
                "}\n",
                "import java.util.Set;\n" +
                "\n" +
                "import org.eclipse.collections.api.factory.SortedSets;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = SortedSets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void explicitGenericsShouldBePreserved() {
        rewriteRun(
            java(
                "import java.util.HashSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = new HashSet<String>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.Sets;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = Sets.mutable.<String>empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void shouldAddImportAndNotUseFullyQualifiedName() {
        rewriteRun(
            java(
                "import java.util.HashSet;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        java.util.Set<Integer> set = new HashSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.Sets;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        java.util.Set<Integer> set = Sets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void nestedGenericsWithDiamondOperator() {
        rewriteRun(
            java(
                "import java.util.HashSet;\n" +
                "import java.util.Set;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<Map.Entry<String, Integer>> set = new HashSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.Sets;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<Map.Entry<String, Integer>> set = Sets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void wildcardGenerics() {
        rewriteRun(
            java(
                "import java.util.HashSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<? extends Number> set = new HashSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.Sets;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<? extends Number> set = Sets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
