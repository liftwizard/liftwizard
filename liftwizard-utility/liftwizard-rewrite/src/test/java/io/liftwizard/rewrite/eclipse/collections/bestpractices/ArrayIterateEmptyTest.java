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

class ArrayIterateEmptyTest implements RewriteTest {

    @Override
    public void defaults(final RecipeSpec spec) {
        spec
            .recipe(new ArrayIterateEmptyRecipes())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    class Test {
                        void test(
                                String[] strings,
                                Integer[] integers,
                                Object[] objects) {
                            boolean isEmptyNullOrLength =
                                    strings == null || strings.length == 0;
                            boolean notEmptyNotNullAndLength =
                                    integers != null && integers.length > 0;

                            if (strings == null || strings.length == 0) {
                            }

                            if (objects != null && objects.length > 0) {
                            }
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        void test(
                                String[] strings,
                                Integer[] integers,
                                Object[] objects) {
                            boolean isEmptyNullOrLength =
                                    ArrayIterate.isEmpty(strings);
                            boolean notEmptyNotNullAndLength =
                                    ArrayIterate.notEmpty(integers);

                            if (ArrayIterate.isEmpty(strings)) {
                            }

                            if (ArrayIterate.notEmpty(objects)) {
                            }
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
                        void test(String[] array) {
                            boolean simpleNullCheck =
                                    array == null;
                            boolean simpleLengthCheck =
                                    array.length == 0;
                            boolean differentLength =
                                    array != null && array.length > 5;
                            boolean wrongOperator =
                                    array != null || array.length > 0;
                        }
                    }
                    """
                )
            );
    }
}
