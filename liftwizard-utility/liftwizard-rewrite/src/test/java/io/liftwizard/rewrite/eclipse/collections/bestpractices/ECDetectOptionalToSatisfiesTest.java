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

class ECDetectOptionalToSatisfiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECDetectOptionalToSatisfies())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean testDetectOptionalIsPresentWithAnySatisfy(MutableList<String> list) {
                            return list.detectOptional(s -> s.length() > 5).isPresent();
                        }

                        boolean testNegatedDetectOptionalIsPresentWithNoneSatisfy(MutableList<String> list) {
                            return !list.detectOptional(s -> s.length() > 5).isPresent();
                        }

                        boolean testDetectOptionalIsEmptyWithNoneSatisfy(MutableList<String> list) {
                            return list.detectOptional(s -> s.length() > 5).isEmpty();
                        }

                        boolean testNegatedDetectOptionalIsEmptyWithAnySatisfy(MutableList<String> list) {
                            return !list.detectOptional(s -> s.length() > 5).isEmpty();
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean testDetectOptionalIsPresentWithAnySatisfy(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5);
                        }

                        boolean testNegatedDetectOptionalIsPresentWithNoneSatisfy(MutableList<String> list) {
                            return list.noneSatisfy(s -> s.length() > 5);
                        }

                        boolean testDetectOptionalIsEmptyWithNoneSatisfy(MutableList<String> list) {
                            return list.noneSatisfy(s -> s.length() > 5);
                        }

                        boolean testNegatedDetectOptionalIsEmptyWithAnySatisfy(MutableList<String> list) {
                            return list.anySatisfy(s -> s.length() > 5);
                        }
                    }"""
                )
            );
    }

    @Test
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        String testOtherOptionalCalls(MutableList<String> list) {
                            return list.detectOptional(s -> s.length() > 5).orElse("default");
                        }
                    }"""
                )
            );
    }
}
