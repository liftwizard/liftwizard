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

package io.liftwizard.rewrite.bestpractices;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class NullSafeEqualsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new NullSafeEqualsRecipes()).parser(JavaParser.fromJavaVersion());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        void test(String left, String right, Integer a, Integer b) {
                            boolean notEqualsPattern1 = left == null ? right != null : !left.equals(right);
                            boolean notEqualsPattern2 = right == null ? left != null : !right.equals(left);
                            boolean equalsPattern1 = left == null ? right == null : left.equals(right);
                            boolean equalsPattern2 = right == null ? left == null : right.equals(left);
                            boolean equalsPattern3 = left == null ? right == null : left == right || left.equals(right);
                            boolean equalsPattern4 = left == right || left != null && left.equals(right);
                            boolean equalsPattern5 = right == left || left != null && left.equals(right);
                            boolean equalsPattern6 = left == null || right == null ? left == right : left.equals(right);
                            boolean differentTypes = a == null ? b == null : a.equals(b);
                        }
                    }
                    """,
                    """
                    import java.util.Objects;

                    class Test {
                        void test(String left, String right, Integer a, Integer b) {
                            boolean notEqualsPattern1 = !Objects.equals(left, right);
                            boolean notEqualsPattern2 = !Objects.equals(right, left);
                            boolean equalsPattern1 = Objects.equals(left, right);
                            boolean equalsPattern2 = Objects.equals(right, left);
                            boolean equalsPattern3 = Objects.equals(left, right);
                            boolean equalsPattern4 = Objects.equals(left, right);
                            boolean equalsPattern5 = Objects.equals(left, right);
                            boolean equalsPattern6 = Objects.equals(left, right);
                            boolean differentTypes = Objects.equals(a, b);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        void test(String left, String right, String other, MyClass obj) {
                            boolean simpleEquals = left.equals(right);
                            boolean simpleNullCheck = left == null;
                            boolean differentTernary = left == null ? false : left.equals(right);
                            boolean variablesMismatch = left == null ? right == null : left.equals(other);
                            boolean notEqualsMethod = left == null ? right == null : left.compareTo(right) == 0;
                            boolean multipleArgs = obj == null ? right == null : obj.equals(right, true);
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
