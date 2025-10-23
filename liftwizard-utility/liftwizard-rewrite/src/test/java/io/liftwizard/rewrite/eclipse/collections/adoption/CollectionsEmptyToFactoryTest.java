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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class CollectionsEmptyToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new CollectionsEmptyToFactoryRecipes())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api", "eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collections;
                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> emptyList = Collections.emptyList();
                            List<String> emptyListExplicit = Collections.<String>emptyList();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> emptyList = Lists.fixedSize.empty();
                            List<String> emptyListExplicit = Lists.fixedSize.empty();
                        }
                    }
                    """
                )
            );
    }
}
