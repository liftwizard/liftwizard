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

class ECSimplifyNegatedEmptyChecksTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECSimplifyNegatedEmptyChecks())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        boolean testNegatedIsEmpty(MutableList<String> list) {
                            return !list.isEmpty();
                        }

                        boolean testNegatedNotEmpty(MutableList<String> list) {
                            return !list.notEmpty();
                        }

                        void testInIfStatement(MutableList<String> list) {
                            if (!list.isEmpty()) {
                                doWork();
                            }
                        }

                        void doWork() {}
                    }

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean notEmpty() {
                            return !this.delegate.isEmpty();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        boolean testNegatedIsEmpty(MutableList<String> list) {
                            return list.notEmpty();
                        }

                        boolean testNegatedNotEmpty(MutableList<String> list) {
                            return list.isEmpty();
                        }

                        void testInIfStatement(MutableList<String> list) {
                            if (list.notEmpty()) {
                                doWork();
                            }
                        }

                        void doWork() {}
                    }

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean notEmpty() {
                            return this.delegate.notEmpty();
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
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        boolean nonNegatedCalls(MutableList<String> list) {
                            return list.isEmpty() || list.notEmpty();
                        }
                    }

                    class A implements MutableList<String> {
                        private MutableList<String> delegate = FastList.newList();

                        @Override
                        public boolean notEmpty() {
                            return !this.isEmpty();
                        }

                        @Override
                        public boolean isEmpty() {
                            return !this.notEmpty();
                        }
                    }
                    """
                )
            );
    }
}
