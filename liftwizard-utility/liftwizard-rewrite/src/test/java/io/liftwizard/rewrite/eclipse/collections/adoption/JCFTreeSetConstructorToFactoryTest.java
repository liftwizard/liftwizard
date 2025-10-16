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
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class JCFTreeSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFTreeSetConstructorToFactory())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.set.sorted;

                        public interface MutableSortedSet<T> extends java.util.SortedSet<T> {
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
    void replaceTreeSetConstructorWithDiamondOperator() {
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
    }

    @Test
    void replaceTreeSetConstructorWithoutGenerics() {
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
    void replaceMultipleTreeSetConstructors() {
        this.rewriteRun(
                java(
                    """
                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            TreeSet<String> set1 = new TreeSet<>();
                            TreeSet set2 = new TreeSet();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.SortedSets;

                    import java.util.TreeSet;

                    class Test {
                        void test() {
                            TreeSet<String> set1 = SortedSets.mutable.empty();
                            TreeSet set2 = SortedSets.mutable.empty();
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
    void treeSetDiamondOperatorShouldNotAddExplicitGenerics() {
        this.rewriteRun(
            java(
                "import java.util.TreeSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = new TreeSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.SortedSets;\n" +
                "\n" +
                "import java.util.Set;\n" +
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
        this.rewriteRun(
            java(
                "import java.util.TreeSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = new TreeSet<String>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.SortedSets;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<String> set = SortedSets.mutable.<String>empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void shouldAddImportAndNotUseFullyQualifiedName() {
        this.rewriteRun(
            java(
                "import java.util.TreeSet;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        java.util.Set<Integer> set = new TreeSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.SortedSets;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        java.util.Set<Integer> set = SortedSets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void nestedGenericsWithDiamondOperator() {
        this.rewriteRun(
            java(
                "import java.util.TreeSet;\n" +
                "import java.util.Set;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<Map.Entry<String, Integer>> set = new TreeSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.SortedSets;\n" +
                "\n" +
                "import java.util.Map;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<Map.Entry<String, Integer>> set = SortedSets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void wildcardGenerics() {
        this.rewriteRun(
            java(
                "import java.util.TreeSet;\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<? extends Number> set = new TreeSet<>();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.SortedSets;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class A {\n" +
                "    void method() {\n" +
                "        Set<? extends Number> set = SortedSets.mutable.empty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
