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
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

// TODO 2025-06-10: Split for HashSet and TreeSet
public class JCFSetConstructorToFactory extends Recipe {

    private static final String HASH_SET = "java.util.HashSet";
    private static final String TREE_SET = "java.util.TreeSet";
    private static final String SETS_FACTORY = "org.eclipse.collections.api.factory.Sets";
    private static final String SORTED_SETS_FACTORY = "org.eclipse.collections.api.factory.SortedSets";

    @Override
    public String getDisplayName() {
        return "`new HashSet<>()` → `Sets.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new HashSet()` and `new TreeSet()` constructor calls with `org.eclipse.collections.api.factory.Sets.mutable.empty()` and `org.eclipse.collections.api.factory.SortedSets.mutable.empty()` respectively.";
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
        return new JavaVisitor<>() {
            private final JavaTemplate setsEmptyTemplate = JavaTemplate.builder("Sets.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.Sets")
                .contextSensitive()
                .javaParser(
                    JavaParser.fromJavaVersion()
                        .classpath("eclipse-collections-api")
                        .dependsOn(
                            """
                            package org.eclipse.collections.api.factory;
                            public final class Sets {
                                public static final MutableSetFactory mutable = null;
                                public static final class MutableSetFactory {
                                    public <T> T empty() { return null; }
                                }
                            }"""
                        )
                )
                .build();

            private final JavaTemplate sortedSetsEmptyTemplate = JavaTemplate.builder("SortedSets.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.SortedSets")
                .contextSensitive()
                .javaParser(
                    JavaParser.fromJavaVersion()
                        .classpath("eclipse-collections-api")
                        .dependsOn(
                            """
                            package org.eclipse.collections.api.factory;
                            public final class SortedSets {
                                public static final MutableSortedSetFactory mutable = null;
                                public static final class MutableSortedSetFactory {
                                    public <T> T empty() { return null; }
                                }
                            }"""
                        )
                )
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                // Check if this is a HashSet or TreeSet constructor
                JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
                if (
                    type == null ||
                    (!HASH_SET.equals(type.getFullyQualifiedName()) && !TREE_SET.equals(type.getFullyQualifiedName()))
                ) {
                    return nc;
                }

                boolean isTreeSet = TREE_SET.equals(type.getFullyQualifiedName());

                // Check if constructor has real arguments (not just J.Empty)
                if (nc.getArguments() != null && !nc.getArguments().isEmpty()) {
                    // Check if the only argument is an Empty (which means no real arguments)
                    if (nc.getArguments().size() == 1 && nc.getArguments().get(0) instanceof J.Empty) {
                        // This is an empty constructor, we can transform it
                    } else {
                        // Has real arguments, skip
                        return nc;
                    }
                }

                // Add appropriate import and remove the JCF import
                if (isTreeSet) {
                    this.maybeAddImport(SORTED_SETS_FACTORY);
                    this.maybeRemoveImport(TREE_SET);
                } else {
                    this.maybeAddImport(SETS_FACTORY);
                    this.maybeRemoveImport(HASH_SET);
                }

                // Check if it's a parameterized type
                if (nc.getClazz() instanceof J.ParameterizedType paramType) {
                    if (paramType.getTypeParameters() != null && !paramType.getTypeParameters().isEmpty()) {
                        // Check if this is a diamond operator by looking at the type parameters
                        boolean isDiamondOperator =
                            paramType.getTypeParameters().size() == 1 &&
                            paramType.getTypeParameters().get(0) instanceof J.Empty;

                        if (isDiamondOperator) {
                            // Diamond operator: new HashSet<>() or new TreeSet<>()
                            // Use the template without explicit type parameters
                            if (isTreeSet) {
                                return this.sortedSetsEmptyTemplate.apply(
                                    this.getCursor(),
                                    nc.getCoordinates().replace()
                                );
                            } else {
                                return this.setsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                            }
                        } else {
                            // Explicit type parameters: new HashSet<String>() or new TreeSet<String>()
                            // Extract the explicit type parameters from the source
                            String typeParams = paramType
                                .getTypeParameters()
                                .stream()
                                .map(tp -> {
                                    // Get the source representation of the type parameter
                                    return tp.print(this.getCursor());
                                })
                                .collect(Collectors.joining(", "));

                            String templatePrefix = isTreeSet ? "SortedSets" : "Sets";
                            String templateString = templatePrefix + ".mutable.<" + typeParams + ">empty()";

                            String factoryImport = isTreeSet
                                ? "org.eclipse.collections.api.factory.SortedSets"
                                : "org.eclipse.collections.api.factory.Sets";

                            JavaTemplate genericTemplate = JavaTemplate.builder(templateString)
                                .imports(factoryImport)
                                .contextSensitive()
                                .javaParser(
                                    JavaParser.fromJavaVersion()
                                        .classpath("eclipse-collections-api")
                                        .dependsOn(
                                            """
                                            package org.eclipse.collections.api.factory;
                                            public final class Sets {
                                                public static final MutableSetFactory mutable = null;
                                                public static final class MutableSetFactory {
                                                    public <T> T empty() { return null; }
                                                }
                                            }
                                            public final class SortedSets {
                                                public static final MutableSortedSetFactory mutable = null;
                                                public static final class MutableSortedSetFactory {
                                                    public <T> T empty() { return null; }
                                                }
                                            }"""
                                        )
                                )
                                .build();

                            return genericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                        }
                    }
                }

                // Non-parameterized HashSet/TreeSet: new HashSet() or new TreeSet()
                if (isTreeSet) {
                    return this.sortedSetsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                }

                return this.setsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }
        };
    }
}
