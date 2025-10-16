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

class ECCollectionAddProcedureToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECCollectionAddProcedureToFactory())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replaceParameterizedCollectionAddProcedure() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = new CollectionAddProcedure<String>(list);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = CollectionAddProcedure.on(list);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceDiamondOperatorCollectionAddProcedure() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = new CollectionAddProcedure<>(list);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = CollectionAddProcedure.on(list);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceRawTypeCollectionAddProcedure() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List list = new ArrayList();
                            CollectionAddProcedure addProcedure = new CollectionAddProcedure(list);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List list = new ArrayList();
                            CollectionAddProcedure addProcedure = CollectionAddProcedure.on(list);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotChangeConstructorWithNoArguments() {
        this.rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.none()),
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;

                    class Test {
                        void test() {
                            CollectionAddProcedure<String> addProcedure = new CollectionAddProcedure<>();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotChangeConstructorWithMultipleArguments() {
        this.rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.none()),
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list1 = new ArrayList<>();
                            List<String> list2 = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = new CollectionAddProcedure<>(list1, list2);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceInMethodArgument() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import org.eclipse.collections.api.map.MutableMap;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(new CollectionAddProcedure<>(keys));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import org.eclipse.collections.api.map.MutableMap;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(CollectionAddProcedure.on(keys));
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleInSameClass() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test1() {
                            List<String> list = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = new CollectionAddProcedure<>(list);
                        }

                        void test2() {
                            List<Integer> list = new ArrayList<>();
                            CollectionAddProcedure<Integer> addProcedure = new CollectionAddProcedure<>(list);
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test1() {
                            List<String> list = new ArrayList<>();
                            CollectionAddProcedure<String> addProcedure = CollectionAddProcedure.on(list);
                        }

                        void test2() {
                            List<Integer> list = new ArrayList<>();
                            CollectionAddProcedure<Integer> addProcedure = CollectionAddProcedure.on(list);
                        }
                    }
                    """
                )
            );
    }
}
