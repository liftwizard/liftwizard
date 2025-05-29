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

package io.liftwizard.rewrite.eclipse.collections;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class SimplifyNegatedSatisfiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
            .recipe(new SimplifyNegatedSatisfies())
            .parser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api").classpath("eclipse-collections"));
    }

    @Test
    @DocumentExample
    void replacesNegatedNoneSatisfyWithAnySatisfyOnRichIterable() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion()
                            .dependsOn(
                                "package org.eclipse.collections.api.list;\n" +
                                "public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {\n" +
                                "    boolean anySatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "    boolean noneSatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "}\n",
                                "package org.eclipse.collections.api;\n" +
                                "public interface RichIterable<T> {\n" +
                                "    boolean anySatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "    boolean noneSatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "}\n",
                                "package org.eclipse.collections.api.block.predicate;\n" +
                                "public interface Predicate<T> {\n" +
                                "    boolean accept(T each);\n" +
                                "}\n"
                            )
                    ),
                java(
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        return !list.noneSatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n",
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        return list.anySatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void replacesNegatedAnySatisfyWithNoneSatisfy() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion()
                            .dependsOn(
                                "package org.eclipse.collections.api.list;\n" +
                                "public interface MutableList<T> extends org.eclipse.collections.api.RichIterable<T> {\n" +
                                "    boolean anySatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "    boolean noneSatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "}\n",
                                "package org.eclipse.collections.api;\n" +
                                "public interface RichIterable<T> {\n" +
                                "    boolean anySatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "    boolean noneSatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super T> predicate);\n" +
                                "}\n",
                                "package org.eclipse.collections.api.block.predicate;\n" +
                                "public interface Predicate<T> {\n" +
                                "    boolean accept(T each);\n" +
                                "}\n"
                            )
                    ),
                java(
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        return !list.anySatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n",
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        return list.noneSatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void replacesNegatedSatisfiesOnPrimitiveIterable() {
        this.rewriteRun(
                java(
                    "import org.eclipse.collections.api.list.primitive.MutableIntList;\n" +
                    "import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableIntList list, IntPredicate predicate) {\n" +
                    "        return !list.noneSatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n",
                    "import org.eclipse.collections.api.list.primitive.MutableIntList;\n" +
                    "import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableIntList list, IntPredicate predicate) {\n" +
                    "        return list.anySatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void replacesNegatedSatisfiesOnMultimap() {
        this.rewriteRun(
                spec ->
                    spec.parser(
                        JavaParser.fromJavaVersion()
                            .dependsOn(
                                "package org.eclipse.collections.api.multimap;\n" +
                                "public interface MutableMultimap<K, V> {\n" +
                                "    boolean anySatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super V> predicate);\n" +
                                "    boolean noneSatisfy(org.eclipse.collections.api.block.predicate.Predicate<? super V> predicate);\n" +
                                "}\n",
                                "package org.eclipse.collections.api.block.predicate;\n" +
                                "public interface Predicate<T> {\n" +
                                "    boolean accept(T each);\n" +
                                "}\n"
                            )
                    ),
                java(
                    "import org.eclipse.collections.api.multimap.MutableMultimap;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableMultimap<String, String> multimap, Predicate<String> predicate) {\n" +
                    "        return !multimap.noneSatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n",
                    "import org.eclipse.collections.api.multimap.MutableMultimap;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableMultimap<String, String> multimap, Predicate<String> predicate) {\n" +
                    "        return multimap.anySatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void doesNotChangeNonNegatedNoneSatisfy() {
        this.rewriteRun(
                java(
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        return list.noneSatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void doesNotChangeNonNegatedAnySatisfy() {
        this.rewriteRun(
                java(
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        return list.anySatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void doesNotChangeJavaUtilCollections() {
        this.rewriteRun(
                java(
                    "import java.util.List;\n" +
                    "import java.util.function.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(List<String> list, Predicate<String> predicate) {\n" +
                    "        return !list.stream().noneMatch(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void handlesComplexExpressions() {
        this.rewriteRun(
                java(
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list1, MutableList<String> list2, Predicate<String> pred) {\n" +
                    "        return !list1.noneSatisfy(pred) && !list2.anySatisfy(pred);\n" +
                    "    }\n" +
                    "}\n",
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list1, MutableList<String> list2, Predicate<String> pred) {\n" +
                    "        return list1.anySatisfy(pred) && list2.noneSatisfy(pred);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }

    @Test
    void preservesWhitespaceAndComments() {
        this.rewriteRun(
                java(
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        // Check if any elements satisfy the predicate\n" +
                    "        return !list.noneSatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n",
                    "import org.eclipse.collections.api.list.MutableList;\n" +
                    "import org.eclipse.collections.api.block.predicate.Predicate;\n" +
                    "\n" +
                    "class Test {\n" +
                    "    boolean test(MutableList<String> list, Predicate<String> predicate) {\n" +
                    "        // Check if any elements satisfy the predicate\n" +
                    "        return list.anySatisfy(predicate);\n" +
                    "    }\n" +
                    "}\n"
                )
            );
    }
}
