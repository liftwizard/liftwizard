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
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ECSimplifyNegatedSatisfiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECSimplifyNegatedSatisfies())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacesNegatedNoneSatisfyWithAnySatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return !list.noneSatisfy(s -> s.length() > 5);
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5);
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedAnySatisfyWithNoneSatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return !list.anySatisfy(s -> s.length() > 5);
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.noneSatisfy(s -> s.length() > 5);
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
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5) || list.noneSatisfy(s -> s.isEmpty());
                        }
                    }"""
                )
            );
    }

    @Test
    void shouldNotReplaceInsideNoneSatisfyMethod() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.RichIterable;
                    import java.util.function.Predicate;

                    class A implements RichIterable<String> {
                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return !this.anySatisfy(predicate);
                        }

                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return false;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldNotReplaceInsideAnySatisfyMethod() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.RichIterable;
                    import java.util.function.Predicate;

                    class A implements RichIterable<String> {
                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return !this.noneSatisfy(predicate);
                        }

                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return true;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void shouldReplaceOutsideNoneSatisfyMethod() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        void method(MutableList<String> list) {
                            if (!list.noneSatisfy(s -> s.isEmpty())) {
                                this.doWork();
                            }
                        }
                        void doWork() {}
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class A {
                        void method(MutableList<String> list) {
                            if (list.anySatisfy(s -> s.isEmpty())) {
                                this.doWork();
                            }
                        }
                        void doWork() {}
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
                    import org.eclipse.collections.api.RichIterable;
                    import java.util.function.Predicate;

                    class A implements RichIterable<String> {
                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return !this.anySatisfy(predicate);
                        }

                        public boolean hasNoMatches(Predicate<? super String> predicate) {
                            return !this.anySatisfy(predicate);
                        }

                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return false;
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.RichIterable;
                    import java.util.function.Predicate;

                    class A implements RichIterable<String> {
                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return !this.anySatisfy(predicate);
                        }

                        public boolean hasNoMatches(Predicate<? super String> predicate) {
                            return this.noneSatisfy(predicate);
                        }

                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return false;
                        }
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
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.impl.list.mutable;
                                import org.eclipse.collections.api.list.MutableList;
                                import java.util.function.Predicate;
                                public class FastList<T> implements MutableList<T> {
                                    public static <T> FastList<T> newList() { return new FastList<>(); }
                                    public boolean anySatisfy(Predicate<? super T> predicate) { return false; }
                                    public boolean noneSatisfy(Predicate<? super T> predicate) { return true; }
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import java.util.function.Predicate;

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return !delegate.anySatisfy(predicate);
                        }

                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return this.delegate.anySatisfy(predicate);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import java.util.function.Predicate;

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return delegate.noneSatisfy(predicate);
                        }

                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return this.delegate.anySatisfy(predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doesNotReplaceInCircularNoneSatisfyImplementation() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.RichIterable;
                    import java.util.function.Predicate;

                    class CircularNoneSatisfy implements RichIterable<String> {
                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return !this.anySatisfy(predicate);
                        }

                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return !this.noneSatisfy(predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doesNotReplaceInCircularAnySatisfyImplementation() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.RichIterable;
                    import java.util.function.Predicate;

                    class CircularAnySatisfy implements RichIterable<String> {
                        @Override
                        public boolean anySatisfy(Predicate<? super String> predicate) {
                            return !this.noneSatisfy(predicate);
                        }

                        @Override
                        public boolean noneSatisfy(Predicate<? super String> predicate) {
                            return !this.anySatisfy(predicate);
                        }
                    }
                    """
                )
            );
    }
}
