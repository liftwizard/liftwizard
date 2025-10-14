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
        spec.recipe(new ECUtilityNegatedEmpty()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean negatedIterateIsEmpty(List<String> list) {
                            return !Iterate.isEmpty(list);
                        }

                        boolean negatedIterateNotEmpty(List<String> list) {
                            return !Iterate.notEmpty(list);
                        }

                        boolean negatedMapIterateIsEmpty(Map<String, Integer> map) {
                            return !MapIterate.isEmpty(map);
                        }

                        boolean negatedMapIterateNotEmpty(Map<String, Integer> map) {
                            return !MapIterate.notEmpty(map);
                        }

                        boolean negatedArrayIterateIsEmpty(String[] array) {
                            return !ArrayIterate.isEmpty(array);
                        }

                        boolean negatedArrayIterateNotEmpty(String[] array) {
                            return !ArrayIterate.notEmpty(array);
                        }

                        boolean allThreeUtilityClasses(List<String> list, Map<String, Integer> map, String[] array) {
                            return !Iterate.isEmpty(list) && !MapIterate.notEmpty(map) && !ArrayIterate.isEmpty(array);
                        }

                        boolean parenthesizedExpressions(List<String> list, Map<String, Integer> map, String[] array) {
                            return !(Iterate.isEmpty(list)) || !(MapIterate.notEmpty(map)) || !(ArrayIterate.isEmpty(array));
                        }

                        boolean iterateWithIterables(Iterable<String> iterable) {
                            return !Iterate.isEmpty(iterable);
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import java.util.Map;
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.impl.utility.MapIterate;
                    import org.eclipse.collections.impl.utility.ArrayIterate;

                    class Test {
                        boolean negatedIterateIsEmpty(List<String> list) {
                            return Iterate.notEmpty(list);
                        }

                        boolean negatedIterateNotEmpty(List<String> list) {
                            return Iterate.isEmpty(list);
                        }

                        boolean negatedMapIterateIsEmpty(Map<String, Integer> map) {
                            return MapIterate.notEmpty(map);
                        }

                        boolean negatedMapIterateNotEmpty(Map<String, Integer> map) {
                            return MapIterate.isEmpty(map);
                        }

                        boolean negatedArrayIterateIsEmpty(String[] array) {
                            return ArrayIterate.notEmpty(array);
                        }

                        boolean negatedArrayIterateNotEmpty(String[] array) {
                            return ArrayIterate.isEmpty(array);
                        }

                        boolean allThreeUtilityClasses(List<String> list, Map<String, Integer> map, String[] array) {
                            return Iterate.notEmpty(list) && MapIterate.isEmpty(map) && ArrayIterate.notEmpty(array);
                        }

                        boolean parenthesizedExpressions(List<String> list, Map<String, Integer> map, String[] array) {
                            return Iterate.notEmpty(list) || MapIterate.isEmpty(map) || ArrayIterate.notEmpty(array);
                        }

                        boolean iterateWithIterables(Iterable<String> iterable) {
                            return Iterate.notEmpty(iterable);
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
}
