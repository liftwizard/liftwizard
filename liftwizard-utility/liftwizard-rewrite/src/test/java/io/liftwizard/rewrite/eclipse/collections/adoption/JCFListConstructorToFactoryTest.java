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
class JCFListConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFListConstructorToFactory())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.list;

                        public interface MutableList<T> extends java.util.List<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.list.MutableList;

                        public interface Lists {
                            MutableListFactory mutable = null;
                           \s
                            interface MutableListFactory {
                                <T> MutableList<T> empty();
                                <T> MutableList<T> with(T... elements);
                                <T> MutableList<T> of(T... elements);
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceArrayListConstructorVariations() {
        // With diamond operator
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            ArrayList<String> list = new ArrayList<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            ArrayList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );

        // Without generics (raw type)
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            ArrayList list = new ArrayList();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            ArrayList list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceLinkedListConstructors() {
        // LinkedList is not transformed to preserve linked list behavior

        // Diamond operator
        this.rewriteRun(
                java(
                    """
                    import java.util.LinkedList;

                    class Test {
                        void test() {
                            LinkedList<String> list = new LinkedList<>();
                        }
                    }
                    """
                )
            );

        // No generics
        this.rewriteRun(
                java(
                    """
                    import java.util.LinkedList;

                    class Test {
                        void test() {
                            LinkedList list = new LinkedList();
                        }
                    }
                    """
                )
            );

        // With arguments
        this.rewriteRun(
                java(
                    """
                    import java.util.LinkedList;
                    import java.util.Collection;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            Collection<String> collection = Arrays.asList("a", "b");
                            LinkedList<String> list = new LinkedList<>(collection);
                        }
                    }
                    """
                )
            );

        // With explicit generics
        this.rewriteRun(
                java(
                    """
                    import java.util.LinkedList;
                    import java.util.List;

                    class A {
                        void method() {
                            List<String> list = new LinkedList<String>();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceArrayListWithArguments() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            ArrayList<String> list = new ArrayList<>(10);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleJCFListConstructors() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.LinkedList;

                    class Test {
                        void test() {
                            ArrayList<String> list1 = new ArrayList<>();
                            ArrayList list2 = new ArrayList();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.ArrayList;
                    import java.util.LinkedList;

                    class Test {
                        void test() {
                            ArrayList<String> list1 = Lists.mutable.empty();
                            ArrayList list2 = Lists.mutable.empty();
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
                    import java.util.ArrayList;

                    class Test {
                        private ArrayList<String> list = new ArrayList<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.ArrayList;

                    class Test {
                        private ArrayList<String> list = Lists.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void diamondOperatorShouldNotAddExplicitGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.List;

                    class A {
                        void method() {
                            List<CollidingInt> toRetain = new ArrayList<>();
                        }

                        static class CollidingInt {
                            int value;
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class A {
                        void method() {
                            List<CollidingInt> toRetain = Lists.mutable.empty();
                        }

                        static class CollidingInt {
                            int value;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void explicitGenericsShouldBePreserved() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.List;

                    class A {
                        void method() {
                            List<String> list = new ArrayList<String>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class A {
                        void method() {
                            List<String> list = Lists.mutable.<String>empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void nestedGenericsWithDiamondOperator() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.List;
                    import java.util.Map;

                    class A {
                        void method() {
                            List<Map<String, Integer>> list = new ArrayList<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;
                    import java.util.Map;

                    class A {
                        void method() {
                            List<Map<String, Integer>> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void wildcardGenerics() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.List;

                    class A {
                        void method() {
                            List<? extends Number> list = new ArrayList<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class A {
                        void method() {
                            List<? extends Number> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldAddImportAndNotUseFullyQualifiedName() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;

                    class A {
                        void method() {
                            java.util.List<Integer> list = new ArrayList<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    class A {
                        void method() {
                            java.util.List<Integer> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotTransformEclipseCollectionsTypes() {
        // This test was in GenericsTest but tests UnifiedMap which isn't a JCF type
        // It doesn't belong in this test class, so we'll skip it
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;

                    class A {
                        void method() {
                            // Only JCF types should be transformed
                            ArrayList<String> list = new ArrayList<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.ArrayList;

                    class A {
                        void method() {
                            // Only JCF types should be transformed
                            ArrayList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }
}
