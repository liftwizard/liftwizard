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

class ArrayIterateToNotEmptyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ArrayIterateToNotEmpty()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacesArrayNullAndLengthCheckWithNotEmpty() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array != null && array.length > 0;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return ArrayIterate.notEmpty(array);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesIntArrayNullAndLengthCheckWithNotEmpty() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(int[] numbers) {\n" +
                "        return numbers != null && numbers.length > 0;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(int[] numbers) {\n" +
                "        return ArrayIterate.notEmpty(numbers);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesComplexArrayNullAndLengthCheckWithNotEmpty() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array1, String[] array2) {\n" +
                "        return (array1 != null && array1.length > 0) || (array2 != null && array2.length > 0);\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(String[] array1, String[] array2) {\n" +
                "        return (ArrayIterate.notEmpty(array1)) || (ArrayIterate.notEmpty(array2));\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeArrayNullOrLengthEqualZero() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array == null || array.length == 0;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeArrayNullCheckOnly() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array != null;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeArrayLengthCheckOnly() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array.length > 0;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeWrongOrder() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array.length > 0 && array != null;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeGreaterThanOrEqualOne() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array != null && array.length >= 1;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void preservesWhitespaceAndComments() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        // Check if array has elements\n" +
                "        return array != null && array.length > 0;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        // Check if array has elements\n" +
                "        return ArrayIterate.notEmpty(array);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
