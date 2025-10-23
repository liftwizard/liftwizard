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

class VerifyAssertSizeToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertSizeToAssertJRecipes())
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.test;

                        import java.util.Map;

                        public final class Verify {
                            public static void assertSize(String message, int expectedSize, Iterable<?> iterable) {}
                            public static void assertSize(int expectedSize, Iterable<?> iterable) {}
                            public static void assertSize(String message, int expectedSize, Object[] array) {}
                            public static void assertSize(int expectedSize, Object[] array) {}
                            public static void assertSize(String mapName, int expectedSize, Map<?, ?> map) {}
                            public static void assertSize(int expectedSize, Map<?, ?> map) {}
                        }
                        """
                    )
                    .classpath("eclipse-collections-api", "eclipse-collections")
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
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                            Verify.assertSize("list should have size 3", 3, list);
                            Verify.assertSize(3, list);

                            String[] array = new String[]{"a", "b", "c"};
                            Verify.assertSize("array should have size 3", 3, array);
                            Verify.assertSize(3, array);

                            Integer[] numbers = new Integer[]{1, 2, 3, 4, 5};
                            Verify.assertSize("numbers should have expected size", 5, numbers);

                            Object[] objects = new Object[10];
                            Verify.assertSize(10, objects);

                            String[] emptyArray = new String[0];
                            Verify.assertSize("should be empty", 0, emptyArray);
                            Verify.assertSize(0, emptyArray);

                            MutableList<Integer> mutableNumbers = Lists.mutable.with(1, 2, 3, 4, 5);
                            Verify.assertSize("numbers should have expected size", 5, mutableNumbers);

                            MutableList<String> stringList = Lists.mutable.with("a", "b", "c");
                            Verify.assertSize(3, stringList);

                            MutableList<String> emptyList = Lists.mutable.with();
                            Verify.assertSize("should be empty", 0, emptyList);
                            Verify.assertSize(0, emptyList);

                            Map<String, Integer> map = new HashMap<>();
                            map.put("a", 1);
                            map.put("b", 2);
                            Verify.assertSize("map should have size 2", 2, map);
                            Verify.assertSize(2, map);

                            Map<String, String> emptyMap = new HashMap<>();
                            Verify.assertSize("map should be empty", 0, emptyMap);
                            Verify.assertSize(0, emptyMap);
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
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                            Assertions.assertThat(list).as("list should have size 3").hasSize(3);
                            Assertions.assertThat(list).hasSize(3);

                            String[] array = new String[]{"a", "b", "c"};
                            Assertions.assertThat(array).as("array should have size 3").hasSize(3);
                            Assertions.assertThat(array).hasSize(3);

                            Integer[] numbers = new Integer[]{1, 2, 3, 4, 5};
                            Assertions.assertThat(numbers).as("numbers should have expected size").hasSize(5);

                            Object[] objects = new Object[10];
                            Assertions.assertThat(objects).hasSize(10);

                            String[] emptyArray = new String[0];
                            Assertions.assertThat(emptyArray).as("should be empty").hasSize(0);
                            Assertions.assertThat(emptyArray).hasSize(0);

                            MutableList<Integer> mutableNumbers = Lists.mutable.with(1, 2, 3, 4, 5);
                            Assertions.assertThat(mutableNumbers).as("numbers should have expected size").hasSize(5);

                            MutableList<String> stringList = Lists.mutable.with("a", "b", "c");
                            Assertions.assertThat(stringList).hasSize(3);

                            MutableList<String> emptyList = Lists.mutable.with();
                            Assertions.assertThat(emptyList).as("should be empty").hasSize(0);
                            Assertions.assertThat(emptyList).hasSize(0);

                            Map<String, Integer> map = new HashMap<>();
                            map.put("a", 1);
                            map.put("b", 2);
                            Assertions.assertThat(map).as("map should have size 2").hasSize(2);
                            Assertions.assertThat(map).hasSize(2);

                            Map<String, String> emptyMap = new HashMap<>();
                            Assertions.assertThat(emptyMap).as("map should be empty").hasSize(0);
                            Assertions.assertThat(emptyMap).hasSize(0);
                        }
                    }
                    """
                )
            );
    }
}
