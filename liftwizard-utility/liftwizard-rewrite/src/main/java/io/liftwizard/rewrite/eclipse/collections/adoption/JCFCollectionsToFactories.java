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

public class JCFCollectionsToFactories extends Recipe {

    private static final String LISTS_FACTORY = "org.eclipse.collections.api.factory.Lists";
    private static final String SETS_FACTORY = "org.eclipse.collections.api.factory.Sets";
    private static final String MAPS_FACTORY = "org.eclipse.collections.api.factory.Maps";

    private static final MethodMatcher COLLECTIONS_EMPTY_LIST = new MethodMatcher("java.util.Collections emptyList()");
    private static final MethodMatcher COLLECTIONS_EMPTY_SET = new MethodMatcher("java.util.Collections emptySet()");
    private static final MethodMatcher COLLECTIONS_EMPTY_MAP = new MethodMatcher("java.util.Collections emptyMap()");
    private static final MethodMatcher COLLECTIONS_SINGLETON_LIST = new MethodMatcher(
        "java.util.Collections singletonList(..)"
    );
    private static final MethodMatcher COLLECTIONS_SINGLETON = new MethodMatcher("java.util.Collections singleton(..)");
    private static final MethodMatcher COLLECTIONS_SINGLETON_MAP = new MethodMatcher(
        "java.util.Collections singletonMap(..)"
    );

    @Override
    public String getDisplayName() {
        return "`Collections.emptyList()` → `Lists.fixedSize.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `Collections.emptyList()`, `Collections.emptySet()`, `Collections.emptyMap()`, `Collections.singletonList()`, `Collections.singleton()`, and `Collections.singletonMap()` with Eclipse Collections factory methods.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(20);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);

                if (COLLECTIONS_EMPTY_LIST.matches(mi)) {
                    this.maybeAddImport(LISTS_FACTORY);
                    this.maybeRemoveImport("java.util.Collections");
                    JavaTemplate template = JavaTemplate.builder("Lists.fixedSize.empty()")
                        .imports(LISTS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    public final class Lists {
                                        public static final FixedSizeListFactory fixedSize = null;
                                        public static final class FixedSizeListFactory {
                                            public <T> java.util.List<T> empty() { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(this.getCursor(), mi.getCoordinates().replace());
                }

                if (COLLECTIONS_EMPTY_SET.matches(mi)) {
                    this.maybeAddImport(SETS_FACTORY);
                    this.maybeRemoveImport("java.util.Collections");
                    JavaTemplate template = JavaTemplate.builder("Sets.fixedSize.empty()")
                        .imports(SETS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    public final class Sets {
                                        public static final FixedSizeSetFactory fixedSize = null;
                                        public static final class FixedSizeSetFactory {
                                            public <T> java.util.Set<T> empty() { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(this.getCursor(), mi.getCoordinates().replace());
                }

                if (COLLECTIONS_EMPTY_MAP.matches(mi)) {
                    this.maybeAddImport(MAPS_FACTORY);
                    this.maybeRemoveImport("java.util.Collections");
                    JavaTemplate template = JavaTemplate.builder("Maps.fixedSize.empty()")
                        .imports(MAPS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    public final class Maps {
                                        public static final FixedSizeMapFactory fixedSize = null;
                                        public static final class FixedSizeMapFactory {
                                            public <K, V> java.util.Map<K, V> empty() { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(this.getCursor(), mi.getCoordinates().replace());
                }

                if (COLLECTIONS_SINGLETON_LIST.matches(mi) && mi.getArguments().size() == 1) {
                    this.maybeAddImport(LISTS_FACTORY);
                    this.maybeRemoveImport("java.util.Collections");
                    JavaTemplate template = JavaTemplate.builder("Lists.fixedSize.of(#{any()})")
                        .imports(LISTS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    public final class Lists {
                                        public static final FixedSizeListFactory fixedSize = null;
                                        public static final class FixedSizeListFactory {
                                            public <T> java.util.List<T> of(T element) { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(this.getCursor(), mi.getCoordinates().replace(), mi.getArguments().get(0));
                }

                if (COLLECTIONS_SINGLETON.matches(mi) && mi.getArguments().size() == 1) {
                    this.maybeAddImport(SETS_FACTORY);
                    this.maybeRemoveImport("java.util.Collections");
                    JavaTemplate template = JavaTemplate.builder("Sets.fixedSize.of(#{any()})")
                        .imports(SETS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    public final class Sets {
                                        public static final FixedSizeSetFactory fixedSize = null;
                                        public static final class FixedSizeSetFactory {
                                            public <T> java.util.Set<T> of(T element) { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(this.getCursor(), mi.getCoordinates().replace(), mi.getArguments().get(0));
                }

                if (COLLECTIONS_SINGLETON_MAP.matches(mi) && mi.getArguments().size() == 2) {
                    this.maybeAddImport(MAPS_FACTORY);
                    this.maybeRemoveImport("java.util.Collections");
                    JavaTemplate template = JavaTemplate.builder("Maps.fixedSize.of(#{any()}, #{any()})")
                        .imports(MAPS_FACTORY)
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.factory;
                                    public final class Maps {
                                        public static final FixedSizeMapFactory fixedSize = null;
                                        public static final class FixedSizeMapFactory {
                                            public <K, V> java.util.Map<K, V> of(K key, V value) { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();
                    return template.apply(
                        this.getCursor(),
                        mi.getCoordinates().replace(),
                        mi.getArguments().get(0),
                        mi.getArguments().get(1)
                    );
                }

                return mi;
            }
        };
    }
}
