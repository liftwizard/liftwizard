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

class ECNullSafeEqualsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ECNullSafeEquals()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePattern1NotEquals() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == null ? right != null : !left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(String left, String right) {
                            return !Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacePattern2Equals() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == null ? right == null : left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacePattern3ReferenceOrEquals() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == null ? right == null : left == right || left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacePattern4ShortCircuitOr() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == right || left != null && left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacePattern5ShortCircuitOrReversed() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return right == left || left != null && left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacePattern6EitherNull() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == null || right == null ? left == right : left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceSimpleEquals() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left.equals(right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceSimpleNullCheck() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left) {
                            return left == null;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceDifferentTernaryPattern() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == null ? false : left.equals(right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceNestedInIfStatement() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        void test(String left, String right) {
                            if (left == null ? right != null : !left.equals(right)) {
                                // Not equal
                            }
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        void test(String left, String right) {
                            if (!Comparators.nullSafeEquals(left, right)) {
                                // Not equal
                            }
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithDifferentTypes() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(Integer left, Integer right) {
                            return left == null ? right == null : left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test(Integer left, Integer right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleInSameClass() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test1(String left, String right) {
                            return left == null ? right != null : !left.equals(right);
                        }

                        boolean test2(String left, String right) {
                            return left == null ? right == null : left.equals(right);
                        }

                        boolean test3(String left, String right) {
                            return left == right || left != null && left.equals(right);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.factory.Comparators;

                    class Test {
                        boolean test1(String left, String right) {
                            return !Comparators.nullSafeEquals(left, right);
                        }

                        boolean test2(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }

                        boolean test3(String left, String right) {
                            return Comparators.nullSafeEquals(left, right);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenVariablesDontMatch() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right, String other) {
                            return left == null ? right == null : left.equals(other);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenNotEqualsMethod() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(String left, String right) {
                            return left == null ? right == null : left.compareTo(right) == 0;
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceWhenEqualsHasMultipleArguments() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        boolean test(MyClass left, String right) {
                            return left == null ? right == null : left.equals(right, true);
                        }

                        class MyClass {
                            boolean equals(String other, boolean ignoreCase) {
                                return false;
                            }
                        }
                    }
                    """
                )
            );
    }
}
