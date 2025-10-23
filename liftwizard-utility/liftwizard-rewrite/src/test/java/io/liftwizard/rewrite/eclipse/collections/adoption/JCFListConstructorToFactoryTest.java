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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class JCFListConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new JCFListConstructorToFactory());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.ArrayList;
                    import java.util.List;

                    class Test {
                        private ArrayList<String> fieldList = new ArrayList<>();

                        void test() {
                            ArrayList<String> diamondList = new ArrayList<>();
                            ArrayList rawList = new ArrayList();
                            List<String> typeInference = new ArrayList<>();
                            List<List<String>> nestedGenerics = new ArrayList<>();
                            List<? extends Number> wildcardGenerics = new ArrayList<>();
                            List<String> explicitSimple = new ArrayList<String>();
                            List<List<String>> explicitNested = new ArrayList<List<String>>();
                            java.util.List<String> fullyQualified = new ArrayList<>();
                            ArrayList<String> withInitialCapacity = new ArrayList<>(10);
                            List<String> withCapacity20 = new ArrayList<>(20);
                            List<String> explicit30 = new ArrayList<String>(30);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    import java.util.ArrayList;
                    import java.util.List;

                    class Test {
                        private ArrayList<String> fieldList = new ArrayList<>();

                        void test() {
                            ArrayList<String> diamondList = new ArrayList<>();
                            ArrayList rawList = new ArrayList();
                            List<String> typeInference = Lists.mutable.empty();
                            List<List<String>> nestedGenerics = Lists.mutable.empty();
                            List<? extends Number> wildcardGenerics = Lists.mutable.empty();
                            List<String> explicitSimple = Lists.mutable.<String>empty();
                            List<List<String>> explicitNested = Lists.mutable.<List<String>>empty();
                            java.util.List<String> fullyQualified = Lists.mutable.empty();
                            ArrayList<String> withInitialCapacity = new ArrayList<>(10);
                            List<String> withCapacity20 = Lists.mutable.withInitialCapacity(20);
                            List<String> explicit30 = Lists.mutable.<String>withInitialCapacity(30);
                        }
                    }
                    """
                )
            );
    }
}
