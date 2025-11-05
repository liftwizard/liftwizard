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

import io.liftwizard.rewrite.eclipse.collections.AbstractEclipseCollectionsTest;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class CollectionAddProcedureOnToMethodReferenceTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec
            .recipe(new CollectionAddProcedureOnToMethodReferenceRecipes())
            .typeValidationOptions(TypeValidation.none());
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list1 = new ArrayList<>();
                            list1.forEach(CollectionAddProcedure.on(list1));

                            List<String> list2 = new ArrayList<>();
                            var procedure = CollectionAddProcedure.on(list2);
                            list2.forEach(procedure);

                            List<String> list3 = new ArrayList<>();
                            Procedure<String> addProcedure1 = new CollectionAddProcedure<String>(list3);

                            List<String> list4 = new ArrayList<>();
                            Procedure<String> addProcedure2 = new CollectionAddProcedure<>(list4);

                            List<String> list5 = new ArrayList<>();
                            list5.forEach(new CollectionAddProcedure<>(list5));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionAddProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> list1 = new ArrayList<>();
                            list1.forEach(list1::add);

                            List<String> list2 = new ArrayList<>();
                            var procedure = list2::add;
                            list2.forEach(procedure);

                            List<String> list3 = new ArrayList<>();
                            Procedure<String> addProcedure1 = list3::add;

                            List<String> list4 = new ArrayList<>();
                            Procedure<String> addProcedure2 = list4::add;

                            List<String> list5 = new ArrayList<>();
                            list5.forEach(list5::add);
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
                    import java.util.List;
                    import java.util.ArrayList;

                    class Test {
                        void test() {
                            List<String> keys = new ArrayList<>();
                            keys.forEach(keys::add);
                        }
                    }
                    """
                )
            );
    }
}
