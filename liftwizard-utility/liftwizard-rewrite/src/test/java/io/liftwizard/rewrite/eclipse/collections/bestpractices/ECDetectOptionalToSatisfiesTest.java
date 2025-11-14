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

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ECDetectOptionalToSatisfiesTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new ECDetectOptionalToSatisfiesRecipes());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void example(MutableList<String> list) {
                            boolean detectOptionalIsPresent = list.detectOptional(s -> s.length() > 5).isPresent();
                            boolean negatedDetectOptionalIsPresent = !list.detectOptional(s -> s.length() > 5).isPresent();
                            boolean detectOptionalIsEmpty = list.detectOptional(s -> s.length() > 5).isEmpty();
                            boolean negatedDetectOptionalIsEmpty = !list.detectOptional(s -> s.length() > 5).isEmpty();
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void example(MutableList<String> list) {
                            boolean detectOptionalIsPresent = list.anySatisfy(s -> s.length() > 5);
                            boolean negatedDetectOptionalIsPresent = list.noneSatisfy(s -> s.length() > 5);
                            boolean detectOptionalIsEmpty = list.noneSatisfy(s -> s.length() > 5);
                            boolean negatedDetectOptionalIsEmpty = list.anySatisfy(s -> s.length() > 5);
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
