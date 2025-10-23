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

class JCFSetToMutableSetTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new JCFSetToMutableSet())
            .typeValidationOptions(TypeValidation.none())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Set;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        private Set<String> fieldSet = Sets.mutable.empty();

                        void test() {
                            Set<String> simpleSet = Sets.mutable.empty();
                            java.util.Set<String> fullyQualifiedSet = Sets.mutable.empty();
                            Set rawSet = Sets.mutable.empty();
                            java.util.Set rawSetFullyQualified = Sets.mutable.empty();
                            Set<List<Integer>> nestedGenerics = Sets.mutable.empty();
                            Set<String> unifiedSet = UnifiedSet.newSet();
                            Set<String> set1 = Sets.mutable.empty(), set2 = Sets.mutable.with("a");
                        }
                    }
                    """,
                    """
                    import java.util.Set;
                    import java.util.List;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.set.MutableSet;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;

                    class Test {
                        private MutableSet<String> fieldSet = Sets.mutable.empty();

                        void test() {
                            MutableSet<String> simpleSet = Sets.mutable.empty();
                            MutableSet<String> fullyQualifiedSet = Sets.mutable.empty();
                            MutableSet rawSet = Sets.mutable.empty();
                            MutableSet rawSetFullyQualified = Sets.mutable.empty();
                            MutableSet<List<Integer>> nestedGenerics = Sets.mutable.empty();
                            MutableSet<String> unifiedSet = UnifiedSet.newSet();
                            MutableSet<String> set1 = Sets.mutable.empty(), set2 = Sets.mutable.with("a");
                        }
                    }
                    """
                )
            );
    }
}
