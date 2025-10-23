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

package io.liftwizard.rewrite.eclipse.collections.bestpractices;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class ECListConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECListConstructorToFactory())
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import java.util.Map;

                    class Test {
                        private MutableList<String> fieldList = new FastList<>();

                        void test() {
                            MutableList<String> diamondList = new FastList<>();
                            MutableList rawList = new FastList();
                            MutableList<String> list1 = new FastList<>();
                            MutableList<Integer> list2 = new FastList<>();
                            MutableList<String> explicitSimple = new FastList<String>();
                            MutableList<Map<String, Integer>> explicitNested = new FastList<Map<String, Integer>>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    import java.util.Map;

                    class Test {
                        private MutableList<String> fieldList = Lists.mutable.empty();

                        void test() {
                            MutableList<String> diamondList = Lists.mutable.empty();
                            MutableList rawList = Lists.mutable.empty();
                            MutableList<String> list1 = Lists.mutable.empty();
                            MutableList<Integer> list2 = Lists.mutable.empty();
                            MutableList<String> explicitSimple = Lists.mutable.<String>empty();
                            MutableList<Map<String, Integer>> explicitNested = Lists.mutable.<Map<String, Integer>>empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void initialCapacityConstructor() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        MutableList<String> listWithCapacity = new FastList<>(16);
                        MutableList<Integer> anotherListWithCapacity = new FastList<>(32);
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        MutableList<String> listWithCapacity = Lists.mutable.withInitialCapacity(16);
                        MutableList<Integer> anotherListWithCapacity = Lists.mutable.withInitialCapacity(32);
                    }
                    """
                )
            );
    }

    @Test
    void collectionConstructor() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        MutableList<String> emptyList = new FastList<>();
                        MutableList<String> listFromOther = new FastList<>(emptyList);

                        void method() {
                            MutableList<Integer> numbers = new FastList<>();
                            MutableList<Integer> listFromNumbers = new FastList<>(numbers);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        MutableList<String> emptyList = Lists.mutable.empty();
                        MutableList<String> listFromOther = Lists.mutable.withAll(emptyList);

                        void method() {
                            MutableList<Integer> numbers = Lists.mutable.empty();
                            MutableList<Integer> listFromNumbers = Lists.mutable.withAll(numbers);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceInvalidPatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        FastList<String> concreteTypeList = new FastList<>();
                    }
                    """
                )
            );
    }
}
