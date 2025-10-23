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

class CollectionsSingletonToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new CollectionsSingletonToFactoryRecipes())
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
                    import java.util.Map;
                    import java.util.Set;

                    class Test {
                        void test() {
                            List<String> singletonList = Collections.singletonList("element");
                            Set<String> singleton = Collections.singleton("element");
                            Map<String, String> singletonMap = Collections.singletonMap("key", "value");
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.Sets;

                    import java.util.List;
                    import java.util.Map;
                    import java.util.Set;

                    class Test {
                        void test() {
                            List<String> singletonList = Lists.fixedSize.with("element");
                            Set<String> singleton = Sets.fixedSize.with("element");
                            Map<String, String> singletonMap = Maps.fixedSize.with("key", "value");
                        }
                    }
                    """
                )
            );
    }
}
