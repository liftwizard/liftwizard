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

class ECSizeToEmptyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ECSizeToEmpty()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
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
                    import org.eclipse.collections.api.RichIterable;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        boolean test(MutableList<String> list) {
                            return list.size() >= 2;
                        }
                    }

                    class IsEmptyImplementation implements MutableList<String> {
                        @Override
                        public boolean isEmpty() {
                            return this.size() == 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }

                    class NotEmptyImplementation implements MutableList<String> {
                        @Override
                        public boolean notEmpty() {
                            return this.size() > 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }

                    class CircularIsEmpty implements RichIterable<String> {
                        @Override
                        public boolean isEmpty() {
                            return !this.notEmpty();
                        }

                        @Override
                        public boolean notEmpty() {
                            return this.size() > 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }

                    class CircularNotEmpty implements RichIterable<String> {
                        @Override
                        public boolean notEmpty() {
                            return !this.isEmpty();
                        }

                        @Override
                        public boolean isEmpty() {
                            return this.size() == 0;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }
                    }
                    """
                )
            );
    }
}
