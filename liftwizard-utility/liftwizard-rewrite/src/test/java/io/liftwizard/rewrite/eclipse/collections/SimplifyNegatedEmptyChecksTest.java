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

class SimplifyNegatedEmptyChecksTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new SimplifyNegatedEmptyChecks())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"));
    }

    @Test
    @DocumentExample
    void replacesNegatedIsEmptyWithNotEmptyOnRichIterable() {
        this.rewriteRun(
            spec ->
                spec.parser(
                    JavaParser.fromJavaVersion().dependsOn(
                        "package org.eclipse.collections.api.list;\n" +
                        "public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {\n" +
                        "    boolean isEmpty();\n" +
                        "    boolean notEmpty();\n" +
                        "}\n",
                        "package org.eclipse.collections.api;\n" +
                        "public interface RichIterable<T> {\n" +
                        "    boolean isEmpty();\n" +
                        "    boolean notEmpty();\n" +
                        "}\n"
                    )
                ),
            java(
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        return !list.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        return list.notEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesNegatedIsEmptyWithNotEmptyOnPrimitiveIterable() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.list.primitive.MutableIntList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableIntList list) {\n" +
                "        return !list.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.list.primitive.MutableIntList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableIntList list) {\n" +
                "        return list.notEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesNegatedIsEmptyWithNotEmptyOnMultimap() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.multimap.MutableMultimap;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableMultimap<String, String> multimap) {\n" +
                "        return !multimap.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.multimap.MutableMultimap;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableMultimap<String, String> multimap) {\n" +
                "        return multimap.notEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesNegatedNotEmptyWithIsEmpty() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        return !list.notEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        return list.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeNonNegatedIsEmpty() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        return list.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeNonNegatedNotEmpty() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        return list.notEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeJavaUtilCollections() {
        this.rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return !list.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void handlesComplexExpressions() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list1, MutableList<String> list2) {\n" +
                "        return !list1.isEmpty() && !list2.notEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list1, MutableList<String> list2) {\n" +
                "        return list1.notEmpty() && list2.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void preservesWhitespaceAndComments() {
        this.rewriteRun(
            java(
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        // Check if list is not empty\n" +
                "        return !list.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.list.MutableList;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(MutableList<String> list) {\n" +
                "        // Check if list is not empty\n" +
                "        return list.notEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
