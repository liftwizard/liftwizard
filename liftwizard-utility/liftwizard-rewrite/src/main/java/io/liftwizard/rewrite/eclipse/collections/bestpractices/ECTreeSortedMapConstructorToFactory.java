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

public class ECTreeSortedMapConstructorToFactory extends Recipe {

    private static final String TREE_SORTED_MAP = "org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap";
    private static final String SORTED_MAPS_FACTORY = "org.eclipse.collections.api.factory.SortedMaps";

    @Override
    public String getDisplayName() {
        return "`new TreeSortedMap<>()` → `SortedMaps.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new TreeSortedMap()` constructor calls with `SortedMaps.mutable.empty()`.";
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
        return new TreeSortedMapConstructorVisitor();
    }

    private static final class TreeSortedMapConstructorVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            final JavaTemplate sortedMapsEmptyTemplate = JavaTemplate.builder("SortedMaps.mutable.empty()")
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

            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            if (type == null) {
                return nc;
            }

            if (!TREE_SORTED_MAP.equals(type.getFullyQualifiedName())) {
                return nc;
            }

            if (nc.getArguments() != null && !nc.getArguments().isEmpty()) {
                if (nc.getArguments().size() == 1 && nc.getArguments().get(0) instanceof J.Empty) {
                    // Empty constructor
                } else {
                    return nc;
                }
            }

            String typeParams = this.extractTypeParameters(nc);

            if (typeParams == null) {
                return nc;
            }

            this.maybeAddImport(SORTED_MAPS_FACTORY);
            this.maybeRemoveImport(TREE_SORTED_MAP);

            this.doAfterVisit(new OrderImports(false).getVisitor());

            if (!typeParams.isEmpty()) {
                JavaTemplate genericTemplate = JavaTemplate.builder("SortedMaps.mutable.<" + typeParams + ">empty()")
                    .imports(SORTED_MAPS_FACTORY)
                    .contextSensitive()
                    .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                    .build();

                return genericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            return sortedMapsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
        }

        private String extractTypeParameters(J.NewClass nc) {
            if (nc.getClazz() instanceof J.ParameterizedType paramType) {
                if (paramType.getTypeParameters() != null && !paramType.getTypeParameters().isEmpty()) {
                    boolean hasActualTypeParams = paramType
                        .getTypeParameters()
                        .stream()
                        .anyMatch(tp -> !(tp instanceof J.Empty));

                    if (hasActualTypeParams) {
                        return paramType
                            .getTypeParameters()
                            .stream()
                            .map(J.class::cast)
                            .map(J::toString)
                            .collect(Collectors.joining(", "));
                    }

                    return "";
                }
            }

            JavaType ncType = nc.getType();
            if (ncType instanceof JavaType.Parameterized) {
                return "";
            }

            return this.inferTypeParametersFromContext(nc);
        }

        private String inferTypeParametersFromContext(J.NewClass nc) {
            Cursor cursor = this.getCursor();
            while (cursor != null) {
                Object value = cursor.getValue();

                if (value instanceof J.VariableDeclarations.NamedVariable namedVar) {
                    JavaType varType = namedVar.getType();
                    if (varType instanceof JavaType.Parameterized paramType) {
                        if (!paramType.getTypeParameters().isEmpty()) {
                            return this.buildTypeParameterString(paramType.getTypeParameters());
                        }
                    }
                    return "";
                }

                if (value instanceof J.MethodDeclaration method) {
                    if (method.getReturnTypeExpression() != null) {
                        JavaType returnType = method.getReturnTypeExpression().getType();
                        if (returnType instanceof JavaType.Parameterized paramType) {
                            if (!paramType.getTypeParameters().isEmpty()) {
                                return this.buildTypeParameterString(paramType.getTypeParameters());
                            }
                        }
                        return "";
                    }
                }

                if (value instanceof J.VariableDeclarations varDecls) {
                    if (varDecls.getTypeExpression() != null) {
                        JavaType fieldType = varDecls.getTypeExpression().getType();
                        if (fieldType instanceof JavaType.Parameterized paramType) {
                            if (!paramType.getTypeParameters().isEmpty()) {
                                return this.buildTypeParameterString(paramType.getTypeParameters());
                            }
                        }
                        return "";
                    }
                }

                cursor = cursor.getParent();
            }

            return "";
        }

        private String buildTypeParameterString(List<JavaType> typeParameters) {
            return typeParameters.stream().map(this::formatJavaType).collect(Collectors.joining(", "));
        }

        private String formatJavaType(JavaType javaType) {
            if (javaType instanceof JavaType.Parameterized pType) {
                String baseType = this.formatJavaType(pType.getType());
                if (!pType.getTypeParameters().isEmpty()) {
                    String params = this.buildTypeParameterString(pType.getTypeParameters());
                    return baseType + "<" + params + ">";
                }
                return baseType;
            }
            if (javaType instanceof JavaType.FullyQualified fq) {
                return fq.getClassName();
            }
            if (javaType instanceof JavaType.GenericTypeVariable) {
                return ((JavaType.GenericTypeVariable) javaType).getName();
            }
            if (javaType instanceof JavaType.Variable) {
                return ((JavaType.Variable) javaType).getName();
            }
            String typeStr = javaType.toString();
            if (typeStr.equals("Generic{?}")) {
                return "?";
            }
            if (typeStr.startsWith("Generic{") && typeStr.endsWith("}")) {
                return typeStr.substring(8, typeStr.length() - 1);
            }
            return typeStr;
        }
    }
}
