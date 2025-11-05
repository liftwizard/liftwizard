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

package io.liftwizard.rewrite.eclipse.collections.adoption.unsafe;

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

class ECDetectToSatisfiesTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new ECDetectToSatisfiesRecipes());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMultiplePatterns(MutableList<String> list) {
                            boolean detectNotNull = list.detect(s -> s.length() > 5) != null;
                            boolean detectEqualsNull = list.detect(s -> s.length() > 5) == null;
                            boolean nullNotEqualsDetect = null != list.detect(s -> s.length() > 5);
                            boolean nullEqualsDetect = null == list.detect(s -> s.length() > 5);
                        }
                    }""",
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void testMultiplePatterns(MutableList<String> list) {
                            boolean detectNotNull = list.anySatisfy(s -> s.length() > 5);
                            boolean detectEqualsNull = list.noneSatisfy(s -> s.length() > 5);
                            boolean nullNotEqualsDetect = list.anySatisfy(s -> s.length() > 5);
                            boolean nullEqualsDetect = list.noneSatisfy(s -> s.length() > 5);
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
                    import org.eclipse.collections.api.list.primitive.IntList;

                    class Test {
                        String testOtherDetectCalls(MutableList<String> list) {
                            String result = list.detect(s -> s.length() > 5);
                            return result != null ? result : "default";
                        }

                        boolean testPrimitiveLists(IntList list) {
                            return list.detectIfNone(i -> i > 5, -1) != -1;
                        }
                    }"""
                )
            );
    }
}
