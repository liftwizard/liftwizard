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
            if (!arguments.isEmpty() && (arguments.size() != 1 || !(arguments.get(0) instanceof J.Empty))) {
                return nc;
            }

            String sourceImport = "java.util." + this.sourceTypeSimpleName;
            String targetImport = "org.eclipse.collections.api.factory." + this.targetFactorySimpleName;
            this.maybeRemoveImport(sourceImport);
            this.maybeAddImport(targetImport);

            JavaParser.Builder<? extends JavaParser, ?> javaParser = this.getJavaParser();

            if (
                !(nc.getClazz() instanceof J.ParameterizedType paramType) ||
                paramType.getTypeParameters() == null ||
                paramType.getTypeParameters().isEmpty()
            ) {
                JavaTemplate nonGenericTemplate = this.getNonGenericTemplate(targetImport, javaParser);
                return nonGenericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            this.doAfterVisit(new OrderImports(false).getVisitor());

            boolean isDiamondOperator =
                paramType.getTypeParameters().size() == 1 && paramType.getTypeParameters().get(0) instanceof J.Empty;
            if (isDiamondOperator) {
                JavaTemplate nonGenericTemplate = this.getNonGenericTemplate(targetImport, javaParser);
                return nonGenericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            JavaTemplate genericTemplate = this.getGenericTemplate(paramType, targetImport, javaParser);
            return genericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
        }

        private JavaParser.Builder<? extends JavaParser, ?> getJavaParser() {
            String factoryTemplateCode = """
                package org.eclipse.collections.api.factory;
                public final class {0} '{'
                    public static final {1} mutable = null;
                    public static final class {1} '{'
                        public <T> T empty() '{ return null; }'
                    '}'
                '}'""";
            String factoryStubCode = MessageFormat.format(
                factoryTemplateCode,
                this.targetFactorySimpleName,
                this.factoryClassName
            );
            JavaParser.Builder<? extends JavaParser, ?> javaParser = JavaParser.fromJavaVersion()
                .classpath("eclipse-collections-api")
                .dependsOn(factoryStubCode);
            return javaParser;
        }

        private JavaTemplate getNonGenericTemplate(
            String targetImport,
            JavaParser.Builder<? extends JavaParser, ?> javaParser
        ) {
            JavaTemplate nonGenericTemplate = JavaTemplate.builder(this.targetFactorySimpleName + ".mutable.empty()")
                .imports(targetImport)
                .contextSensitive()
                .javaParser(javaParser)
                .build();
            return nonGenericTemplate;
        }

        private JavaTemplate getGenericTemplate(
            ParameterizedType paramType,
            String targetImport,
            JavaParser.Builder<? extends JavaParser, ?> javaParser
        ) {
            String typeParams = paramType
                .getTypeParameters()
                .stream()
                .map(tp -> tp.print(this.getCursor()))
                .collect(Collectors.joining(", "));

            JavaTemplate genericTemplate = JavaTemplate.builder(
                this.targetFactorySimpleName + ".mutable.<" + typeParams + ">empty()"
            )
                .imports(targetImport)
                .contextSensitive()
                .javaParser(javaParser)
                .build();
            return genericTemplate;
        }
    }
}
