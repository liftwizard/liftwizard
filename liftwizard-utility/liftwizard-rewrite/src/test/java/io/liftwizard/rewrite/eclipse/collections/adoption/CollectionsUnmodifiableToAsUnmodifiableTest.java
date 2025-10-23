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

class CollectionsUnmodifiableToAsUnmodifiableTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new CollectionsUnmodifiableToAsUnmodifiableRecipes())
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
                            List<String> unmodifiableList = Collections.unmodifiableList(list);

                            MutableSet<String> set = Sets.mutable.with("x", "y");
                            Set<String> unmodifiableSet = Collections.unmodifiableSet(set);

                            MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
                            Map<String, Integer> unmodifiableMap = Collections.unmodifiableMap(map);

                            Collection<String> unmodifiableCollection = Collections.unmodifiableCollection(list);

                            List<String> inlineExpression = Collections.unmodifiableList(Lists.mutable.with("c", "d"));

                            MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
                            Map<String, Integer> result = Collections.unmodifiableMap(anotherMap);

                            MutableList<String> list2 = Lists.mutable.with("e", "f");
                            List<String> unmodifiableList1 = Collections.unmodifiableList(list2);
                            List<String> unmodifiableList2 = Collections.unmodifiableList(Lists.mutable.with("g"));

                            MutableSet<String> set2 = Sets.mutable.with("z");
                            Set<String> unmodifiableSet2 = Collections.unmodifiableSet(set2);

                            List<String> chainResult = Collections.unmodifiableList(Lists.mutable.with("h", "i", "j"));
                            int size = chainResult.size();
                        }

                        List<String> getList() {
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            return Collections.unmodifiableList(list);
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
                            List<String> unmodifiableList = list.asUnmodifiable();

                            MutableSet<String> set = Sets.mutable.with("x", "y");
                            Set<String> unmodifiableSet = set.asUnmodifiable();

                            MutableMap<String, Integer> map = Maps.mutable.with("key", 1);
                            Map<String, Integer> unmodifiableMap = map.asUnmodifiable();

                            Collection<String> unmodifiableCollection = list.asUnmodifiable();

                            List<String> inlineExpression = Lists.mutable.with("c", "d").asUnmodifiable();

                            MutableMap<String, Integer> anotherMap = Maps.mutable.with("key", 1);
                            Map<String, Integer> result = anotherMap.asUnmodifiable();

                            MutableList<String> list2 = Lists.mutable.with("e", "f");
                            List<String> unmodifiableList1 = list2.asUnmodifiable();
                            List<String> unmodifiableList2 = Lists.mutable.with("g").asUnmodifiable();

                            MutableSet<String> set2 = Sets.mutable.with("z");
                            Set<String> unmodifiableSet2 = set2.asUnmodifiable();

                            List<String> chainResult = Lists.mutable.with("h", "i", "j").asUnmodifiable();
                            int size = chainResult.size();
                        }

                        List<String> getList() {
                            MutableList<String> list = Lists.mutable.with("a", "b");
                            return list.asUnmodifiable();
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
                            List<String> unmodifiableList = Collections.unmodifiableList(list);
                        }
                    }
                    """
                )
            );
    }
}
