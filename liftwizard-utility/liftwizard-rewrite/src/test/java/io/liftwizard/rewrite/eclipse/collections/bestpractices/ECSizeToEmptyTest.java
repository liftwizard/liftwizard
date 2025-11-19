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

class ECSizeToEmptyTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec.recipe(new ECSizeToEmptyRecipes());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test(MutableList<String> list) {
                            boolean sizeEqualsZero = list.size() == 0;
                            boolean sizeGreaterThanZero = list.size() > 0;
                            boolean sizeNotEqualsZero = list.size() != 0;
                            boolean sizeGreaterThanOrEqualOne = list.size() >= 1;
                            boolean sizeLessThanOne = list.size() < 1;
                            boolean sizeLessThanOrEqualZero = list.size() <= 0;
                            boolean reversedOneGreaterThanSize = 1 > list.size();
                            boolean reversedZeroGreaterThanOrEqualSize = 0 >= list.size();
                            boolean reversedZeroEqualsSize = 0 == list.size();

                            if (list.size() == 0) {
                                doWork();
                            }
                        }

                        void doWork() {}
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test(MutableList<String> list) {
                            boolean sizeEqualsZero = list.isEmpty();
                            boolean sizeGreaterThanZero = list.notEmpty();
                            boolean sizeNotEqualsZero = list.notEmpty();
                            boolean sizeGreaterThanOrEqualOne = list.notEmpty();
                            boolean sizeLessThanOne = list.isEmpty();
                            boolean sizeLessThanOrEqualZero = list.isEmpty();
                            boolean reversedOneGreaterThanSize = list.isEmpty();
                            boolean reversedZeroGreaterThanOrEqualSize = list.isEmpty();
                            boolean reversedZeroEqualsSize = list.isEmpty();

                            if (list.isEmpty()) {
                                doWork();
                            }
                        }

                        void doWork() {}
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
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() >= 2;
                        }
                    }
                    """
                )
            );
    }
}
