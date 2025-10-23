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

class VerifyAssertNotEmptyToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertNotEmptyToAssertJRecipes())
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.test;

                        import java.util.Map;

                        public final class Verify {
                            public static void assertNotEmpty(String message, Iterable<?> iterable) {}
                            public static void assertNotEmpty(Iterable<?> iterable) {}
                            public static void assertNotEmpty(String message, Map<?, ?> map) {}
                            public static void assertNotEmpty(Map<?, ?> map) {}
                            public static <T> void assertNotEmpty(String message, T[] array) {}
                            public static <T> void assertNotEmpty(T[] array) {}
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
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            Verify.assertNotEmpty("list should not be empty", list);
                            Verify.assertNotEmpty(list);

                            Map<String, Integer> map = new HashMap<>();
                            map.put("key", 1);
                            Verify.assertNotEmpty("map should not be empty", map);
                            Verify.assertNotEmpty(map);

                            String[] array = {"a", "b"};
                            Verify.assertNotEmpty("array should not be empty", array);
                            Verify.assertNotEmpty(array);
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
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            Assertions.assertThat(list).as("list should not be empty").isNotEmpty();
                            Assertions.assertThat(list).isNotEmpty();

                            Map<String, Integer> map = new HashMap<>();
                            map.put("key", 1);
                            Assertions.assertThat(map).as("map should not be empty").isNotEmpty();
                            Assertions.assertThat(map).isNotEmpty();

                            String[] array = {"a", "b"};
                            Assertions.assertThat(array).as("array should not be empty").isNotEmpty();
                            Assertions.assertThat(array).isNotEmpty();
                        }
                    }
                    """
                )
            );
    }
}
