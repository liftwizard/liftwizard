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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class IterateGetFirstTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new IterateGetFirstRecipes())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        void testMultiplePatterns(List<String> list, ArrayList<Integer> numbers, Set<Object> set) {
                            String listFirst = list.iterator().next();
                            Integer arrayListFirst = numbers.listIterator().next();
                            Object setFirst = set.iterator().next();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.utility.Iterate;

                    import java.util.ArrayList;
                    import java.util.List;
                    import java.util.Set;

                    class Test {
                        void testMultiplePatterns(List<String> list, ArrayList<Integer> numbers, Set<Object> set) {
                            String listFirst = Iterate.getFirst(list);
                            Integer arrayListFirst = Iterate.getFirst(numbers);
                            Object setFirst = Iterate.getFirst(set);
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
                    import java.util.Iterator;
                    import java.util.List;
                    import java.util.ListIterator;

                    class Test {
                        void test(List<String> list) {
                            Iterator<String> iter = list.iterator();
                            String first = iter.next();

                            ListIterator<String> listIter = list.listIterator();
                            String second = listIter.next();

                            iter.hasNext();
                            listIter.hasPrevious();
                        }
                    }
                    """
                )
            );
    }
}
