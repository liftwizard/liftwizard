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

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.OrderImports;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.NewClass;
import org.openrewrite.java.tree.J.ParameterizedType;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public abstract class AbstractJCFConstructorToFactoryRecipe extends Recipe {

    private final String sourceTypeSimpleName;
    private final String targetFactorySimpleName;
    private final String factoryClassName;

    protected AbstractJCFConstructorToFactoryRecipe(
        String sourceTypeSimpleName,
        String targetFactorySimpleName,
        String factoryClassName
    ) {
        this.sourceTypeSimpleName = Objects.requireNonNull(sourceTypeSimpleName);
        this.targetFactorySimpleName = Objects.requireNonNull(targetFactorySimpleName);
        this.factoryClassName = Objects.requireNonNull(factoryClassName);
    }

    @Override
    public final Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public final Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(10);
    }

    @Override
    public final TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ConstructorToFactoryVisitor(
            this.sourceTypeSimpleName,
            this.targetFactorySimpleName,
            this.factoryClassName
        );
    }

    private static final class ConstructorToFactoryVisitor extends JavaVisitor<ExecutionContext> {

        private final String sourceTypeSimpleName;
        private final String targetFactorySimpleName;
        private final String factoryClassName;

        private ConstructorToFactoryVisitor(
            String sourceTypeSimpleName,
            String targetFactorySimpleName,
            String factoryClassName
        ) {
            this.sourceTypeSimpleName = Objects.requireNonNull(sourceTypeSimpleName);
            this.targetFactorySimpleName = Objects.requireNonNull(targetFactorySimpleName);
            this.factoryClassName = Objects.requireNonNull(factoryClassName);
        }

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            if (type == null || !("java.util." + this.sourceTypeSimpleName).equals(type.getFullyQualifiedName())) {
                return nc;
            }

            List<Expression> arguments = nc.getArguments();

            boolean isEmptyConstructor =
                arguments.isEmpty() || (arguments.size() == 1 && arguments.get(0) instanceof J.Empty);
            boolean isInitialCapacityConstructor =
                arguments.size() == 1 &&
                !(arguments.get(0) instanceof J.Empty) &&
                isNumericType(arguments.get(0).getType());
            boolean isComparatorConstructor =
                arguments.size() == 1 &&
                !(arguments.get(0) instanceof J.Empty) &&
                isComparatorType(arguments.get(0).getType());

            if (!isEmptyConstructor && !isInitialCapacityConstructor && !isComparatorConstructor) {
                return nc;
            }

            if (this.isVariableTypeConcreteClass(nc)) {
                return nc;
            }

            this.maybeRemoveImport("java.util." + this.sourceTypeSimpleName);
            this.maybeAddImport("org.eclipse.collections.api.factory." + this.targetFactorySimpleName);

            JavaParser.Builder<? extends JavaParser, ?> javaParser = this.getJavaParser();
            this.doAfterVisit(new OrderImports(false).getVisitor());

            ParameterizedType typeParamsSource = getTypeParamsSource(nc);
            String typeParamsTemplate = this.getTypeParamsTemplate(typeParamsSource);

            String templateSource = this.getTemplateSource(
                typeParamsTemplate,
                isInitialCapacityConstructor,
                isComparatorConstructor
            );
            JavaTemplate template = JavaTemplate.builder(templateSource)
                .imports("org.eclipse.collections.api.factory." + this.targetFactorySimpleName)
                .contextSensitive()
                .javaParser(javaParser)
                .build();

            if (isInitialCapacityConstructor || isComparatorConstructor) {
                return template.apply(this.getCursor(), nc.getCoordinates().replace(), arguments.get(0));
            } else {
                return template.apply(this.getCursor(), nc.getCoordinates().replace());
            }
        }

        private String getTemplateSource(
            String typeParamsTemplate,
            boolean isInitialCapacityConstructor,
            boolean isComparatorConstructor
        ) {
            if (isInitialCapacityConstructor) {
                return (
                    this.targetFactorySimpleName + ".mutable." + typeParamsTemplate + "withInitialCapacity(#{any(int)})"
                );
            }
            if (isComparatorConstructor) {
                return (
                    this.targetFactorySimpleName +
                    ".mutable." +
                    typeParamsTemplate +
                    "with(#{any(java.util.Comparator)})"
                );
            }
            return this.targetFactorySimpleName + ".mutable." + typeParamsTemplate + "empty()";
        }

        private static boolean isNumericType(JavaType type) {
            if (type instanceof JavaType.Primitive primitive) {
                return switch (primitive) {
                    case Int, Long, Short, Byte -> true;
                    default -> false;
                };
            }
            return false;
        }

        private static boolean isComparatorType(JavaType type) {
            JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(type);
            return fullyQualified != null && "java.util.Comparator".equals(fullyQualified.getFullyQualifiedName());
        }

        private boolean isVariableTypeConcreteClass(J.NewClass newClass) {
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
                        return ("java.util." + this.sourceTypeSimpleName).equals(variableTypeName);
                    }
                }
            }
            return false;
        }

        private JavaParser.Builder<? extends JavaParser, ?> getJavaParser() {
            String factoryTemplateCode = """
                package org.eclipse.collections.api.factory;
                import java.util.Comparator;
                public final class {0} '{'
                    public static final {1} mutable = null;
                    public static final class {1} '{'
                        public <T> T empty() '{ return null; }'
                        public <T> T withInitialCapacity(int capacity) '{ return null; }'
                        public <T> T with(Comparator<T> comparator) '{ return null; }'
                    '}'
                '}'""";
            String factoryStubCode = MessageFormat.format(
                factoryTemplateCode,
                this.targetFactorySimpleName,
                this.factoryClassName
            );
            JavaParser.Builder<? extends JavaParser, ?> javaParser = JavaParser.fromJavaVersion().dependsOn(
                factoryStubCode
            );
            return javaParser;
        }

        private static ParameterizedType getTypeParamsSource(NewClass nc) {
            if (!(nc.getClazz() instanceof ParameterizedType paramType)) {
                return null;
            }

            List<Expression> typeParameters = paramType.getTypeParameters();
            boolean hasTypeParameters = typeParameters != null && !typeParameters.isEmpty();
            boolean isDiamondOperator =
                hasTypeParameters && typeParameters.size() == 1 && typeParameters.get(0) instanceof J.Empty;
            return hasTypeParameters && !isDiamondOperator ? paramType : null;
        }

        private String getTypeParamsTemplate(ParameterizedType paramType) {
            if (paramType == null) {
                return "";
            }

            return paramType
                .getTypeParameters()
                .stream()
                .map(tp -> tp.print(this.getCursor()))
                .collect(Collectors.joining(", ", "<", ">"));
        }
    }
}
