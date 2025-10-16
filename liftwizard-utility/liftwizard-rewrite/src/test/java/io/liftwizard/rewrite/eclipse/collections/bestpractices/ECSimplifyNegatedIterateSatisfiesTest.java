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

class ECSimplifyNegatedIterateSatisfiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECSimplifyNegatedIterateSatisfies())
            .parser(
                JavaParser.fromJavaVersion()
                    .classpath("eclipse-collections-api", "eclipse-collections")
                    .dependsOn(
                        """
                        package org.eclipse.collections.api.block.predicate;

                        public interface Predicate<T> {
                            boolean accept(T each);
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.utility;

                        import org.eclipse.collections.api.block.predicate.Predicate;

                        public final class Iterate {
                            public static <T> boolean anySatisfy(Iterable<T> iterable, Predicate<? super T> predicate) {
                                return false;
                            }

                            public static <T> boolean noneSatisfy(Iterable<T> iterable, Predicate<? super T> predicate) {
                                return false;
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void simplifyNegatedNoneSatisfy() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return !Iterate.noneSatisfy(list, predicate);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return Iterate.anySatisfy(list, predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void simplifyNegatedAnySatisfy() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return !Iterate.anySatisfy(list, predicate);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return Iterate.noneSatisfy(list, predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void simplifyNegatedNoneSatisfyWithParentheses() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return !(Iterate.noneSatisfy(list, predicate));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return Iterate.anySatisfy(list, predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void simplifyMultipleNegatedSatisfies() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        void test(List<String> list, Predicate<String> predicate1, Predicate<String> predicate2) {
                            boolean result1 = !Iterate.noneSatisfy(list, predicate1);
                            boolean result2 = !Iterate.anySatisfy(list, predicate2);

                            if (!Iterate.noneSatisfy(list, predicate1) && !Iterate.anySatisfy(list, predicate2)) {
                                // Both conditions met
                            }
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        void test(List<String> list, Predicate<String> predicate1, Predicate<String> predicate2) {
                            boolean result1 = Iterate.anySatisfy(list, predicate1);
                            boolean result2 = Iterate.noneSatisfy(list, predicate2);

                            if (Iterate.anySatisfy(list, predicate1) && Iterate.noneSatisfy(list, predicate2)) {
                                // Both conditions met
                            }
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotChangeNonNegatedCalls() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        void test(List<String> list, Predicate<String> predicate) {
                            boolean result1 = Iterate.noneSatisfy(list, predicate);
                            boolean result2 = Iterate.anySatisfy(list, predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotChangeDoubleNegation() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list, Predicate<String> predicate) {
                            return !!Iterate.noneSatisfy(list, predicate);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void simplifyNegatedWithLambdaPredicate() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list) {
                            return !Iterate.noneSatisfy(list, str -> str.length() > 5);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list) {
                            return Iterate.anySatisfy(list, str -> str.length() > 5);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void simplifyNegatedWithMethodReference() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list) {
                            return !Iterate.anySatisfy(list, String::isEmpty);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import java.util.List;

                    class Test {
                        boolean test(List<String> list) {
                            return Iterate.noneSatisfy(list, String::isEmpty);
                        }
                    }
                    """
                )
            );
    }
}
