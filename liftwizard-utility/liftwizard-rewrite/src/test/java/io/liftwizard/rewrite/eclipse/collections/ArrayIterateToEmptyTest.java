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

class ArrayIterateToEmptyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ArrayIterateToEmpty()).parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacesArrayNullOrLengthZeroWithIsEmpty() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array == null || array.length == 0;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return ArrayIterate.isEmpty(array);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesIntArrayNullOrLengthZeroWithIsEmpty() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(int[] numbers) {\n" +
                "        return numbers == null || numbers.length == 0;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(int[] numbers) {\n" +
                "        return ArrayIterate.isEmpty(numbers);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesComplexArrayNullOrLengthZeroWithIsEmpty() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array1, String[] array2) {\n" +
                "        return (array1 == null || array1.length == 0) && (array2 == null || array2.length == 0);\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(String[] array1, String[] array2) {\n" +
                "        return (ArrayIterate.isEmpty(array1)) && (ArrayIterate.isEmpty(array2));\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeArrayNullAndLengthGreaterThanZero() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array != null && array.length > 0;\n" +
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
                "        return array == null;\n" +
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
                "        return array.length == 0;\n" +
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
                "        return array.length == 0 || array == null;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeLengthLessThanOrEqualZero() {
        rewriteRun(
            java(
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        return array == null || array.length <= 0;\n" +
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
                "        // Check if array is empty\n" +
                "        return array == null || array.length == 0;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(String[] array) {\n" +
                "        // Check if array is empty\n" +
                "        return ArrayIterate.isEmpty(array);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
