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

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class VerifyArrayAssertSizeToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyArrayAssertSizeToAssertJ())
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.test;
                        public class Verify {
                            public static void assertSize(String message, int expectedSize, Object[] array) {}
                            public static void assertSize(String message, int expectedSize, int[] array) {}
                            public static void assertSize(String message, int expectedSize, String[] array) {}
                            public static void assertSize(String message, int expectedSize, java.util.Collection<?> collection) {}
                            public static void assertNotEmpty(Object[] array) {}
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class Assertions {
                            public static ObjectArrayAssert assertThat(Object[] actual) { return new ObjectArrayAssert(); }
                            public static IntArrayAssert assertThat(int[] actual) { return new IntArrayAssert(); }
                            public static <T> ObjectArrayAssert<T> assertThat(T[] actual) { return new ObjectArrayAssert<>(); }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class ObjectArrayAssert<T> {
                            public ObjectArrayAssert<T> as(String description) { return this; }
                            public ObjectArrayAssert<T> hasSize(int expected) { return this; }
                        }
                        """,
                        """
                        package org.assertj.core.api;
                        public class IntArrayAssert {
                            public IntArrayAssert as(String description) { return this; }
                            public IntArrayAssert hasSize(int expected) { return this; }
                        }
                        """
                    )
                    .classpath("eclipse-collections", "assertj-core")
            )
            .typeValidationOptions(org.openrewrite.test.TypeValidation.none());
    }

    @Test
    @DocumentExample
    void transformsVerifyAssertSizeWithStringArray() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"a\", \"b\", \"c\"};\n" +
                "        Verify.assertSize(\"Array should have 3 elements\", 3, array);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"a\", \"b\", \"c\"};\n" +
                "        assertThat(array).as(\"Array should have 3 elements\").hasSize(3);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void transformsVerifyAssertSizeWithIntArray() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        int[] numbers = {1, 2, 3, 4, 5};\n" +
                "        Verify.assertSize(\"Expected 5 numbers\", 5, numbers);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        int[] numbers = {1, 2, 3, 4, 5};\n" +
                "        assertThat(numbers).as(\"Expected 5 numbers\").hasSize(5);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void transformsVerifyAssertSizeWithObjectArray() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Object[] objects = new Object[10];\n" +
                "        Verify.assertSize(\"Objects array\", 10, objects);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Object[] objects = new Object[10];\n" +
                "        assertThat(objects).as(\"Objects array\").hasSize(10);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void transformsVerifyAssertSizeWithEmptyArray() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] emptyArray = {};\n" +
                "        Verify.assertSize(\"Should be empty\", 0, emptyArray);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] emptyArray = {};\n" +
                "        assertThat(emptyArray).as(\"Should be empty\").hasSize(0);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void transformsVerifyAssertSizeWithVariableSize() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"hello\", \"world\"};\n" +
                "        int expectedSize = 2;\n" +
                "        Verify.assertSize(\"Array size check\", expectedSize, array);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"hello\", \"world\"};\n" +
                "        int expectedSize = 2;\n" +
                "        assertThat(array).as(\"Array size check\").hasSize(expectedSize);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void transformsVerifyAssertSizeWithMethodArguments() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(String[] inputArray, int size) {\n" +
                "        Verify.assertSize(\"Input validation\", size, inputArray);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(String[] inputArray, int size) {\n" +
                "        assertThat(inputArray).as(\"Input validation\").hasSize(size);\n" +
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
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"a\", \"b\"};\n" +
                "        // Verify array has correct size\n" +
                "        Verify.assertSize(\"Size check\", 2, array);\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"a\", \"b\"};\n" +
                "        // Verify array has correct size\n" +
                "        assertThat(array).as(\"Size check\").hasSize(2);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotTransformNonArrayAssertSize() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        List<String> list = java.util.Arrays.asList(\"a\", \"b\");\n" +
                "        Verify.assertSize(\"List check\", 2, list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotTransformOtherVerifyMethods() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        String[] array = {\"a\"};\n" +
                "        Verify.assertNotEmpty(array);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void handlesComplexArrayExpressions() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertSize(\"Array from method\", 3, getArray());\n" +
                "    }\n" +
                "    \n" +
                "    private String[] getArray() {\n" +
                "        return new String[]{\"x\", \"y\", \"z\"};\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThat(getArray()).as(\"Array from method\").hasSize(3);\n" +
                "    }\n" +
                "    \n" +
                "    private String[] getArray() {\n" +
                "        return new String[]{\"x\", \"y\", \"z\"};\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
