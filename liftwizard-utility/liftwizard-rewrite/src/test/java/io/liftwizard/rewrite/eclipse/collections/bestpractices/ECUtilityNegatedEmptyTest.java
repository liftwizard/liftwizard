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

class ECUtilityNegatedEmptyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECUtilityNegatedEmpty())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api").classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacesNegatedIterateIsEmptyWithNotEmpty() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.impl.utility.Iterate;

                    class Test {
                        boolean test(List<String> list) {
                            return !Iterate.isEmpty(list);
                        }
                    }""",
                    """
                    import java.util.List;
                    import org.eclipse.collections.impl.utility.Iterate;

                    class Test {
                        boolean test(List<String> list) {
                            return Iterate.notEmpty(list);
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedIterateNotEmptyWithIsEmpty() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import org.eclipse.collections.impl.utility.Iterate;

                    class Test {
                        boolean test(List<String> list) {
                            return !Iterate.notEmpty(list);
                        }
                    }""",
                    """
                    import java.util.List;
                    import org.eclipse.collections.impl.utility.Iterate;

                    class Test {
                        boolean test(List<String> list) {
                            return Iterate.isEmpty(list);
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedMapIterateIsEmptyWithNotEmpty() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.MapIterate;

                    class Test {
                        boolean test(Map<String, Integer> map) {
                            return !MapIterate.isEmpty(map);
                        }
                    }""",
                    """
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.MapIterate;

                    class Test {
                        boolean test(Map<String, Integer> map) {
                            return MapIterate.notEmpty(map);
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedMapIterateNotEmptyWithIsEmpty() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.MapIterate;

                    class Test {
                        boolean test(Map<String, Integer> map) {
                            return !MapIterate.notEmpty(map);
                        }
                    }""",
                    """
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.MapIterate;

                    class Test {
                        boolean test(Map<String, Integer> map) {
                            return MapIterate.isEmpty(map);
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedArrayIterateIsEmptyWithNotEmpty() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(String[] array) {
                            return !ArrayIterate.isEmpty(array);
                        }
                    }""",
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(String[] array) {
                            return ArrayIterate.notEmpty(array);
                        }
                    }"""
                )
            );
    }

    @Test
    void replacesNegatedArrayIterateNotEmptyWithIsEmpty() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(String[] array) {
                            return !ArrayIterate.notEmpty(array);
                        }
                    }""",
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(String[] array) {
                            return ArrayIterate.isEmpty(array);
                        }
                    }"""
                )
            );
    }

    @Test
    void handlesAllThreeUtilityClassesInSameFile() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(List<String> list, Map<String, Integer> map, String[] array) {
                            return !Iterate.isEmpty(list) && !MapIterate.notEmpty(map) && !ArrayIterate.isEmpty(array);
                        }
                    }""",
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(List<String> list, Map<String, Integer> map, String[] array) {
                            return Iterate.notEmpty(list) && MapIterate.isEmpty(map) && ArrayIterate.notEmpty(array);
                        }
                    }"""
                )
            );
    }

    @Test
    void handlesParenthesizedExpressions() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(List<String> list, Map<String, Integer> map, String[] array) {
                            return !(Iterate.isEmpty(list)) || !(MapIterate.notEmpty(map)) || !(ArrayIterate.isEmpty(array));
                        }
                    }""",
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(List<String> list, Map<String, Integer> map, String[] array) {
                            return Iterate.notEmpty(list) || MapIterate.isEmpty(map) || ArrayIterate.notEmpty(array);
                        }
                    }"""
                )
            );
    }

    @Test
    void doesNotChangeNonNegatedCalls() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(List<String> list, Map<String, Integer> map, String[] array) {
                            return Iterate.isEmpty(list) || MapIterate.notEmpty(map) || ArrayIterate.isEmpty(array);
                        }
                    }"""
                )
            );
    }

    @Test
    void handlesIterateWithIterables() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;

                    class Test {
                        boolean test(Iterable<String> iterable) {
                            return !Iterate.isEmpty(iterable);
                        }
                    }""",
                    """
                    import org.eclipse.collections.impl.utility.Iterate;

                    class Test {
                        boolean test(Iterable<String> iterable) {
                            return Iterate.notEmpty(iterable);
                        }
                    }"""
                )
            );
    }

    @Test
    void handlesArrayIterateWithPrimitiveArrays() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion()
                            .classpath("eclipse-collections-api", "eclipse-collections")
                            .dependsOn(
                                """
                                package org.eclipse.collections.impl.utility;
                                public class ArrayIterate {
                                    public static boolean isEmpty(int[] array) { return true; }
                                    public static boolean isEmpty(double[] array) { return true; }
                                    public static boolean notEmpty(int[] array) { return true; }
                                    public static boolean notEmpty(double[] array) { return true; }
                                }
                                """
                            )
                    ),
                java(
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(int[] intArray, double[] doubleArray) {
                            return !ArrayIterate.isEmpty(intArray) && !ArrayIterate.notEmpty(doubleArray);
                        }
                    }""",
                    """
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean test(int[] intArray, double[] doubleArray) {
                            return ArrayIterate.notEmpty(intArray) && ArrayIterate.isEmpty(doubleArray);
                        }
                    }"""
                )
            );
    }
}
