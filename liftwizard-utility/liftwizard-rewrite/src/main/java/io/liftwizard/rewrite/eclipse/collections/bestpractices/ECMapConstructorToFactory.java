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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.OrderImports;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class ECMapConstructorToFactory extends Recipe {

    private static final String UNIFIED_MAP = "org.eclipse.collections.impl.map.mutable.UnifiedMap";
    private static final String TREE_SORTED_MAP = "org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap";
    private static final String MAPS_FACTORY = "org.eclipse.collections.api.factory.Maps";
    private static final String SORTED_MAPS_FACTORY = "org.eclipse.collections.api.factory.SortedMaps";

    @Override
    public String getDisplayName() {
        return "`new UnifiedMap<>()` → `Maps.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new UnifiedMap()` and `new TreeSortedMap()` constructor calls with `Maps.mutable.empty()` and `SortedMaps.mutable.empty()` respectively.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(5);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MapConstructorVisitor();
    }

    private static final class MapConstructorVisitor extends JavaVisitor<ExecutionContext> {

        private final JavaTemplate mapsEmptyTemplate = JavaTemplate.builder("Maps.mutable.empty()")
            .imports("org.eclipse.collections.api.factory.Maps")
            .contextSensitive()
            .javaParser(
                JavaParser.fromJavaVersion()
                    .classpath("eclipse-collections-api")
                    .dependsOn(
                        """
                        package org.eclipse.collections.api.factory;
                        import org.eclipse.collections.api.map.MutableMap;
                        public final class Maps {
                            public static final MutableMapFactory mutable = null;
                            public static final class MutableMapFactory {
                                public <K, V> MutableMap<K, V> empty() { return null; }
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.map;
                        public interface MutableMap<K, V> extends java.util.Map<K, V> {
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
                        import org.eclipse.collections.api.map.sorted.MutableSortedMap;
                        public final class SortedMaps {
                            public static final MutableSortedMapFactory mutable = null;
                            public static final class MutableSortedMapFactory {
                                public <K extends Comparable<? super K>, V> MutableSortedMap<K, V> empty() { return null; }
                            }
                        }
                        """,
                        """
                        package org.eclipse.collections.api.map.sorted;
                        public interface MutableSortedMap<K, V> extends java.util.SortedMap<K, V> {
                        }
                        """
                    )
            )
            .build();

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            // Check if this is a UnifiedMap or TreeSortedMap constructor
            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            if (type == null) {
                return nc;
            }

            boolean isUnifiedMap = UNIFIED_MAP.equals(type.getFullyQualifiedName());
            boolean isTreeSortedMap = TREE_SORTED_MAP.equals(type.getFullyQualifiedName());

            if (!isUnifiedMap && !isTreeSortedMap) {
                return nc;
            }

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

            // Try to extract type parameters from various sources
            String typeParams = this.extractTypeParameters(nc);

            // If typeParams is null, this means we have explicit type parameters and should not transform
            if (typeParams == null) {
                return nc;
            }

            if (isTreeSortedMap) {
                // Add import for SortedMaps factory and remove the impl import
                this.maybeAddImport(SORTED_MAPS_FACTORY);
                this.maybeRemoveImport(TREE_SORTED_MAP);

                // Ensure imports are properly ordered
                this.doAfterVisit(new OrderImports(false).getVisitor());

                if (!typeParams.isEmpty()) {
                    JavaTemplate genericTemplate = JavaTemplate.builder(
                        "SortedMaps.mutable.<" + typeParams + ">empty()"
                    )
                        .imports(SORTED_MAPS_FACTORY)
                        .contextSensitive()
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();

                    return genericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                }

                // Diamond operator case or simple constructor - use no explicit generics
                return this.sortedMapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            } else {
                // UnifiedMap case
                // Add import for Maps factory and remove the impl import
                this.maybeAddImport(MAPS_FACTORY);
                this.maybeRemoveImport(UNIFIED_MAP);

                // Ensure imports are properly ordered
                this.doAfterVisit(new OrderImports(false).getVisitor());

                if (!typeParams.isEmpty()) {
                    JavaTemplate genericTemplate = JavaTemplate.builder("Maps.mutable.<" + typeParams + ">empty()")
                        .imports(MAPS_FACTORY)
                        .contextSensitive()
                        .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                        .build();

                    return genericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                }

                // Diamond operator case or simple constructor - use no explicit generics
                return this.mapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }
        }

        private String extractTypeParameters(J.NewClass nc) {
            // 1. First check if this constructor has explicit type parameters
            if (nc.getClazz() instanceof J.ParameterizedType) {
                J.ParameterizedType paramType = (J.ParameterizedType) nc.getClazz();
                if (paramType.getTypeParameters() != null && !paramType.getTypeParameters().isEmpty()) {
                    // Check if these are actual type parameters or just diamond operator
                    boolean hasActualTypeParams = paramType
                        .getTypeParameters()
                        .stream()
                        .anyMatch(tp -> !(tp instanceof J.Empty));

                    if (hasActualTypeParams) {
                        // Has explicit type parameters - should not transform
                        return null;
                    }

                    // Diamond operator case - don't add explicit generics
                    return "";
                }
            }

            // 2. Check if this is a raw type (no type parameters at all)
            JavaType ncType = nc.getType();
            if (ncType instanceof JavaType.Parameterized) {
                // Already parameterized, don't add generics
                return "";
            }

            // Raw type case - need to infer and add explicit generics
            return this.inferTypeParametersFromContext(nc);
        }

        private String inferTypeParametersFromContext(J.NewClass nc) {
            // Look for variable declaration that contains this constructor
            // Walk up the tree to find the variable declaration
            Cursor cursor = this.getCursor();
            while (cursor != null) {
                Object value = cursor.getValue();

                // Check if we're in a variable declaration
                if (value instanceof J.VariableDeclarations.NamedVariable) {
                    J.VariableDeclarations.NamedVariable namedVar = (J.VariableDeclarations.NamedVariable) value;
                    JavaType varType = namedVar.getType();
                    if (varType instanceof JavaType.Parameterized) {
                        JavaType.Parameterized paramType = (JavaType.Parameterized) varType;
                        if (!paramType.getTypeParameters().isEmpty()) {
                            return this.buildTypeParameterString(paramType.getTypeParameters());
                        }
                    }
                    // If the variable type is not parameterized, it's a raw type
                    // Don't add any generics
                    return "";
                }

                // Check if we're in a method declaration (return type)
                if (value instanceof J.MethodDeclaration) {
                    J.MethodDeclaration method = (J.MethodDeclaration) value;
                    if (method.getReturnTypeExpression() != null) {
                        JavaType returnType = method.getReturnTypeExpression().getType();
                        if (returnType instanceof JavaType.Parameterized) {
                            JavaType.Parameterized paramType = (JavaType.Parameterized) returnType;
                            if (!paramType.getTypeParameters().isEmpty()) {
                                return this.buildTypeParameterString(paramType.getTypeParameters());
                            }
                        }
                        // If the return type is not parameterized, it's a raw type
                        // Don't add any generics
                        return "";
                    }
                }

                // Check if we're in a field declaration
                if (value instanceof J.VariableDeclarations) {
                    J.VariableDeclarations varDecls = (J.VariableDeclarations) value;
                    if (varDecls.getTypeExpression() != null) {
                        JavaType fieldType = varDecls.getTypeExpression().getType();
                        if (fieldType instanceof JavaType.Parameterized) {
                            JavaType.Parameterized paramType = (JavaType.Parameterized) fieldType;
                            if (!paramType.getTypeParameters().isEmpty()) {
                                return this.buildTypeParameterString(paramType.getTypeParameters());
                            }
                        }
                        // If the field type is not parameterized, it's a raw type
                        // Don't add any generics
                        return "";
                    }
                }

                cursor = cursor.getParent();
            }

            // If no context found, don't add generics
            return "";
        }

        private String buildTypeParameterString(List<JavaType> typeParameters) {
            return typeParameters.stream().map(this::formatJavaType).collect(Collectors.joining(", "));
        }

        private String formatJavaType(JavaType javaType) {
            if (javaType instanceof JavaType.FullyQualified) {
                // Check if this is a raw type that should have parameters
                JavaType.FullyQualified fq = (JavaType.FullyQualified) javaType;
                return fq.getClassName();
            } else if (javaType instanceof JavaType.Parameterized) {
                // Handle nested generics recursively
                JavaType.Parameterized pType = (JavaType.Parameterized) javaType;
                String baseType = this.formatJavaType(pType.getType());
                if (!pType.getTypeParameters().isEmpty()) {
                    String params = this.buildTypeParameterString(pType.getTypeParameters());
                    return baseType + "<" + params + ">";
                }
                return baseType;
            } else if (javaType instanceof JavaType.GenericTypeVariable) {
                return ((JavaType.GenericTypeVariable) javaType).getName();
            } else if (javaType instanceof JavaType.Variable) {
                return ((JavaType.Variable) javaType).getName();
            } else if (javaType instanceof JavaType.Unknown) {
                return "?";
            } else {
                // Handle wildcards and other special types
                String typeStr = javaType.toString();
                // Convert Generic{?} to just ?
                if (typeStr.equals("Generic{?}")) {
                    return "?";
                }
                // Handle other Generic{...} patterns
                if (typeStr.startsWith("Generic{") && typeStr.endsWith("}")) {
                    return typeStr.substring(8, typeStr.length() - 1);
                }
                return typeStr;
            }
        }
    }
}
