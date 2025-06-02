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
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

public class JCFSetToMutableSet extends Recipe {

    private static final String JAVA_UTIL_SET = "java.util.Set";
    private static final String MUTABLE_SET = "org.eclipse.collections.api.set.MutableSet";

    @Override
    public String getDisplayName() {
        return "`Set<T>` → `MutableSet<T>`";
    }

    @Override
    public String getDescription() {
        return "Replace `java.util.Set<T>` with `org.eclipse.collections.api.set.MutableSet<T>` when the variable is initialized with a MutableSet.";
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

                // Check if this is a java.util.Set type
                if (vd.getTypeExpression() == null || !this.isJavaUtilSet(vd.getTypeExpression())) {
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

                    // Check if the initializer returns a MutableSet type
                    if (!TypeUtils.isAssignableTo(MUTABLE_SET, initializerType)) {
                        continue;
                    }

                    // Replace the type expression with MutableSet
                    TypeTree typeExpr = vd.getTypeExpression();
                    TypeTree newTypeExpr = null;

                    if (typeExpr instanceof J.Identifier ident) {
                        // Simple case: Set variable = ...
                        newTypeExpr = ident.withSimpleName("MutableSet").withType(JavaType.buildType(MUTABLE_SET));
                        this.maybeAddImport(MUTABLE_SET);
                        this.maybeRemoveImport(JAVA_UTIL_SET);
                    } else if (typeExpr instanceof J.ParameterizedType paramType) {
                        // Parameterized case: Set<String> variable = ...
                        J clazz = paramType.getClazz();

                        if (clazz instanceof J.Identifier ident) {
                            // Set<String>
                            J.Identifier newClazz = ident
                                .withSimpleName("MutableSet")
                                .withType(JavaType.buildType(MUTABLE_SET));
                            newTypeExpr = paramType.withClazz(newClazz);
                            this.maybeAddImport(MUTABLE_SET);
                            this.maybeRemoveImport(JAVA_UTIL_SET);
                        } else if (clazz instanceof J.FieldAccess) {
                            // java.util.Set<String>
                            // Replace with just MutableSet<String> and add import
                            J.Identifier mutableSetIdent = new J.Identifier(
                                java.util.UUID.randomUUID(),
                                clazz.getPrefix(),
                                clazz.getMarkers(),
                                "MutableSet",
                                JavaType.buildType(MUTABLE_SET),
                                null
                            );
                            newTypeExpr = paramType.withClazz(mutableSetIdent);
                            this.maybeAddImport(MUTABLE_SET);
                        }
                    } else if (typeExpr instanceof J.FieldAccess) {
                        // Fully qualified without generics: java.util.Set variable = ...
                        J.Identifier mutableSetIdent = new J.Identifier(
                            java.util.UUID.randomUUID(),
                            typeExpr.getPrefix(),
                            typeExpr.getMarkers(),
                            "MutableSet",
                            JavaType.buildType(MUTABLE_SET),
                            null
                        );
                        newTypeExpr = mutableSetIdent;
                        this.maybeAddImport(MUTABLE_SET);
                    }

                    if (newTypeExpr != null) {
                        return vd.withTypeExpression(newTypeExpr);
                    }

                    return vd;
                }

                return vd;
            }

            private boolean isJavaUtilSet(J typeExpression) {
                if (typeExpression instanceof J.Identifier identifier) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
                    return type != null && JAVA_UTIL_SET.equals(type.getFullyQualifiedName());
                } else if (typeExpression instanceof J.ParameterizedType parameterizedType) {
                    return this.isJavaUtilSet(parameterizedType.getClazz());
                } else if (typeExpression instanceof J.FieldAccess fieldAccess) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
                    return type != null && JAVA_UTIL_SET.equals(type.getFullyQualifiedName());
                }
                return false;
            }
        };
    }
}
