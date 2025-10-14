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

import static org.openrewrite.java.Assertions.java;

class ECArraysAsListToWithTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECArraysAsListToWithRecipes())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api", "eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import org.eclipse.collections.impl.bag.mutable.HashBag;
                    import org.eclipse.collections.api.list.MutableList;
                    import java.util.Arrays;

                    class Test {
                        private final MutableList<String> fieldList = FastList.newList(Arrays.asList("a", "b", "c"));

                        void test() {
                            String a = "a";
                            String b = "b";
                            var list = FastList.newList(Arrays.asList("a", "b", "c"));
                            var set = UnifiedSet.newSet(Arrays.asList("a", "b", "c"));
                            var bag = HashBag.newBag(Arrays.asList("a", "b", "c"));
                            var singleElement = FastList.newList(Arrays.asList("single"));
                            var numbers = UnifiedSet.newSet(Arrays.asList(1, 2, 3, 4, 5));
                            var variables = FastList.newList(Arrays.asList(a, b));
                            var multipleList = FastList.newList(Arrays.asList("x", "y"));
                            var multipleSet = UnifiedSet.newSet(Arrays.asList(1, 2, 3));
                            var multipleBag = HashBag.newBag(Arrays.asList("p", "q", "r"));
                            MutableList<String> typed = FastList.newList(Arrays.asList("d", "e", "f"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import org.eclipse.collections.impl.bag.mutable.HashBag;
                    import org.eclipse.collections.api.factory.Bags;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        private final MutableList<String> fieldList = Lists.mutable.with("a", "b", "c");

                        void test() {
                            String a = "a";
                            String b = "b";
                            var list = Lists.mutable.with("a", "b", "c");
                            var set = Sets.mutable.with("a", "b", "c");
                            var bag = Bags.mutable.with("a", "b", "c");
                            var singleElement = Lists.mutable.with("single");
                            var numbers = Sets.mutable.with(1, 2, 3, 4, 5);
                            var variables = Lists.mutable.with(a, b);
                            var multipleList = Lists.mutable.with("x", "y");
                            var multipleSet = Sets.mutable.with(1, 2, 3);
                            var multipleBag = Bags.mutable.with("p", "q", "r");
                            MutableList<String> typed = Lists.mutable.with("d", "e", "f");
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
                    import java.util.List;

                    class Test {
                        void test(List<String> source) {
                            var list = FastList.newList(source);
                        }
                    }
                    """
                )
            );
    }
}
