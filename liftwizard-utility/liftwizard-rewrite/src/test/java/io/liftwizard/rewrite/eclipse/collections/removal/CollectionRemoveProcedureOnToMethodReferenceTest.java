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
class CollectionRemoveProcedureOnToMethodReferenceTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new CollectionRemoveProcedureOnToMethodReferenceRecipes())
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import org.eclipse.collections.api.set.MutableSet;
                    import java.util.List;
                    import java.util.ArrayList;
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void testFactoryMethodInArgument(MutableSet<String> set) {
                            Set<String> toRemove = new HashSet<>();
                            set.forEach(CollectionRemoveProcedure.on(toRemove));
                        }

                        void testFactoryMethodAssignment() {
                            Set<String> set = new HashSet<>();
                            var procedure = CollectionRemoveProcedure.on(set);
                            set.forEach(procedure);
                        }

                        void testConstructorExplicitGenerics() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> removeProcedure = new CollectionRemoveProcedure<String>(list);
                        }

                        void testConstructorDiamondOperator() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> removeProcedure = new CollectionRemoveProcedure<>(list);
                        }

                        void testConstructorInArgument(MutableSet<String> set) {
                            List<String> toRemove = new ArrayList<>();
                            set.forEach(new CollectionRemoveProcedure<>(toRemove));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import org.eclipse.collections.api.set.MutableSet;
                    import java.util.List;
                    import java.util.ArrayList;
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void testFactoryMethodInArgument(MutableSet<String> set) {
                            Set<String> toRemove = new HashSet<>();
                            set.forEach(toRemove::remove);
                        }

                        void testFactoryMethodAssignment() {
                            Set<String> set = new HashSet<>();
                            var procedure = set::remove;
                            set.forEach(procedure);
                        }

                        void testConstructorExplicitGenerics() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> removeProcedure = list::remove;
                        }

                        void testConstructorDiamondOperator() {
                            List<String> list = new ArrayList<>();
                            Procedure<String> removeProcedure = list::remove;
                        }

                        void testConstructorInArgument(MutableSet<String> set) {
                            List<String> toRemove = new ArrayList<>();
                            set.forEach(toRemove::remove);
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
                    import org.eclipse.collections.api.set.MutableSet;
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void testMethodReference(MutableSet<String> set) {
                            Set<String> toRemove = new HashSet<>();
                            set.forEach(toRemove::remove);
                        }
                    }
                    """
                )
            );
    }
}
