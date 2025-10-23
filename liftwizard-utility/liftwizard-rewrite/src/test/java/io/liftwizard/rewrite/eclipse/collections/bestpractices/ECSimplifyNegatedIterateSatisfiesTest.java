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
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        void test(List<String> list, Predicate<String> predicate1, Predicate<String> predicate2) {
                            boolean negatedNoneSatisfy = !Iterate.noneSatisfy(list, predicate1);
                            boolean negatedAnySatisfy = !Iterate.anySatisfy(list, predicate2);
                            boolean negatedWithParentheses = !(Iterate.noneSatisfy(list, predicate1));
                            boolean negatedWithLambda = !Iterate.noneSatisfy(list, str -> str.length() > 5);
                            boolean negatedWithMethodReference = !Iterate.anySatisfy(list, String::isEmpty);

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
                            boolean negatedNoneSatisfy = Iterate.anySatisfy(list, predicate1);
                            boolean negatedAnySatisfy = Iterate.noneSatisfy(list, predicate2);
                            boolean negatedWithParentheses = Iterate.anySatisfy(list, predicate1);
                            boolean negatedWithLambda = Iterate.anySatisfy(list, str -> str.length() > 5);
                            boolean negatedWithMethodReference = Iterate.noneSatisfy(list, String::isEmpty);

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
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.utility.Iterate;
                    import org.eclipse.collections.api.block.predicate.Predicate;
                    import java.util.List;

                    class Test {
                        void test(List<String> list, Predicate<String> predicate) {
                            boolean nonNegatedNoneSatisfy = Iterate.noneSatisfy(list, predicate);
                            boolean nonNegatedAnySatisfy = Iterate.anySatisfy(list, predicate);
                            boolean doubleNegation = !!Iterate.noneSatisfy(list, predicate);
                        }
                    }
                    """
                )
            );
    }
}
