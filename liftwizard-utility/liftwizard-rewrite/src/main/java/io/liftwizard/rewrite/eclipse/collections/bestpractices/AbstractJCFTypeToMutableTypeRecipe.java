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
import java.util.Locale;
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

public abstract class AbstractJCFTypeToMutableTypeRecipe extends Recipe {

    private static final int ESTIMATED_EFFORT_SECONDS = 20;
    private static final String JAVA_UTIL_PACKAGE = "java.util.";
    private static final String EC_API_PACKAGE = "org.eclipse.collections.api.";

    private final String jcfTypeSimpleName;
    private final String mutableTypeSimpleName;

    protected AbstractJCFTypeToMutableTypeRecipe(final String jcfTypeSimpleName, final String mutableTypeSimpleName) {
        this.jcfTypeSimpleName = jcfTypeSimpleName;
        this.mutableTypeSimpleName = mutableTypeSimpleName;
    }

    protected final String getJavaUtilTypeFullyQualifiedName() {
        return JAVA_UTIL_PACKAGE + this.jcfTypeSimpleName;
    }

    protected final String getMutableTypeFullyQualifiedName() {
        return EC_API_PACKAGE + this.jcfTypeSimpleName.toLowerCase(Locale.ROOT) + "." + this.mutableTypeSimpleName;
    }

    protected final String getMutableTypeSimpleName() {
        return this.mutableTypeSimpleName;
    }

    @Override
    public final Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public final Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(ESTIMATED_EFFORT_SECONDS);
    }

    @Override
    public final TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.VariableDeclarations visitVariableDeclarations(
                final J.VariableDeclarations multiVariable,
                final ExecutionContext ctx
            ) {
                J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);

                if (vd.getTypeExpression() == null || !this.isJavaUtilType(vd.getTypeExpression())) {
                    return vd;
                }

                boolean shouldTransform = false;
                for (J.VariableDeclarations.NamedVariable variable : vd.getVariables()) {
                    if (variable.getInitializer() == null) {
                        continue;
                    }

                    JavaType initializerType = variable.getInitializer().getType();
                    if (initializerType == null) {
                        continue;
                    }

                    String mutableTypeName = AbstractJCFTypeToMutableTypeRecipe.this.getMutableTypeFullyQualifiedName();
                    if (TypeUtils.isAssignableTo(mutableTypeName, initializerType)) {
                        shouldTransform = true;
                        break;
                    }
                }

                if (!shouldTransform) {
                    return vd;
                }

                TypeTree typeExpr = vd.getTypeExpression();
                TypeTree newTypeExpr = null;

                String simpleName = AbstractJCFTypeToMutableTypeRecipe.this.getMutableTypeSimpleName();
                String fullyQualifiedName = AbstractJCFTypeToMutableTypeRecipe.this.getMutableTypeFullyQualifiedName();

                if (typeExpr instanceof J.Identifier) {
                    newTypeExpr = ((J.Identifier) typeExpr).withSimpleName(simpleName).withType(
                        JavaType.buildType(fullyQualifiedName)
                    );
                } else if (typeExpr instanceof J.ParameterizedType) {
                    J.ParameterizedType paramType = (J.ParameterizedType) typeExpr;
                    J clazz = paramType.getClazz();

                    if (clazz instanceof J.Identifier) {
                        J.Identifier newClazz = ((J.Identifier) clazz).withSimpleName(simpleName).withType(
                            JavaType.buildType(fullyQualifiedName)
                        );
                        newTypeExpr = paramType.withClazz(newClazz);
                    } else if (clazz instanceof J.FieldAccess) {
                        J.Identifier mutableTypeIdent = new J.Identifier(
                            UUID.randomUUID(),
                            clazz.getPrefix(),
                            clazz.getMarkers(),
                            simpleName,
                            JavaType.buildType(fullyQualifiedName),
                            null
                        );
                        newTypeExpr = paramType.withClazz(mutableTypeIdent);
                    }
                } else if (typeExpr instanceof J.FieldAccess) {
                    J.Identifier mutableTypeIdent = new J.Identifier(
                        UUID.randomUUID(),
                        typeExpr.getPrefix(),
                        typeExpr.getMarkers(),
                        simpleName,
                        JavaType.buildType(fullyQualifiedName),
                        null
                    );
                    newTypeExpr = mutableTypeIdent;
                }

                if (newTypeExpr != null) {
                    this.maybeAddImport(fullyQualifiedName);
                    this.doAfterVisit(new RemoveUnusedImports().getVisitor());
                    return vd.withTypeExpression(newTypeExpr);
                }

                return vd;
            }

            private boolean isJavaUtilType(final J typeExpression) {
                String javaUtilTypeName = AbstractJCFTypeToMutableTypeRecipe.this.getJavaUtilTypeFullyQualifiedName();
                if (typeExpression instanceof J.Identifier identifier) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
                    return type != null && javaUtilTypeName.equals(type.getFullyQualifiedName());
                }
                if (typeExpression instanceof J.ParameterizedType paramType) {
                    return this.isJavaUtilType(paramType.getClazz());
                }
                if (typeExpression instanceof J.FieldAccess fieldAccess) {
                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(fieldAccess.getType());
                    return type != null && javaUtilTypeName.equals(type.getFullyQualifiedName());
                }

                return false;
            }
        };
    }
}
