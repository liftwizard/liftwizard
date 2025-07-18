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

class VerifyAssertNotEmptyToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertNotEmptyToAssertJ())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                    "package org.eclipse.collections.impl.test;" +
                    "public class Verify {" +
                    "    public static void assertNotEmpty(String message, Object collection) {}" +
                    "    public static void assertNotEmpty(Object collection) {}" +
                    "}",
                    "package org.eclipse.collections.api.list;" +
                    "public interface MutableList<T> extends java.lang.Iterable<T> {}" +
                    "",
                    "package org.assertj.core.api;" +
                    "public class Assertions {" +
                    "    public static ObjectAssert assertThat(Object actual) { return new ObjectAssert(); }" +
                    "}",
                    "package org.assertj.core.api;" +
                    "public class ObjectAssert {" +
                    "    public ObjectAssert as(String description) { return this; }" +
                    "    public ObjectAssert isNotEmpty() { return this; }" +
                    "}"
                )
            );
    }

    @Test
    @DocumentExample
    void replacesVerifyAssertNotEmptyWithAssertJ() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        Verify.assertNotEmpty(\"List should not be empty\", list);\n" +
                "    }\n" +
                "}\n",
                "import org.assertj.core.api.Assertions;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        Assertions.assertThat(list).as(\"List should not be empty\").isNotEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesVerifyAssertNotEmptyWithCollection() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(MutableList<String> list) {\n" +
                "        Verify.assertNotEmpty(\"MutableList should not be empty\", list);\n" +
                "    }\n" +
                "}\n",
                "import org.assertj.core.api.Assertions;\n" +
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(MutableList<String> list) {\n" +
                "        Assertions.assertThat(list).as(\"MutableList should not be empty\").isNotEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesVerifyAssertNotEmptyWithVariableMessage() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        String message = \"Collection should not be empty\";\n" +
                "        Verify.assertNotEmpty(message, list);\n" +
                "    }\n" +
                "}\n",
                "import org.assertj.core.api.Assertions;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        String message = \"Collection should not be empty\";\n" +
                "        Assertions.assertThat(list).as(message).isNotEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesMultipleAssertNotEmptyInSameMethod() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list1, List<String> list2) {\n" +
                "        Verify.assertNotEmpty(\"First list should not be empty\", list1);\n" +
                "        Verify.assertNotEmpty(\"Second list should not be empty\", list2);\n" +
                "    }\n" +
                "}\n",
                "import org.assertj.core.api.Assertions;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list1, List<String> list2) {\n" +
                "        Assertions.assertThat(list1).as(\"First list should not be empty\").isNotEmpty();\n" +
                "        Assertions.assertThat(list2).as(\"Second list should not be empty\").isNotEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeOtherVerifyMethods() {
        rewriteRun(
            spec ->
                spec.parser(
                    JavaParser.fromJavaVersion().dependsOn(
                        "package org.eclipse.collections.impl.test;" +
                        "public class Verify {" +
                        "    public static void assertNotEmpty(String message, Object collection) {}" +
                        "    public static void assertEmpty(String message, Object collection) {}" +
                        "    public static void assertNotNull(String message, Object object) {}" +
                        "}"
                    )
                ),
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        Verify.assertEmpty(\"List should be empty\", list);\n" +
                "        Verify.assertNotNull(\"List should not be null\", list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeAssertNotEmptyWithWrongArity() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        Verify.assertNotEmpty(list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void preservesWhitespaceAndComments() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        // Verify the list is not empty\n" +
                "        Verify.assertNotEmpty(\"List should not be empty\", list);\n" +
                "    }\n" +
                "}\n",
                "import org.assertj.core.api.Assertions;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(List<String> list) {\n" +
                "        // Verify the list is not empty\n" +
                "        Assertions.assertThat(list).as(\"List should not be empty\").isNotEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
