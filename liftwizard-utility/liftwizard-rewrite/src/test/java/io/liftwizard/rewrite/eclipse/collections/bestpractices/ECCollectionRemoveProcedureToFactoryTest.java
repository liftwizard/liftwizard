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

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class ECCollectionRemoveProcedureToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECCollectionRemoveProcedureToFactory())
            .parser(org.openrewrite.java.JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replaceParameterizedCollectionRemoveProcedure() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List<String> list = new ArrayList<>();
                        CollectionRemoveProcedure<String> removeProcedure = new CollectionRemoveProcedure<String>(list);
                    }
                }
                """,
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List<String> list = new ArrayList<>();
                        CollectionRemoveProcedure<String> removeProcedure = CollectionRemoveProcedure.on(list);
                    }
                }
                """
            )
        );
    }

    @Test
    void replaceDiamondOperatorCollectionRemoveProcedure() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List<String> list = new ArrayList<>();
                        CollectionRemoveProcedure<String> removeProcedure = new CollectionRemoveProcedure<>(list);
                    }
                }
                """,
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List<String> list = new ArrayList<>();
                        CollectionRemoveProcedure<String> removeProcedure = CollectionRemoveProcedure.on(list);
                    }
                }
                """
            )
        );
    }

    @Test
    void replaceRawTypeCollectionRemoveProcedure() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List list = new ArrayList();
                        CollectionRemoveProcedure removeProcedure = new CollectionRemoveProcedure(list);
                    }
                }
                """,
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List list = new ArrayList();
                        CollectionRemoveProcedure removeProcedure = CollectionRemoveProcedure.on(list);
                    }
                }
                """
            )
        );
    }

    @Test
    @org.junit.jupiter.api.Disabled("Type resolution issues with CollectionRemoveProcedure")
    void doNotChangeConstructorWithNoArguments() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;

                class Test {
                    void test() {
                        // This would be a compilation error, but test that we don't transform it
                        CollectionRemoveProcedure<String> removeProcedure = new CollectionRemoveProcedure<>();
                    }
                }
                """
            )
        );
    }

    @Test
    @org.junit.jupiter.api.Disabled("Type resolution issues with CollectionRemoveProcedure")
    void doNotChangeConstructorWithMultipleArguments() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test() {
                        List<String> list1 = new ArrayList<>();
                        List<String> list2 = new ArrayList<>();
                        // This would be a compilation error, but test that we don't transform it
                        CollectionRemoveProcedure<String> removeProcedure = new CollectionRemoveProcedure<>(list1, list2);
                    }
                }
                """
            )
        );
    }

    @Test
    void replaceInMethodArgument() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import org.eclipse.collections.api.set.MutableSet;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test(MutableSet<String> set) {
                        List<String> toRemove = new ArrayList<>();
                        set.forEach(new CollectionRemoveProcedure<>(toRemove));
                    }
                }
                """,
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import org.eclipse.collections.api.set.MutableSet;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test(MutableSet<String> set) {
                        List<String> toRemove = new ArrayList<>();
                        set.forEach(CollectionRemoveProcedure.on(toRemove));
                    }
                }
                """
            )
        );
    }

    @Test
    void replaceMultipleInSameClass() {
        rewriteRun(
            java(
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test1() {
                        List<String> list = new ArrayList<>();
                        CollectionRemoveProcedure<String> removeProcedure = new CollectionRemoveProcedure<>(list);
                    }

                    void test2() {
                        List<Integer> list = new ArrayList<>();
                        CollectionRemoveProcedure<Integer> removeProcedure = new CollectionRemoveProcedure<>(list);
                    }
                }
                """,
                """
                import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                import java.util.List;
                import java.util.ArrayList;

                class Test {
                    void test1() {
                        List<String> list = new ArrayList<>();
                        CollectionRemoveProcedure<String> removeProcedure = CollectionRemoveProcedure.on(list);
                    }

                    void test2() {
                        List<Integer> list = new ArrayList<>();
                        CollectionRemoveProcedure<Integer> removeProcedure = CollectionRemoveProcedure.on(list);
                    }
                }
                """
            )
        );
    }
}
