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

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class ECArraysAsListToWith extends Recipe {

    private static final String LISTS_FACTORY = "org.eclipse.collections.api.factory.Lists";
    private static final String SETS_FACTORY = "org.eclipse.collections.api.factory.Sets";
    private static final String BAGS_FACTORY = "org.eclipse.collections.api.factory.Bags";

    private static final MethodMatcher FAST_LIST_NEW_LIST = new MethodMatcher(
        "org.eclipse.collections.impl.list.mutable.FastList newList(java.util.Collection)"
    );
    private static final MethodMatcher UNIFIED_SET_NEW_SET = new MethodMatcher(
        "org.eclipse.collections.impl.set.mutable.UnifiedSet newSet(java.util.Collection)"
    );
    private static final MethodMatcher HASH_BAG_NEW_BAG = new MethodMatcher(
        "org.eclipse.collections.impl.bag.mutable.HashBag newBag(java.util.Collection)"
    );
    private static final MethodMatcher ARRAYS_AS_LIST = new MethodMatcher("java.util.Arrays asList(..)");

    @Override
    public String getDisplayName() {
        return "`FastList.newList(Arrays.asList())` → `Lists.mutable.with()`";
    }

    @Override
    public String getDescription() {
        return "Replace `FastList.newList(Arrays.asList())`, `UnifiedSet.newSet(Arrays.asList())`, and `HashBag.newBag(Arrays.asList())` with Eclipse Collections factory methods using varargs.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(10);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

                if (mi.getArguments().size() != 1) {
                    return mi;
                }

                // Check if the argument is Arrays.asList()
                if (!(mi.getArguments().get(0) instanceof J.MethodInvocation arraysAsList)) {
                    return mi;
                }

                if (!ARRAYS_AS_LIST.matches(arraysAsList)) {
                    return mi;
                }

                if (FAST_LIST_NEW_LIST.matches(mi)) {
                    this.maybeAddImport(LISTS_FACTORY);
                    this.maybeRemoveImport("org.eclipse.collections.impl.list.mutable.FastList");
                    this.maybeRemoveImport("java.util.Arrays");

                    // Build template with correct number of parameters
                    String templatePattern = this.buildTemplatePattern(
                        "Lists.mutable.with",
                        arraysAsList.getArguments().size()
                    );
                    JavaTemplate template = JavaTemplate.builder(templatePattern)
                        .imports(LISTS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.list;
                                    public interface MutableList<T> extends java.util.List<T> {
                                    }""",
                                    """
                                    package org.eclipse.collections.api.factory;
                                    import org.eclipse.collections.api.list.MutableList;
                                    public final class Lists {
                                        public static final MutableListFactory mutable = null;
                                        public static final class MutableListFactory {
                                            public <T> MutableList<T> with(T... elements) { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(
                        this.getCursor(),
                        mi.getCoordinates().replace(),
                        arraysAsList.getArguments().toArray()
                    );
                }

                if (UNIFIED_SET_NEW_SET.matches(mi)) {
                    this.maybeAddImport(SETS_FACTORY);
                    this.maybeRemoveImport("org.eclipse.collections.impl.set.mutable.UnifiedSet");
                    this.maybeRemoveImport("java.util.Arrays");

                    // Build template with correct number of parameters
                    String templatePattern = this.buildTemplatePattern(
                        "Sets.mutable.with",
                        arraysAsList.getArguments().size()
                    );
                    JavaTemplate template = JavaTemplate.builder(templatePattern)
                        .imports(SETS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.set;
                                    public interface MutableSet<T> extends java.util.Set<T> {
                                    }""",
                                    """
                                    package org.eclipse.collections.api.factory;
                                    import org.eclipse.collections.api.set.MutableSet;
                                    public final class Sets {
                                        public static final MutableSetFactory mutable = null;
                                        public static final class MutableSetFactory {
                                            public <T> MutableSet<T> with(T... elements) { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(
                        this.getCursor(),
                        mi.getCoordinates().replace(),
                        arraysAsList.getArguments().toArray()
                    );
                }

                if (HASH_BAG_NEW_BAG.matches(mi)) {
                    this.maybeAddImport(BAGS_FACTORY);
                    this.maybeRemoveImport("org.eclipse.collections.impl.bag.mutable.HashBag");
                    this.maybeRemoveImport("java.util.Arrays");

                    // Build template with correct number of parameters
                    String templatePattern = this.buildTemplatePattern(
                        "Bags.mutable.with",
                        arraysAsList.getArguments().size()
                    );
                    JavaTemplate template = JavaTemplate.builder(templatePattern)
                        .imports(BAGS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    import org.eclipse.collections.api.bag.MutableBag;
                                    public final class Bags {
                                        public static final MutableBagFactory mutable = null;
                                        public static final class MutableBagFactory {
                                            public <T> MutableBag<T> with(T... elements) { return null; }
                                        }
                                    }""",
                                    """
                                    package org.eclipse.collections.api.bag;
                                    public interface MutableBag<T> extends java.util.Collection<T> {
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(
                        this.getCursor(),
                        mi.getCoordinates().replace(),
                        arraysAsList.getArguments().toArray()
                    );
                }

                return mi;
            }

            private String buildTemplatePattern(String methodPrefix, int argCount) {
                if (argCount == 0) {
                    return methodPrefix + "()";
                }
                StringBuilder pattern = new StringBuilder(methodPrefix).append("(");
                for (int i = 0; i < argCount; i++) {
                    if (i > 0) {
                        pattern.append(", ");
                    }
                    pattern.append("#{any()}");
                }
                pattern.append(")");
                return pattern.toString();
            }
        };
    }
}
