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

class ECSimplifyNegatedEmptyChecksTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECSimplifyNegatedEmptyChecks())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacesNegatedIsEmptyWithNotEmptyOnRichIterable() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                public interface RichIterable<T> {
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return !list.isEmpty();
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.notEmpty();
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedNotEmptyWithIsEmpty() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                public interface RichIterable<T> {
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return !list.notEmpty();
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.isEmpty();
                        }
                    }"""
                )
            );
    }

    @Test
    void doesNotChangeNonNegatedCalls() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                public interface RichIterable<T> {
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.isEmpty() || list.notEmpty();
                        }
                    }"""
                )
            );
    }

    @Test
    void shouldNotReplaceInsideNotEmptyMethod() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A implements MutableList<String> {
                        @Override
                        public boolean notEmpty() {
                            return !this.isEmpty();
                        }

                        @Override
                        public boolean isEmpty() {
                            return this.size() == 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotReplaceInsideIsEmptyMethod() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A implements MutableList<String> {
                        @Override
                        public boolean isEmpty() {
                            return !this.notEmpty();
                        }

                        @Override
                        public boolean notEmpty() {
                            return this.size() > 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldReplaceOutsideTheseMethods() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        void method(MutableList<String> list) {
                            if (!list.isEmpty()) {
                                doWork();
                            }
                        }
                        void doWork() {}
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        void method(MutableList<String> list) {
                            if (list.notEmpty()) {
                                doWork();
                            }
                        }
                        void doWork() {}
                    }
                    """
                )
            );
    }

    @Test
    void shouldReplaceNonThisCalls() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int size();
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                public interface RichIterable<T> {
                                    int size();
                                    boolean isEmpty();
                                    boolean notEmpty();
                                }
                                """,
                                """
                                package org.eclipse.collections.impl.list.mutable;
                                import org.eclipse.collections.api.list.MutableList;
                                public class FastList<T> implements MutableList<T> {
                                    public static <T> FastList<T> newList() { return new FastList<>(); }
                                    public int size() { return 0; }
                                    public boolean isEmpty() { return true; }
                                    public boolean notEmpty() { return false; }
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean notEmpty() {
                            return !this.delegate.isEmpty();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean notEmpty() {
                            return this.delegate.notEmpty();
                        }
                    }
                    """
                )
            );
    }
}
