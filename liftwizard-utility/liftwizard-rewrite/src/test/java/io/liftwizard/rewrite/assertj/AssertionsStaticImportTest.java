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

import static org.openrewrite.java.Assertions.java;

class AssertionsStaticImportTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AssertionsStaticImport()).parser(JavaParser.fromJavaVersion().classpath("assertj-core"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.assertj.core.api.Assertions;
                    import java.util.List;
                    import java.util.ArrayList;
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            Assertions.assertThat(list).isEmpty();
                            Assertions.assertThat(list).isNotEmpty();
                            Assertions.assertThat(list).hasSize(0);
                            Assertions.assertThat("text").isEqualTo("text");
                            Assertions.assertThat(42).isGreaterThan(0);
                            Assertions.assertThat(true).isTrue();

                            Assertions.assertThatThrownBy(() -> {
                                throw new IllegalArgumentException("error");
                            }).isInstanceOf(IllegalArgumentException.class);

                            Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                                .isThrownBy(() -> {
                                    throw new IllegalArgumentException("error");
                                });

                            Map<String, String> map = new HashMap<>();
                            Assertions.assertThat(map).containsKey("key");

                            Assertions.fail("Should not reach here");

                            Assertions.useDefaultDateFormatsOnly();
                        }
                    }
                    """,
                    """
                    import java.util.List;
                    import java.util.ArrayList;
                    import java.util.Map;
                    import java.util.HashMap;

                    import static org.assertj.core.api.Assertions.*;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            assertThat(list).isEmpty();
                            assertThat(list).isNotEmpty();
                            assertThat(list).hasSize(0);
                            assertThat("text").isEqualTo("text");
                            assertThat(42).isGreaterThan(0);
                            assertThat(true).isTrue();

                            assertThatThrownBy(() -> {
                                throw new IllegalArgumentException("error");
                            }).isInstanceOf(IllegalArgumentException.class);

                            assertThatExceptionOfType(IllegalArgumentException.class)
                                .isThrownBy(() -> {
                                    throw new IllegalArgumentException("error");
                                });

                            Map<String, String> map = new HashMap<>();
                            assertThat(map).containsKey("key");

                            fail("Should not reach here");

                            useDefaultDateFormatsOnly();
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
                    import java.util.ArrayList;

                    import static org.assertj.core.api.Assertions.assertThat;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            assertThat(list).isEmpty();
                        }
                    }
                    """
                )
            );
    }
}
