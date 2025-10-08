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
import org.openrewrite.java.RemoveUnusedImports;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;
import org.openrewrite.java.tree.TypeUtils;

public class JCFListToMutableList extends Recipe {

    private static final String JAVA_UTIL_LIST = "java.util.List";
    private static final String MUTABLE_LIST = "org.eclipse.collections.api.list.MutableList";

    @Override
    public String getDisplayName() {
        return "`List<T>` → `MutableList<T>`";
    }

    @Override
    public String getDescription() {
        return "Replace `java.util.List<T>` with `org.eclipse.collections.api.list.MutableList<T>` when the variable is initialized with a MutableList.";
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

                // Check if this is a java.util.List type
                if (vd.getTypeExpression() == null || !this.isJavaUtilList(vd.getTypeExpression())) {
                    return vd;
                }

                // Check all variables in the declaration
                boolean shouldTransform = false;
                for (J.VariableDeclarations.NamedVariable variable : vd.getVariables()) {
                    if (variable.getInitializer() == null) {
                        continue;
                    }

                    JavaType initializerType = variable.getInitializer().getType();
                    if (initializerType == null) {
                        continue;
                    }

                    // Check if the initializer returns a MutableList type
                    if (TypeUtils.isAssignableTo(MUTABLE_LIST, initializerType)) {
                        shouldTransform = true;
                        break;
                    }
                }

                if (!shouldTransform) {
                    return vd;
                }

                // Replace the type expression with MutableList
                TypeTree typeExpr = vd.getTypeExpression();
                TypeTree newTypeExpr = null;

                if (typeExpr instanceof J.Identifier) {
                    // Simple case: List variable = ...
                    newTypeExpr = ((J.Identifier) typeExpr).withSimpleName("MutableList").withType(
                        JavaType.buildType(MUTABLE_LIST)
                    );
                } else if (typeExpr instanceof J.ParameterizedType) {
                    // Parameterized case: List<String> variable = ...
                    J.ParameterizedType paramType = (J.ParameterizedType) typeExpr;
                    J clazz = paramType.getClazz();

                    if (clazz instanceof J.Identifier) {
                        // List<String>
                        J.Identifier newClazz = ((J.Identifier) clazz).withSimpleName("MutableList").withType(
                            JavaType.buildType(MUTABLE_LIST)
                        );
                        newTypeExpr = paramType.withClazz(newClazz);
                    } else if (clazz instanceof J.FieldAccess) {
                        // java.util.List<String>
                        // Replace with just MutableList<String> and add import
                        J.Identifier mutableListIdent = new J.Identifier(
                            UUID.randomUUID(),
                            clazz.getPrefix(),
                            clazz.getMarkers(),
                            "MutableList",
                            JavaType.buildType(MUTABLE_LIST),
                            null
                        );
                        newTypeExpr = paramType.withClazz(mutableListIdent);
                    }
                } else if (typeExpr instanceof J.FieldAccess) {
                    // Fully qualified without generics: java.util.List variable = ...
                    J.Identifier mutableListIdent = new J.Identifier(
                        UUID.randomUUID(),
                        typeExpr.getPrefix(),
                        typeExpr.getMarkers(),
                        "MutableList",
                        JavaType.buildType(MUTABLE_LIST),
                        null
                    );
                    newTypeExpr = mutableListIdent;
                }

                if (newTypeExpr != null) {
                    this.maybeAddImport(MUTABLE_LIST);
                    this.doAfterVisit(new RemoveUnusedImports().getVisitor());
                    return vd.withTypeExpression(newTypeExpr);
                }

                return vd;
            }

            private boolean isJavaUtilList(J typeExpression) {
                if (typeExpression instanceof J.Identifier identifier) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
                    return type != null && JAVA_UTIL_LIST.equals(type.getFullyQualifiedName());
                }
                if (typeExpression instanceof J.ParameterizedType parameterizedType) {
                    return this.isJavaUtilList(parameterizedType.getClazz());
                }
                if (typeExpression instanceof J.FieldAccess fieldAccess) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
                    return type != null && JAVA_UTIL_LIST.equals(type.getFullyQualifiedName());
                }

                return false;
            }
        };
    }
}
