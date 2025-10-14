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

package io.liftwizard.rewrite.eclipse.collections.removal;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

// TODO 2025-10-22: Refactor the test input to be shorter, by relying on local variables rather than separate methods
class CollectionAddProcedureOnToMethodReferenceTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new CollectionAddProcedureOnToMethodReferenceRecipes())
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import org.eclipse.collections.api.map.MutableMap;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void testFactoryMethodInArgument(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(CollectionAddProcedure.on(keys));
                        }

                        void testFactoryMethodAssignment() {
                            List<String> list = new ArrayList<>();
                            var procedure = CollectionAddProcedure.on(list);
                            list.forEach(procedure);
                        }

                        void testConstructorParameterized() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> addProcedure = new CollectionAddProcedure<String>(list);
                        }

                        void testConstructorDiamond() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> addProcedure = new CollectionAddProcedure<>(list);
                        }

                        void testConstructorInArgument(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(new CollectionAddProcedure<>(keys));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import org.eclipse.collections.api.map.MutableMap;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void testFactoryMethodInArgument(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(keys::add);
                        }

                        void testFactoryMethodAssignment() {
                            List<String> list = new ArrayList<>();
                            var procedure = list::add;
                            list.forEach(procedure);
                        }

                        void testConstructorParameterized() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> addProcedure = list::add;
                        }

                        void testConstructorDiamond() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> addProcedure = list::add;
                        }

                        void testConstructorInArgument(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(keys::add);
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
                    import org.eclipse.collections.api.map.MutableMap;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void testMethodReference(MutableMap<String, Integer> map) {
                            List<String> keys = new ArrayList<>();
                            map.forEachKey(keys::add);
                        }
                    }
                    """
                )
            );
    }
}
