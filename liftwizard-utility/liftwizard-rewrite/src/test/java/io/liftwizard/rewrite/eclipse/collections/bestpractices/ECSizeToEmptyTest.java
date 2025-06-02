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

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class ECSizeToEmptyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ECSizeToEmpty()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacesSizeEqualsZeroWithIsEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() == 0;
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
    void replacesSizeGreaterThanZeroWithNotEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() > 0;
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
    void replacesSizeNotEqualsZeroWithNotEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() != 0;
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
    void replacesSizeGreaterThanOrEqualOneWithNotEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() >= 1;
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
    void doesNotChangeOtherComparisons() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() >= 2;
                        }
                    }"""
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
                            return this.size() == 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """
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
                            return this.size() > 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldReplaceOutsideIsEmptyMethod() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        void method(MutableList<String> list) {
                            if (list.size() == 0) {
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
                            if (list.isEmpty()) {
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
                        public boolean isEmpty() {
                            return delegate.size() == 0;
                        }

                        @Override
                        public int size() {
                            return this.delegate.size();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean isEmpty() {
                            return delegate.isEmpty();
                        }

                        @Override
                        public int size() {
                            return this.delegate.size();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldReplaceInOtherMethods() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A implements MutableList<String> {
                        @Override
                        public boolean isEmpty() {
                            return this.size() == 0;
                        }

                        public boolean isReallyEmpty() {
                            return this.size() == 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A implements MutableList<String> {
                        @Override
                        public boolean isEmpty() {
                            return this.size() == 0;
                        }

                        public boolean isReallyEmpty() {
                            return this.isEmpty();
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doesNotReplaceInCircularIsEmptyImplementation() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.RichIterable;

                    class CircularIsEmpty implements RichIterable<String> {
                        @Override
                        public boolean isEmpty() {
                            return !this.notEmpty();
                        }

                        @Override
                        public boolean notEmpty() {
                            return this.size() > 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doesNotReplaceInCircularNotEmptyImplementation() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.RichIterable;

                    class CircularNotEmpty implements RichIterable<String> {
                        @Override
                        public boolean notEmpty() {
                            return !this.isEmpty();
                        }

                        @Override
                        public boolean isEmpty() {
                            return this.size() == 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacesSizeLessThanOneWithIsEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() < 1;
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
    void replacesSizeLessThanOrEqualZeroWithIsEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() <= 0;
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
    void replacesReversedComparisonsWithIsEmpty() {
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
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test1(MutableList<String> list) {
                            return 1 > list.size();
                        }
                        boolean test2(MutableList<String> list) {
                            return 0 >= list.size();
                        }
                        boolean test3(MutableList<String> list) {
                            return 0 == list.size();
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test1(MutableList<String> list) {
                            return list.isEmpty();
                        }
                        boolean test2(MutableList<String> list) {
                            return list.isEmpty();
                        }
                        boolean test3(MutableList<String> list) {
                            return list.isEmpty();
                        }
                    }"""
                )
            );
    }
}
