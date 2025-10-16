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

class ECCountToSatisfiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ECCountToSatisfies()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacesCountEqualsZeroWithNoneSatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                            return list.count(s -> s.length() > 5) == 0;
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
    void replacesCountGreaterThanZeroWithAnySatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                            return list.count(s -> s.length() > 5) > 0;
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
    void replacesCountNotEqualsZeroWithAnySatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                            return list.count(s -> s.length() > 5) != 0;
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
    void replacesCountLessThanOrEqualZeroWithNoneSatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                            return list.count(s -> s.length() > 5) <= 0;
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
    void replacesCountGreaterThanOrEqualOneWithAnySatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                            return list.count(s -> s.length() > 5) >= 1;
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
    void replacesReversedComparisonsWithSatisfies() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                        boolean test1(MutableList<String> list) {
                            return 0 >= list.count(s -> s.length() > 5);
                        }
                        boolean test2(MutableList<String> list) {
                            return 1 <= list.count(s -> s.length() > 5);
                        }
                        boolean test3(MutableList<String> list) {
                            return 0 < list.count(s -> s.length() > 5);
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test1(MutableList<String> list) {
                            return list.noneSatisfy(s -> s.length() > 5);
                        }
                        boolean test2(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5);
                        }
                        boolean test3(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5);
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
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    int count(Predicate<? super T> predicate);
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
                            return list.count(s -> s.length() > 5) >= 2;
                        }
                    }"""
                )
            );
    }
}
