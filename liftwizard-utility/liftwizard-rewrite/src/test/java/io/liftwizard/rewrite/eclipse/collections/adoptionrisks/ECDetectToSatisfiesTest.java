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

package io.liftwizard.rewrite.eclipse.collections.adoptionrisks;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class ECDetectToSatisfiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECDetectToSatisfies())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacesDetectNotNullWithAnySatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
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
                            return list.detect(s -> s.length() > 5) != null;
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
    void replacesDetectEqualsNullWithNoneSatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
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
                            return list.detect(s -> s.length() > 5) == null;
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
    void replacesNullOnLeftSide() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
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
                            return null != list.detect(s -> s.length() > 5);
                        }

                        boolean test2(MutableList<String> list) {
                            return null == list.detect(s -> s.length() > 5);
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test1(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5);
                        }

                        boolean test2(MutableList<String> list) {
                            return list.noneSatisfy(s -> s.length() > 5);
                        }
                    }"""
                )
            );
    }

    @Test
    void doesNotChangeOtherDetectCalls() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.list;
                                import java.util.function.Predicate;
                                public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
                                    boolean anySatisfy(Predicate<? super T> predicate);
                                    boolean noneSatisfy(Predicate<? super T> predicate);
                                }
                                """,
                                """
                                package org.eclipse.collections.api;
                                import java.util.function.Predicate;
                                public interface RichIterable<T> {
                                    T detect(Predicate<? super T> predicate);
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
                        String test(MutableList<String> list) {
                            String result = list.detect(s -> s.length() > 5);
                            return result != null ? result : "default";
                        }
                    }"""
                )
            );
    }

    @Test
    void worksWithPrimitiveLists() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion().dependsOn(
                                """
                                package org.eclipse.collections.api.block.predicate.primitive;
                                public interface IntPredicate {
                                    boolean accept(int value);
                                }
                                """,
                                """
                                package org.eclipse.collections.api.list.primitive;
                                import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;
                                public interface IntList {
                                    int detectIfNone(IntPredicate predicate, int ifNone);
                                    boolean anySatisfy(IntPredicate predicate);
                                    boolean noneSatisfy(IntPredicate predicate);
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.api.list.primitive.IntList;

                    class Test {
                        boolean test(IntList list) {
                            return list.detectIfNone(i -> i > 5, -1) != -1;
                        }
                    }"""
                )
            );
    }
}
