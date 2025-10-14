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

class JCFHashSetConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFHashSetConstructorToFactory())
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
                                <T> MutableSet<T> with(T... elements);
                                <T> MutableSet<T> of(T... elements);
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceHashSetConstructorWithDiamondOperator() {
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
    }

    @Test
    void replaceHashSetConstructorWithoutGenerics() {
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
    void doNotReplaceConstructorsWithArguments() {
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
    }

    @Test
    void replaceMultipleHashSetConstructors() {
        this.rewriteRun(
                java(
                    """
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet<String> set1 = new HashSet<>();
                            HashSet set2 = new HashSet();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.HashSet;

                    class Test {
                        void test() {
                            HashSet<String> set1 = Sets.mutable.empty();
                            HashSet set2 = Sets.mutable.empty();
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
    void hashSetDiamondOperatorShouldNotAddExplicitGenerics() {
        this.rewriteRun(
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
    void explicitGenericsShouldBePreserved() {
        this.rewriteRun(
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
        this.rewriteRun(
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
        this.rewriteRun(
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
                "import java.util.Map;\n" +
                "import java.util.Set;\n" +
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
        this.rewriteRun(
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
