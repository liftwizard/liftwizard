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
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

@ExtendWith(LogMarkerTestExtension.class)
class ECArraysAsListToWithTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECArraysAsListToWith())
            .parser(
                JavaParser.fromJavaVersion()
                    .classpath("eclipse-collections-api", "eclipse-collections")
                    .dependsOn(
                        """
                        package org.eclipse.collections.api.list;

                        public interface MutableList<T> extends java.util.List<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.set;

                        public interface MutableSet<T> extends java.util.Set<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.bag;

                        public interface MutableBag<T> extends java.util.Collection<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.list.mutable;

                        import org.eclipse.collections.api.list.MutableList;

                        public class FastList<T> implements MutableList<T> {
                            public static <T> MutableList<T> newList(java.util.Collection<? extends T> source) {
                                return null;
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.set.mutable;

                        import org.eclipse.collections.api.set.MutableSet;

                        public class UnifiedSet<T> implements MutableSet<T> {
                            public static <T> MutableSet<T> newSet(java.util.Collection<? extends T> source) {
                                return null;
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.bag.mutable;

                        import org.eclipse.collections.api.bag.MutableBag;

                        public class HashBag<T> implements MutableBag<T> {
                            public static <T> MutableBag<T> newBag(java.util.Collection<? extends T> source) {
                                return null;
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.list.MutableList;

                        public final class Lists {
                            public static final MutableListFactory mutable = null;

                            public static final class MutableListFactory {
                                public <T> MutableList<T> with(T... elements) { return null; }
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.set.MutableSet;

                        public final class Sets {
                            public static final MutableSetFactory mutable = null;

                            public static final class MutableSetFactory {
                                public <T> MutableSet<T> with(T... elements) { return null; }
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.bag.MutableBag;

                        public final class Bags {
                            public static final MutableBagFactory mutable = null;

                            public static final class MutableBagFactory {
                                public <T> MutableBag<T> with(T... elements) { return null; }
                            }
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceFastListNewListWithArraysAsList() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            var list = FastList.newList(Arrays.asList("a", "b", "c"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            var list = Lists.mutable.with("a", "b", "c");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceUnifiedSetNewSetWithArraysAsList() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            var set = UnifiedSet.newSet(Arrays.asList("a", "b", "c"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            var set = Sets.mutable.with("a", "b", "c");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceHashBagNewBagWithArraysAsList() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.bag.mutable.HashBag;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            var bag = HashBag.newBag(Arrays.asList("a", "b", "c"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Bags;

                    class Test {
                        void test() {
                            var bag = Bags.mutable.with("a", "b", "c");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithSingleElement() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            var list = FastList.newList(Arrays.asList("single"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            var list = Lists.mutable.with("single");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithNumbers() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            var set = UnifiedSet.newSet(Arrays.asList(1, 2, 3, 4, 5));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            var set = Sets.mutable.with(1, 2, 3, 4, 5);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceWithVariables() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            String a = "first";
                            String b = "second";
                            var list = FastList.newList(Arrays.asList(a, b));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;

                    class Test {
                        void test() {
                            String a = "first";
                            String b = "second";
                            var list = Lists.mutable.with(a, b);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceNonArraysAsListArgument() {
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

    @Test
    void replaceMultipleInSameMethod() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import org.eclipse.collections.impl.set.mutable.UnifiedSet;
                    import org.eclipse.collections.impl.bag.mutable.HashBag;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            var list = FastList.newList(Arrays.asList("a", "b"));
                            var set = UnifiedSet.newSet(Arrays.asList(1, 2, 3));
                            var bag = HashBag.newBag(Arrays.asList("x", "y", "z"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Bags;
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Sets;

                    class Test {
                        void test() {
                            var list = Lists.mutable.with("a", "b");
                            var set = Sets.mutable.with(1, 2, 3);
                            var bag = Bags.mutable.with("x", "y", "z");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void preserveTypeParameters() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import org.eclipse.collections.api.list.MutableList;
                    import java.util.Arrays;

                    class Test {
                        void test() {
                            MutableList<String> list = FastList.newList(Arrays.asList("a", "b", "c"));
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b", "c");
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceInFieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;
                    import org.eclipse.collections.api.list.MutableList;
                    import java.util.Arrays;

                    class Test {
                        private final MutableList<String> list = FastList.newList(Arrays.asList("a", "b", "c"));
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.list.MutableList;

                    class Test {
                        private final MutableList<String> list = Lists.mutable.with("a", "b", "c");
                    }
                    """
                )
            );
    }
}
