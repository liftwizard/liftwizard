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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class ECNullSafeEqualsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECNullSafeEquals())
            .parser(org.openrewrite.java.JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePattern1NotEquals() {
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
        rewriteRun(
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
