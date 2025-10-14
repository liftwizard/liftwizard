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

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

public abstract class AbstractECConstructorToFactoryRecipe extends Recipe {

    private final String implementationClassName;
    private final String implementationPackagePath;
    private final String factoryClassName;
    private final String factoryPackageSuffix;
    private final String factoryMethod;

    protected AbstractECConstructorToFactoryRecipe(
        String implementationClassName,
        String implementationPackagePath,
        String factoryClassName,
        String factoryPackageSuffix,
        String factoryMethod
    ) {
        this.implementationClassName = Objects.requireNonNull(implementationClassName);
        this.implementationPackagePath = Objects.requireNonNull(implementationPackagePath);
        this.factoryClassName = Objects.requireNonNull(factoryClassName);
        this.factoryPackageSuffix = Objects.requireNonNull(factoryPackageSuffix);
        this.factoryMethod = Objects.requireNonNull(factoryMethod);
    }

    @Override
    public final Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public final Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(20);
    }

    @Override
    public final TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ECConstructorToFactoryVisitor(
            this.implementationClassName,
            this.implementationPackagePath,
            this.factoryClassName,
            this.factoryPackageSuffix,
            this.factoryMethod
        );
    }

    private static final class ECConstructorToFactoryVisitor extends JavaVisitor<ExecutionContext> {

        private final String implementationClassName;
        private final String implementationPackagePath;
        private final String factoryClassName;
        private final String factoryPackageSuffix;
        private final String factoryMethod;

        private ECConstructorToFactoryVisitor(
            String implementationClassName,
            String implementationPackagePath,
            String factoryClassName,
            String factoryPackageSuffix,
            String factoryMethod
        ) {
            this.implementationClassName = Objects.requireNonNull(implementationClassName);
            this.implementationPackagePath = Objects.requireNonNull(implementationPackagePath);
            this.factoryClassName = Objects.requireNonNull(factoryClassName);
            this.factoryPackageSuffix = Objects.requireNonNull(factoryPackageSuffix);
            this.factoryMethod = Objects.requireNonNull(factoryMethod);
        }

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            String implementationClass = MessageFormat.format(
                "org.eclipse.collections.impl.{0}.mutable.{1}",
                this.implementationPackagePath,
                this.implementationClassName
            );
            if (type == null || !implementationClass.equals(type.getFullyQualifiedName())) {
                return nc;
            }

            List<org.openrewrite.java.tree.Expression> arguments = nc.getArguments();

            boolean isEmptyConstructor =
                arguments.isEmpty() || (arguments.size() == 1 && arguments.get(0) instanceof J.Empty);
            boolean isInitialCapacityConstructor =
                arguments.size() == 1 &&
                !(arguments.get(0) instanceof J.Empty) &&
                this.isNumericType(arguments.get(0).getType());
            boolean isCollectionConstructor =
                arguments.size() == 1 &&
                !(arguments.get(0) instanceof J.Empty) &&
                !this.isNumericType(arguments.get(0).getType());

            if (!isEmptyConstructor && !isInitialCapacityConstructor && !isCollectionConstructor) {
                return nc;
            }

            if (this.isVariableTypeConcreteClass(nc)) {
                return nc;
            }

            String typeParams = this.extractTypeParameters(nc);
            if (typeParams == null) {
                return nc;
            }

            String prefixedFactoryPackageSuffix = this.factoryPackageSuffix.isEmpty()
                ? ""
                : "." + this.factoryPackageSuffix;
            String factoryPackage = "org.eclipse.collections.api.factory" + prefixedFactoryPackageSuffix;
            String factoryClass = factoryPackage + "." + this.factoryClassName;

            this.maybeAddImport(factoryClass);
            this.maybeRemoveImport(implementationClass);
            this.doAfterVisit(new OrderImports(false).getVisitor());

            String templateString = """
                package {0};
                import java.lang.Iterable;
                public class {1} '{'
                    public static final MutableFactory {2} = null;
                    public static final class MutableFactory '{'
                        public <T> T empty() '{' return null; '}'
                        public <T> T withInitialCapacity(int capacity) '{' return null; '}'
                        public <T> T withAll(Iterable<? extends T> items) '{' return null; '}'
                    '}'
                '}'""";
            String stubCode = MessageFormat.format(
                templateString,
                factoryPackage,
                this.factoryClassName,
                this.factoryMethod
            );

            String typeParamsTemplate = typeParams.isEmpty() ? "" : "<" + typeParams + ">";
            String templateSource;
            if (isInitialCapacityConstructor) {
                templateSource =
                    this.factoryClassName +
                    "." +
                    this.factoryMethod +
                    "." +
                    typeParamsTemplate +
                    "withInitialCapacity(#{any(int)})";
            } else if (isCollectionConstructor) {
                templateSource =
                    this.factoryClassName +
                    "." +
                    this.factoryMethod +
                    "." +
                    typeParamsTemplate +
                    "withAll(#{any(java.lang.Iterable)})";
            } else {
                templateSource =
                    this.factoryClassName + "." + this.factoryMethod + "." + typeParamsTemplate + "empty()";
            }

            JavaTemplate template = JavaTemplate.builder(templateSource)
                .imports(factoryClass)
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().dependsOn(stubCode))
                .build();

            if (isInitialCapacityConstructor || isCollectionConstructor) {
                return template.apply(this.getCursor(), nc.getCoordinates().replace(), arguments.get(0));
            } else {
                return template.apply(this.getCursor(), nc.getCoordinates().replace());
            }
        }

        private boolean isNumericType(JavaType type) {
            if (type instanceof JavaType.Primitive primitive) {
                return switch (primitive) {
                    case Int, Long, Short, Byte -> true;
                    default -> false;
                };
            }
            return false;
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
            if (javaType instanceof JavaType.GenericTypeVariable gtv) {
                return gtv.getName();
            }
            if (javaType instanceof JavaType.Variable var) {
                return var.getName();
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

        private boolean isVariableTypeConcreteClass(J.NewClass newClass) {
            String implementationClass = MessageFormat.format(
                "org.eclipse.collections.impl.{0}.mutable.{1}",
                this.implementationPackagePath,
                this.implementationClassName
            );

            if (
                this.getCursor().getParentTreeCursor().getValue() instanceof
                J.VariableDeclarations.NamedVariable namedVariable
            ) {
                if (
                    this.getCursor().getParentTreeCursor().getParentTreeCursor().getValue() instanceof
                    J.VariableDeclarations variableDecls
                ) {
                    JavaType.FullyQualified variableType = TypeUtils.asFullyQualified(variableDecls.getType());
                    if (variableType != null) {
                        String variableTypeName = variableType.getFullyQualifiedName();
                        return implementationClass.equals(variableTypeName);
                    }
                }
            }

            if (this.getCursor().getParentTreeCursor().getValue() instanceof J.Return returnStatement) {
                Cursor cursor = this.getCursor();
                while (cursor != null) {
                    Object value = cursor.getValue();
                    if (value instanceof J.MethodDeclaration method) {
                        if (method.getReturnTypeExpression() != null) {
                            JavaType.FullyQualified returnType = TypeUtils.asFullyQualified(
                                method.getReturnTypeExpression().getType()
                            );
                            if (returnType != null) {
                                String returnTypeName = returnType.getFullyQualifiedName();
                                return implementationClass.equals(returnTypeName);
                            }
                        }
                        break;
                    }
                    cursor = cursor.getParent();
                }
            }

            return false;
        }
    }
}
