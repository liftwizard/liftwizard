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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class JCFMapConstructorToFactory extends Recipe {

    private static final String HASH_MAP = "java.util.HashMap";
    private static final String TREE_MAP = "java.util.TreeMap";
    private static final String MAPS_FACTORY = "org.eclipse.collections.api.factory.Maps";
    private static final String SORTED_MAPS_FACTORY = "org.eclipse.collections.api.factory.SortedMaps";

    @Override
    public String getDisplayName() {
        return "`new HashMap<>()` → `Maps.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new HashMap()` and `new TreeMap()` constructor calls with `org.eclipse.collections.api.factory.Maps.mutable.empty()` and `org.eclipse.collections.api.factory.SortedMaps.mutable.empty()` respectively.";
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
            private final JavaTemplate mapsEmptyTemplate = JavaTemplate.builder("Maps.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.Maps")
                .contextSensitive()
                .javaParser(
                    JavaParser.fromJavaVersion()
                        .classpath("eclipse-collections-api")
                        .dependsOn(
                            """
                            package org.eclipse.collections.api.factory;
                            public enum Maps {
                                ;
                                public static final MutableMapFactory mutable = null;
                                public static final class MutableMapFactory {
                                    public <T> T empty() { return null; }
                                }
                            }
                            """
                        )
                )
                .build();

            private final JavaTemplate sortedMapsEmptyTemplate = JavaTemplate.builder("SortedMaps.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.SortedMaps")
                .contextSensitive()
                .javaParser(
                    JavaParser.fromJavaVersion()
                        .classpath("eclipse-collections-api")
                        .dependsOn(
                            """
                            package org.eclipse.collections.api.factory;
                            public enum SortedMaps {
                                ;
                                public static final MutableSortedMapFactory mutable = null;
                                public static final class MutableSortedMapFactory {
                                    public <T> T empty() { return null; }
                                }
                            }
                            """
                        )
                )
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                // Check if this is a HashMap or TreeMap constructor
                JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
                if (
                    type == null ||
                    (!HASH_MAP.equals(type.getFullyQualifiedName()) && !TREE_MAP.equals(type.getFullyQualifiedName()))
                ) {
                    return nc;
                }

                boolean isTreeMap = TREE_MAP.equals(type.getFullyQualifiedName());

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
                if (isTreeMap) {
                    this.maybeAddImport(SORTED_MAPS_FACTORY);
                    this.maybeRemoveImport(TREE_MAP);
                } else {
                    this.maybeAddImport(MAPS_FACTORY);
                    this.maybeRemoveImport(HASH_MAP);
                }

                // Check if it's a parameterized type
                if (nc.getClazz() instanceof J.ParameterizedType paramType) {
                    if (paramType.getTypeParameters() != null && !paramType.getTypeParameters().isEmpty()) {
                        // Check if this is a diamond operator by looking at the type parameters
                        boolean isDiamondOperator =
                            paramType.getTypeParameters().size() == 1 &&
                            paramType.getTypeParameters().get(0) instanceof J.Empty;

                        if (isDiamondOperator) {
                            // Diamond operator: new HashMap<>() or new TreeMap<>()
                            // Use the template without explicit type parameters
                            return isTreeMap
                                ? this.sortedMapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace())
                                : this.mapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                        }
                        // Explicit type parameters: new HashMap<String, Integer>() or new TreeMap<String, Integer>()
                        // For now, skip transforming constructors with explicit type parameters
                        // to avoid issues with complex generic types
                        return nc;
                    }
                }

                // Non-parameterized HashMap/TreeMap: new HashMap() or new TreeMap()
                return isTreeMap
                    ? this.sortedMapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace())
                    : this.mapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }
        };
    }
}
