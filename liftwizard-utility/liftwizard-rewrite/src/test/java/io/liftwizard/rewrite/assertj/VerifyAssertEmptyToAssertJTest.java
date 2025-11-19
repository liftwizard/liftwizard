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

class VerifyAssertEmptyToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertEmptyToAssertJRecipes())
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.test;

                        import java.util.Map;

                        public final class Verify {
                            public static void assertEmpty(String message, Iterable<?> iterable) {}
                            public static void assertEmpty(Iterable<?> iterable) {}
                            public static void assertEmpty(String message, Map<?, ?> map) {}
                            public static void assertEmpty(Map<?, ?> map) {}
                        }
                        """
                    )
                    .classpath("eclipse-collections-api", "eclipse-collections", "assertj-core")
            );
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.test.Verify;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.empty();
                            Verify.assertEmpty("list should be empty", list);
                            Verify.assertEmpty(list);

                            Map<String, Integer> map = new HashMap<>();
                            Verify.assertEmpty("map should be empty", map);
                            Verify.assertEmpty(map);
                        }
                    }
                    """,
                    """
                    import org.assertj.core.api.Assertions;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.factory.Lists;
                    import java.util.Map;
                    import java.util.HashMap;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.empty();
                            Assertions.assertThat(list).as("list should be empty").isEmpty();
                            Assertions.assertThat(list).isEmpty();

                            Map<String, Integer> map = new HashMap<>();
                            Assertions.assertThat(map).as("map should be empty").isEmpty();
                            Assertions.assertThat(map).isEmpty();
                        }
                    }
                    """
                )
            );
    }
}
