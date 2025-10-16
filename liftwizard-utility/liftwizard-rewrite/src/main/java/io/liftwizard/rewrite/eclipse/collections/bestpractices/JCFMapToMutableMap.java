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
import java.util.UUID;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

public class JCFMapToMutableMap extends Recipe {

    private static final String JAVA_UTIL_MAP = "java.util.Map";
    private static final String MUTABLE_MAP = "org.eclipse.collections.api.map.MutableMap";

    @Override
    public String getDisplayName() {
        return "`Map<K,V>` → `MutableMap<K,V>`";
    }

    @Override
    public String getDescription() {
        return "Replace `java.util.Map<K, V>` with `org.eclipse.collections.api.map.MutableMap<K, V>` when the variable is initialized with a MutableMap.";
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
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.VariableDeclarations visitVariableDeclarations(
                J.VariableDeclarations multiVariable,
                ExecutionContext ctx
            ) {
                J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);

                // Check if this is a java.util.Map type
                if (vd.getTypeExpression() == null || !this.isJavaUtilMap(vd.getTypeExpression())) {
                    return vd;
                }

                // Check all variables in the declaration
                for (J.VariableDeclarations.NamedVariable variable : vd.getVariables()) {
                    if (variable.getInitializer() == null) {
                        continue;
                    }

                    JavaType initializerType = variable.getInitializer().getType();
                    if (initializerType == null) {
                        continue;
                    }

                    // Check if the initializer returns a MutableMap type
                    if (!TypeUtils.isAssignableTo(MUTABLE_MAP, initializerType)) {
                        continue;
                    }

                    // Replace the type expression with MutableMap
                    TypeTree typeExpr = vd.getTypeExpression();
                    TypeTree newTypeExpr = null;

                    if (typeExpr instanceof J.Identifier ident) {
                        // Simple case: Map variable = ...
                        newTypeExpr = ident.withSimpleName("MutableMap").withType(JavaType.buildType(MUTABLE_MAP));
                        this.maybeAddImport(MUTABLE_MAP);
                    } else if (typeExpr instanceof J.ParameterizedType paramType) {
                        // Parameterized case: Map<K, V> variable = ...
                        J clazz = paramType.getClazz();

                        if (clazz instanceof J.Identifier ident) {
                            // Map<K, V>
                            J.Identifier newClazz = ident
                                .withSimpleName("MutableMap")
                                .withType(JavaType.buildType(MUTABLE_MAP));
                            newTypeExpr = paramType.withClazz(newClazz);
                            this.maybeAddImport(MUTABLE_MAP);
                        } else if (clazz instanceof J.FieldAccess) {
                            // java.util.Map<K, V>
                            // Replace with just MutableMap<K, V> and add import
                            J.Identifier mutableMapIdent = new J.Identifier(
                                UUID.randomUUID(),
                                clazz.getPrefix(),
                                clazz.getMarkers(),
                                "MutableMap",
                                JavaType.buildType(MUTABLE_MAP),
                                null
                            );
                            newTypeExpr = paramType.withClazz(mutableMapIdent);
                            this.maybeAddImport(MUTABLE_MAP);
                        }
                    } else if (typeExpr instanceof J.FieldAccess) {
                        // Fully qualified without generics: java.util.Map variable = ...
                        J.Identifier mutableMapIdent = new J.Identifier(
                            UUID.randomUUID(),
                            typeExpr.getPrefix(),
                            typeExpr.getMarkers(),
                            "MutableMap",
                            JavaType.buildType(MUTABLE_MAP),
                            null
                        );
                        newTypeExpr = mutableMapIdent;
                        this.maybeAddImport(MUTABLE_MAP);
                    }

                    if (newTypeExpr != null) {
                        return vd.withTypeExpression(newTypeExpr);
                    }

                    return vd;
                }

                return vd;
            }

            private boolean isJavaUtilMap(J typeExpression) {
                if (typeExpression instanceof J.Identifier identifier) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
                    return type != null && JAVA_UTIL_MAP.equals(type.getFullyQualifiedName());
                } else if (typeExpression instanceof J.ParameterizedType parameterizedType) {
                    return this.isJavaUtilMap(parameterizedType.getClazz());
                } else if (typeExpression instanceof J.FieldAccess fieldAccess) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
                    return type != null && JAVA_UTIL_MAP.equals(type.getFullyQualifiedName());
                }
                return false;
            }
        };
    }
}
