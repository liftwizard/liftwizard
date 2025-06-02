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
class ECListConstructorToFactoryTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new ECListConstructorToFactory())
            .parser(
                JavaParser.fromJavaVersion().dependsOn(
                        """
                        package org.eclipse.collections.api.list;

                        public interface MutableList<T> extends java.util.List<T> {
                        }
                        """,
                        """
                        package org.eclipse.collections.api.factory;

                        import org.eclipse.collections.api.list.MutableList;

                        public interface Lists {
                            MutableListFactory mutable = null;
                           \s
                            interface MutableListFactory {
                                <T> MutableList<T> empty();
                                <T> MutableList<T> with(T... elements);
                                <T> MutableList<T> of(T... elements);
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.impl.list.mutable;

                        import org.eclipse.collections.api.list.MutableList;

                        public class FastList<T> implements MutableList<T> {
                            public FastList() {}
                            public FastList(int initialCapacity) {}
                        }
                        """
                    )
            );
    }

    @Test
    @DocumentExample
    void replaceFastListConstructorVariations() {
        // With diamond operator
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList<String> list = new FastList<>();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList<String> list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );

        // Without generics (raw type)
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList list = new FastList();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList list = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void doNotReplaceFastListWithArguments() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList<String> list = new FastList<>(10);
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replaceMultipleFastListConstructors() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList<String> list1 = new FastList<>();
                            FastList<Integer> list2 = new FastList<>();
                            FastList list3 = new FastList();
                        }
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList<String> list1 = Lists.mutable.empty();
                            FastList<Integer> list2 = Lists.mutable.empty();
                            FastList list3 = Lists.mutable.empty();
                        }
                    }
                    """
                )
            );
    }

    @Test
    void replacementInFieldDeclaration() {
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        private FastList<String> list = new FastList<>();
                    }
                    """,
                    """
                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        private FastList<String> list = Lists.mutable.empty();
                    }
                    """
                )
            );
    }

    @Test
    void explicitGenericsAreNotTransformed() {
        // TODO: Support explicit generics in the future
        // For now, we skip transforming constructors with explicit type parameters
        this.rewriteRun(
                java(
                    """
                    import org.eclipse.collections.impl.list.mutable.FastList;

                    class Test {
                        void test() {
                            FastList<String> list = new FastList<String>();
                        }
                    }
                    """
                )
            );
    }
}
