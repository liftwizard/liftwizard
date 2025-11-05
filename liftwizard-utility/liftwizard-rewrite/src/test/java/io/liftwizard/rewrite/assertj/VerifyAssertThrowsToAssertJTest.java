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

package io.liftwizard.rewrite.assertj;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class VerifyAssertThrowsToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertThrowsToAssertJ())
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.test;

                        import java.util.concurrent.Callable;

                        public final class Verify {
                            public static void assertThrows(Class<? extends Throwable> expectedExceptionClass, Runnable code) {}
                            public static void assertThrows(Class<? extends Throwable> expectedExceptionClass, Callable<?> code) {}
                        }
                        """
                    )
                    .classpath("assertj-core")
            );
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                spec ->
                    spec.typeValidationOptions(
                        TypeValidation.builder().identifiers(false).methodInvocations(false).build()
                    ),
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;

                    import java.util.concurrent.Callable;

                    class Test {
                        void test() {
                            Verify.assertThrows(IllegalArgumentException.class, () -> {
                                throw new IllegalArgumentException("error");
                            });

                            Verify.assertThrows(NullPointerException.class, () -> {
                                throw new NullPointerException();
                            });

                            Callable<Object> failingCallable = () -> {
                                throw new RuntimeException("error");
                            };
                            Verify.assertThrows(RuntimeException.class, failingCallable);
                        }
                    }
                    """,
                    """
                    import java.util.concurrent.Callable;

                    import static org.assertj.core.api.Assertions.assertThatThrownBy;

                    class Test {
                        void test() {
                            assertThatThrownBy(() -> {
                                throw new IllegalArgumentException("error");
                            }).isInstanceOf(IllegalArgumentException.class);

                            assertThatThrownBy(() -> {
                                throw new NullPointerException();
                            }).isInstanceOf(NullPointerException.class);

                            Callable<Object> failingCallable = () -> {
                                throw new RuntimeException("error");
                            };
                            assertThatThrownBy(failingCallable::call).isInstanceOf(RuntimeException.class);
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
                    import java.util.concurrent.Callable;

                    import static org.assertj.core.api.Assertions.assertThatThrownBy;

                    class Test {
                        void test() {
                            assertThatThrownBy(() -> {
                                throw new IllegalArgumentException("error");
                            }).isInstanceOf(IllegalArgumentException.class);
                        }
                    }
                    """
                )
            );
    }
}
