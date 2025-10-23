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

class IterateEmptyTest implements RewriteTest {

    @Override
    public void defaults(final RecipeSpec spec) {
        spec
            .recipe(new IterateEmptyRecipes())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        boolean testIsEmpty(List<String> list) {
                            return list == null || list.isEmpty();
                        }

                        boolean testNotEmpty(Set<Integer> set) {
                            return set != null && !set.isEmpty();
                        }

                        void testMultiple(List<String> strings, Set<Object> objects) {
                            if (strings == null || strings.isEmpty()) {
                            }

                            if (objects != null && !objects.isEmpty()) {
                            }
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;

                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        boolean testIsEmpty(List<String> list) {
                            return Iterate.isEmpty(list);
                        }

                        boolean testNotEmpty(Set<Integer> set) {
                            return Iterate.notEmpty(set);
                        }

                        void testMultiple(List<String> strings, Set<Object> objects) {
                            if (Iterate.isEmpty(strings)) {
                            }

                            if (Iterate.notEmpty(objects)) {
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
                    import java.util.List;

                    class Test {
                        void test(List<String> list) {
                            boolean simpleNullCheck = list == null;
                            boolean simpleIsEmptyCheck = list.isEmpty();
                            boolean wrongOperator = list != null || !list.isEmpty();
                        }
                    }
                    """
                )
            );
    }
}
