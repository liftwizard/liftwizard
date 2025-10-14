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

package io.liftwizard.rewrite.eclipse.collections.adoption;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

// TODO 2025-10-22: The assertion on `Collection<String> synchronizedCollection = Collections.synchronizedCollection(list);` should use MutableCollection instead of MutableList.
class CollectionsSynchronizedToAsSynchronizedTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new CollectionsSynchronizedToAsSynchronizedRecipes())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api", "eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacePatterns() {
        this.rewriteRun(
                java(
                    """
                    import java.util.Collection;
                    import java.util.Collections;
                    import java.util.List;
                    import java.util.Map;
                    import java.util.Set;

                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            List<String> synchronizedList = Collections.synchronizedList(list);

                            MutableSet<String> set = Sets.mutable.with("x", "y");
                            Set<String> synchronizedSet = Collections.synchronizedSet(set);

                            MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
                            Map<String, Integer> synchronizedMap = Collections.synchronizedMap(map);

                            Collection<String> synchronizedCollection = Collections.synchronizedCollection(list);

                            List<String> inlineExpression = Collections.synchronizedList(Lists.mutable.with("c", "d"));

                            MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
                            Map<String, Integer> result = Collections.synchronizedMap(anotherMap);
                            result.put("another", 2);
                        }
                    }
                    """,
                    """
                    import java.util.Collection;
                    import java.util.List;
                    import java.util.Map;
                    import java.util.Set;

                    import org.eclipse.collections.api.factory.Lists;
                    import org.eclipse.collections.api.factory.Maps;
                    import org.eclipse.collections.api.factory.Sets;
                    import org.eclipse.collections.api.list.MutableList;
                    import org.eclipse.collections.api.map.MutableMap;
                    import org.eclipse.collections.api.set.MutableSet;

                    class Test {
                        void test() {
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            List<String> synchronizedList = list.asSynchronized();

                            MutableSet<String> set = Sets.mutable.with("x", "y");
                            Set<String> synchronizedSet = set.asSynchronized();

                            MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
                            Map<String, Integer> synchronizedMap = map.asSynchronized();

                            Collection<String> synchronizedCollection = list.asSynchronized();

                            List<String> inlineExpression = Lists.mutable.with("c", "d").asSynchronized();

                            MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
                            Map<String, Integer> result = anotherMap.asSynchronized();
                            result.put("another", 2);
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
                    import java.util.ArrayList;
                    import java.util.Collections;
                    import java.util.List;

                    class Test {
                        void test() {
                            List<String> list = new ArrayList<>();
                            List<String> synchronizedList = Collections.synchronizedList(list);
                        }
                    }
                    """
                )
            );
    }
}
