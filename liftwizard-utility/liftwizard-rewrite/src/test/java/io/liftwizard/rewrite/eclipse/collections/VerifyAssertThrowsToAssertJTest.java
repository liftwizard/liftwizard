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

class VerifyAssertThrowsToAssertJTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new VerifyAssertThrowsToAssertJ())
            .parser(
                JavaParser.fromJavaVersion()
                    .classpath("junit", "assertj-core", "eclipse-collections-api")
                    .dependsOn(
                        "package org.eclipse.collections.impl.test;\n" +
                        "public class Verify {\n" +
                        "    public static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable) {\n" +
                        "        // stub implementation\n" +
                        "    }\n" +
                        "    public static void assertEmpty(java.util.Collection<?> collection) {\n" +
                        "        // stub implementation\n" +
                        "    }\n" +
                        "    public static void assertNotEmpty(java.util.Collection<?> collection) {\n" +
                        "        // stub implementation\n" +
                        "    }\n" +
                        "}\n",
                        "package java.util;\n" +
                        "public class Collections {\n" +
                        "    public static <T> java.util.List<T> emptyList() {\n" +
                        "        return new java.util.ArrayList<T>();\n" +
                        "    }\n" +
                        "    public static <T> java.util.List<T> singletonList(T o) {\n" +
                        "        java.util.List<T> list = new java.util.ArrayList<T>();\n" +
                        "        list.add(o);\n" +
                        "        return list;\n" +
                        "    }\n" +
                        "}\n"
                    )
            );
    }

    @Test
    @DocumentExample
    void replacesVerifyAssertThrowsWithAssertThatThrownBy() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertThrows(IllegalArgumentException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                throw new IllegalArgumentException();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            throw new IllegalArgumentException();\n" +
                "        }).isInstanceOf(IllegalArgumentException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesVerifyAssertThrowsWithNullPointerException() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertThrows(NullPointerException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                String str = null;\n" +
                "                str.length();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            String str = null;\n" +
                "            str.length();\n" +
                "        }).isInstanceOf(NullPointerException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesVerifyAssertThrowsWithRuntimeException() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertThrows(RuntimeException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                throw new RuntimeException(\"Test exception\");\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            throw new RuntimeException(\"Test exception\");\n" +
                "        }).isInstanceOf(RuntimeException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesVerifyAssertThrowsWithUnsupportedOperationException() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertThrows(UnsupportedOperationException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                throw new UnsupportedOperationException();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            throw new UnsupportedOperationException();\n" +
                "        }).isInstanceOf(UnsupportedOperationException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void replacesVerifyAssertThrowsWithMultipleStatements() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertThrows(IllegalArgumentException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                int x = 5;\n" +
                "                if (x > 0) {\n" +
                "                    throw new IllegalArgumentException();\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            int x = 5;\n" +
                "            if (x > 0) {\n" +
                "                throw new IllegalArgumentException();\n" +
                "            }\n" +
                "        }).isInstanceOf(IllegalArgumentException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void preservesExistingStaticImports() {
        rewriteRun(
            java(
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThat(\"test\").isNotNull();\n" +
                "        Verify.assertThrows(IllegalArgumentException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                throw new IllegalArgumentException();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThat;\n" +
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThat(\"test\").isNotNull();\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            throw new IllegalArgumentException();\n" +
                "        }).isInstanceOf(IllegalArgumentException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeOtherVerifyMethods() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import java.util.Collections;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertEmpty(Collections.emptyList());\n" +
                "        Verify.assertNotEmpty(Collections.singletonList(\"test\"));\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void doesNotChangeNonVerifyAssertThrows() {
        rewriteRun(
            java(
                "import org.junit.jupiter.api.Test;\n" +
                "import static org.junit.jupiter.api.Assertions.assertThrows;\n" +
                "\n" +
                "class TestClass {\n" +
                "    @Test\n" +
                "    void test() {\n" +
                "        assertThrows(IllegalArgumentException.class, () -> {\n" +
                "            throw new IllegalArgumentException();\n" +
                "        });\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void handlesInlineRunnableInstantiation() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        Verify.assertThrows(IllegalStateException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                throw new IllegalStateException(\"Invalid state\");\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            throw new IllegalStateException(\"Invalid state\");\n" +
                "        }).isInstanceOf(IllegalStateException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }

    @Test
    void preservesCommentsAndFormatting() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        // This should throw an exception\n" +
                "        Verify.assertThrows(IllegalArgumentException.class, new Runnable() {\n" +
                "            public void run() {\n" +
                "                throw new IllegalArgumentException();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n",
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
                "\n" +
                "class Test {\n" +
                "    void test() {\n" +
                "        // This should throw an exception\n" +
                "        assertThatThrownBy(() -> {\n" +
                "            throw new IllegalArgumentException();\n" +
                "        }).isInstanceOf(IllegalArgumentException.class);\n" +
                "    }\n" +
                "}\n"
            )
        );
    }
}
