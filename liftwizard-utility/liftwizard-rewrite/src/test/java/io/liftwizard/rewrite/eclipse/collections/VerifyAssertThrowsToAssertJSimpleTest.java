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

class VerifyAssertThrowsToAssertJSimpleTest implements RewriteTest {

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
                        "}\n"
                    )
            );
    }

    @Test
    @DocumentExample
    void replacesVerifyAssertThrows() {
        rewriteRun(
            java(
                "import org.eclipse.collections.impl.test.Verify;\n" +
                "import static org.assertj.core.api.Assertions.assertThatThrownBy;\n" +
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
}
