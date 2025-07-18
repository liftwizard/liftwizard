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

class ArrayIterateAsListToListsMutableWithTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ArrayIterateAsListToListsMutableWith())
            .parser(
                JavaParser.fromJavaVersion()
                    .dependsOn(
                        """
                        package org.eclipse.collections.impl.utility;
                        import org.eclipse.collections.api.list.MutableList;
                        import org.eclipse.collections.impl.factory.Lists;
                        public class ArrayIterate {
                            public static <T> MutableList<T> asList(T[] array) {
                                return Lists.mutable.with(array);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;
                        import org.eclipse.collections.api.list.MutableList;
                        public class Lists {
                            public static final MutableListFactory mutable = new MutableListFactory();
                            public static class MutableListFactory {
                                public <T> MutableList<T> with(T... elements) {
                                    return null;
                                }
                            }
                        }
                        """
                    )
                    .classpath("eclipse-collections-api", "eclipse-collections")
            )
            .typeValidationOptions(org.openrewrite.test.TypeValidation.none());
    }

    @Test
    @DocumentExample
    void replacesArrayIterateAsListWithSingleElement() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Object list = ArrayIterate.asList(new String[]{\"hello\"});\n" +
                "    }\n" +
                "}\n",
                "import org.eclipse.collections.api.factory.Lists;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Object list = Lists.mutable.with(\"hello\");\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeArrayIterateAsListWithoutArrayInitializer() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.utility.ArrayIterate;\n" +
                "\n" +
                "class Test {\n" +
                "    void test(String[] array) {\n" +
                "        Object list = ArrayIterate.asList(array);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
