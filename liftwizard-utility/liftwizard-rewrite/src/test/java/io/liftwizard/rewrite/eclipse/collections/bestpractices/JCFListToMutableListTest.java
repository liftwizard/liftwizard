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

class JCFListToMutableListTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFListToMutableList())
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
                        """,
                        """
                        package org.eclipse.collections.impl.list.mutable;

                        import org.eclipse.collections.api.list.MutableList;

                        public class FastList<T> implements MutableList<T> {
                            public static <T> FastList<T> newList() {
                                return new FastList<>();
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceJavaUtilListWithMutableList() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            List<String> list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceJavaUtilListWithMutableListFullyQualified() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            java.util.List<String> list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceRawTypeList() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            List list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test() {
                            MutableList list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenInitializedWithJavaUtilList() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
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
                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> list;
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
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            List<Map<String, Integer>> list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test() {
                            MutableList<Map<String, Integer>> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithFastList() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            List<String> list = FastList.newList();
                        }
                    }
                    """,
                    """
                    import java.util.List;

                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            MutableList<String> list = FastList.newList();
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
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            List<String> list1 = Lists.mutable.empty(), list2 = Lists.mutable.with("a", "b");
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test() {
                            MutableList<String> list1 = Lists.mutable.empty(), list2 = Lists.mutable.with("a", "b");
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
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        private List<String> list = Lists.mutable.empty();
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        private MutableList<String> list = Lists.mutable.empty();
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
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        List<String> getList() {
                            return Lists.mutable.empty();
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
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void processLists(List<String> list) {
                            list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeMethodReference() {
        this.rewriteRun(
                spec ->
                    spec
                        .typeValidationOptions(TypeValidation.none())
                        .parser(
                            JavaParser.fromJavaVersion().dependsOn(
                                    """
                                    package org.eclipse.collections.api.list;
                                    public interface MutableList<T> extends java.util.List<T> {
                                        void forEachWith(java.util.function.BiConsumer<? super T, ?> consumer, Object parameter);
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
                                    """,
                                    """
                                    package java.util.function;
                                    public interface UnaryOperator<T> extends java.util.function.Function<T, T> {
                                    }
                                    """,
                                    """
                                    package java.util.function;
                                    public interface Function<T, R> {
                                        R apply(T t);
                                    }
                                    """,
                                    """
                                    package java.util.function;
                                    public interface BiConsumer<T, U> {
                                        void accept(T t, U u);
                                    }
                                    """,
                                    """
                                    package java.util;
                                    public interface List<E> extends Collection<E> {
                                        default void replaceAll(java.util.function.UnaryOperator<E> operator) {}
                                    }
                                    """,
                                    """
                                    package java.util;
                                    public interface Collection<E> extends Iterable<E> {
                                    }
                                    """,
                                    """
                                    package java.lang;
                                    public interface Iterable<T> {
                                    }
                                    """
                                )
                        ),
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;
                    import java.util.function.UnaryOperator;

                    class A {
                        void method() {
                            MutableList<MutableList<String>> lists = Lists.mutable.empty();
                            UnaryOperator<String> operator = s -> s.toUpperCase();
                            lists.forEachWith(List::replaceAll, operator);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeJavaDocReferences() {
        this.rewriteRun(
                spec ->
                    spec.parser(
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
                                """,
                                """
                                public class Verify {
                                    public static void assertMapsEqual(String message, java.util.Map<?, ?> expected, java.util.Map<?, ?> actual) {}
                                }
                                """
                            )
                    ),
                java(
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Lists;

                    class A {
                        /**
                         * Tests that {@link Verify#assertMapsEqual(String, Map, Map)} really throw when they ought to.
                         * Also tests {@link List#size()} method.
                         */
                        void method() {
                            List<String> list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        /**
                         * Tests that {@link Verify#assertMapsEqual(String, Map, Map)} really throw when they ought to.
                         * Also tests {@link List#size()} method.
                         */
                        void method() {
                            MutableList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeImplementsClause() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    interface MyInterface extends List<String> {
                    }

                    class A implements List<String> {
                        void method() {
                            List<String> list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    interface MyInterface extends List<String> {
                    }

                    class A implements List<String> {
                        void method() {
                            MutableList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeGenericBounds() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class A<T extends List<String>> {
                        void method() {
                            List<String> list = Lists.mutable.empty();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class A<T extends List<String>> {
                        void method() {
                            MutableList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeCastExpressions() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class A {
                        void method() {
                            Object obj = Lists.mutable.empty();
                            List<String> list = (List<String>) obj;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotChangeInstanceofExpressions() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;

                    class A {
                        void method() {
                            Object obj = Lists.mutable.empty();
                            if (obj instanceof List) {
                                List<String> list = Lists.mutable.empty();
                            }
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        void method() {
                            Object obj = Lists.mutable.empty();
                            if (obj instanceof List) {
                                MutableList<String> list = Lists.mutable.empty();
                            }
                        }
                    }
                    """
                )
            );
    }
}
