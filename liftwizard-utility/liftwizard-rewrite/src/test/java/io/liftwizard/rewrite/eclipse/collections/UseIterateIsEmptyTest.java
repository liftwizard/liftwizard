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

class UseIterateIsEmptyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new UseIterateIsEmpty())
            .parser(
                JavaParser.fromJavaVersion()
                    .classpath("eclipse-collections")
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.utility;
                        public final class Iterate {
                            public static boolean isEmpty(Iterable<?> iterable) {
                                return iterable == null || !iterable.iterator().hasNext();
                            }
                            public static boolean isEmpty(Object[] array) {
                                return array == null || array.length == 0;
                            }
                        }"""
                    )
            );
    }

    @Test
    @DocumentExample
    void replacesNullCheckAndIsEmptyWithIterateIsEmpty() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return list == null || list.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.Iterate;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return Iterate.isEmpty(list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesNullCheckAndIsEmptyWithIterateIsEmptyReversedOrder() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return null == list || list.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.Iterate;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return Iterate.isEmpty(list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesNullCheckAndIsEmptyWithSet() {
        rewriteRun(
            java(
                "import java.util.Set;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(Set<String> set) {\n" +
                "        return set == null || set.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.Iterate;\n" +
                "\n" +
                "import java.util.Set;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(Set<String> set) {\n" +
                "        return Iterate.isEmpty(set);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesNullCheckAndIsEmptyWithCollection() {
        rewriteRun(
            java(
                "import java.util.Collection;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(Collection<String> collection) {\n" +
                "        return collection == null || collection.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.Iterate;\n" +
                "\n" +
                "import java.util.Collection;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(Collection<String> collection) {\n" +
                "        return Iterate.isEmpty(collection);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void handlesComplexExpressions() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return getValue().getList() == null || getValue().getList().isEmpty();\n" +
                "    }\n" +
                "    \n" +
                "    private Test getValue() {\n" +
                "        return this;\n" +
                "    }\n" +
                "    \n" +
                "    private List<String> getList() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.Iterate;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return Iterate.isEmpty(getValue().getList());\n" +
                "    }\n" +
                "    \n" +
                "    private Test getValue() {\n" +
                "        return this;\n" +
                "    }\n" +
                "    \n" +
                "    private List<String> getList() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeWhenCollectionsDontMatch() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list1, List<String> list2) {\n" +
                "        return list1 == null || list2.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeWhenNotNullCheck() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return list.size() == 0 || list.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeWhenNotOrOperator() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return list == null && list.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeSimpleIsEmpty() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return list.isEmpty();\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeSimpleNullCheck() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        return list == null;\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void preservesWhitespaceAndComments() {
        rewriteRun(
            java(
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        // Check if list is null or empty\n" +
                "        return list == null || list.isEmpty();\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.impl.utility.Iterate;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "class Test {\n" +
                "    boolean test(List<String> list) {\n" +
                "        // Check if list is null or empty\n" +
                "        return Iterate.isEmpty(list);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
