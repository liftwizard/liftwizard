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

class CollectionRemoveProcedureOnToMethodReferenceTest extends AbstractEclipseCollectionsTest {

    @Override
    public void defaults(RecipeSpec spec) {
        super.defaults(spec);
        spec
            .recipe(new CollectionRemoveProcedureOnToMethodReferenceRecipes())
            .typeValidationOptions(TypeValidation.none());
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
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import java.util.List;
                    import java.util.ArrayList;
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            MutableSet<String> set1 = UnifiedSet.newSet();
                            Set<String> toRemove1 = new HashSet<>();
                            set1.forEach(CollectionRemoveProcedure.on(toRemove1));

                            MutableSet<String> set2 = UnifiedSet.newSet();
                            Set<String> toRemove2 = new HashSet<>();
                            var procedure = CollectionRemoveProcedure.on(toRemove2);
                            set2.forEach(procedure);

                            List<String> list1 = new ArrayList<>();
                            Procedure<String> removeProcedure1 = new CollectionRemoveProcedure<String>(list1);

                            List<String> list2 = new ArrayList<>();
                            Procedure<String> removeProcedure2 = new CollectionRemoveProcedure<>(list2);

                            MutableSet<String> set3 = UnifiedSet.newSet();
                            List<String> toRemove3 = new ArrayList<>();
                            set3.forEach(new CollectionRemoveProcedure<>(toRemove3));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.block.procedure.CollectionRemoveProcedure;
                    import org.eclipse.collections.api.block.procedure.Procedure;
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import java.util.List;
                    import java.util.ArrayList;
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            MutableSet<String> set1 = UnifiedSet.newSet();
                            Set<String> toRemove1 = new HashSet<>();
                            set1.forEach(toRemove1::remove);

                            MutableSet<String> set2 = UnifiedSet.newSet();
                            Set<String> toRemove2 = new HashSet<>();
                            var procedure = toRemove2::remove;
                            set2.forEach(procedure);

                            List<String> list1 = new ArrayList<>();
                            Procedure<String> removeProcedure1 = list1::remove;

                            List<String> list2 = new ArrayList<>();
                            Procedure<String> removeProcedure2 = list2::remove;

                            MutableSet<String> set3 = UnifiedSet.newSet();
                            List<String> toRemove3 = new ArrayList<>();
                            set3.forEach(toRemove3::remove);
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
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import java.util.Set;
                    import java.util.HashSet;

                    class Test {
                        void test() {
                            MutableSet<String> set = UnifiedSet.newSet();
                            Set<String> toRemove = new HashSet<>();
                            set.forEach(toRemove::remove);
                        }
                    }
                    """
                )
            );
    }
}
