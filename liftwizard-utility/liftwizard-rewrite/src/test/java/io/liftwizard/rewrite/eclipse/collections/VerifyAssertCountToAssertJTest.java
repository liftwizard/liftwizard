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

package io.liftwizard.rewrite.eclipse.collections;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class VerifyAssertCountToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertCountToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                    "package org.eclipse.collections.impl.test;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "public class Verify {\n" +
                    "    public static <T> void assertCount(int expectedCount, Iterable<T> iterable, Predicate<? super T> predicate) {}\n" +
                    "    public static <T> void assertCount(int expectedCount, Iterable<T> iterable) {}\n" +
                    "    public static <T> void assertNotEmpty(Iterable<T> iterable) {}\n" +
                    "    public static <T> void assertSize(int expectedSize, Iterable<T> iterable) {}\n" +
                    "}\n",
                    "package org.eclipse.collections.api.block.predicate;\n" +
                    "public interface Predicate<T> {\n" +
                    "    boolean accept(T item);\n" +
                    "}\n",
                    "package org.eclipse.collections.impl.utility;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "public class Iterate {\n" +
                    "    public static <T> int count(Iterable<T> iterable, Predicate<? super T> predicate) { return 0; }\n" +
                    "}\n",
                    "package org.assertj.core.api;\n" +
                    "public class Assertions {\n" +
                    "    public static IntegerAssert assertThat(int actual) { return null; }\n" +
                    "}\n",
                    "package org.assertj.core.api;\n" +
                    "public class IntegerAssert {\n" +
                    "    public IntegerAssert isEqualTo(int expected) { return this; }\n" +
                    "}\n"
                )
            );
    }

    @Test
    @DocumentExample
    void replacesAssertCountWithLambdaPredicate() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        List<String> list = List.of(\"a\", \"b\", \"c\");\n" +
                "        Verify.assertCount(2, list, each -> each.length() > 0);\n" +
                "    }\n" +
                "}\n",
                """
                import org.assertj.core.api.Assertions;
                import org.eclipse.collections.impl.utility.Iterate;

                import java.util.List;

                class Test {
                    void test() {
                        List<String> list = List.of("a", "b", "c");
                        Assertions.assertThat(Iterate.count(list, each -> each.length() > 0)).isEqualTo(2);
                    }
                }
                """
            )
        );
    }

    @Test
    void replacesAssertCountWithMethodReference() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        List<String> list = List.of(\"a\", \"b\", \"c\");\n" +
                "        Verify.assertCount(0, list, String::isEmpty);\n" +
                "    }\n" +
                "}\n",
                """
                import org.assertj.core.api.Assertions;
                import org.eclipse.collections.impl.utility.Iterate;

                import java.util.List;

                class Test {
                    void test() {
                        List<String> list = List.of("a", "b", "c");
                        Assertions.assertThat(Iterate.count(list, String::isEmpty)).isEqualTo(0);
                    }
                }
                """
            )
        );
    }

    @Test
    void replacesAssertCountWithVariableExpectedCount() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        List<String> list = List.of(\"a\", \"b\", \"c\");\n" +
                "        int expectedCount = 2;\n" +
                "        Verify.assertCount(expectedCount, list, each -> each.length() > 0);\n" +
                "    }\n" +
                "}\n",
                """
                import org.assertj.core.api.Assertions;
                import org.eclipse.collections.impl.utility.Iterate;

                import java.util.List;

                class Test {
                    void test() {
                        List<String> list = List.of("a", "b", "c");
                        int expectedCount = 2;
                        Assertions.assertThat(Iterate.count(list, each -> each.length() > 0)).isEqualTo(expectedCount);
                    }
                }
                """
            )
        );
    }

    @Test
    void doesNotChangeOtherVerifyMethods() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        List<String> list = List.of(\"a\", \"b\", \"c\");\n" +
                "        Verify.assertNotEmpty(list);\n" +
                "        Verify.assertSize(3, list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeAssertCountWithWrongNumberOfArguments() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        List<String> list = List.of(\"a\", \"b\", \"c\");\n" +
                "        Verify.assertCount(3, list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
