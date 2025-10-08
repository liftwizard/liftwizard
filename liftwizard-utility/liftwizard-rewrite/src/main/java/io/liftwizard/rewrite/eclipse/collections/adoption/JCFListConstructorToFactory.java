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
import java.util.List;
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
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class JCFListConstructorToFactory extends Recipe {

    @Override
    public String getDisplayName() {
        return "`new ArrayList<>()` → `Lists.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new ArrayList()` with `Lists.mutable.empty()`.";
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
        return new JCFListConstructorToFactoryJavaVisitor();
    }

    private static final class JCFListConstructorToFactoryJavaVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            // Check if this is an ArrayList constructor
            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            if (type == null || !"java.util.ArrayList".equals(type.getFullyQualifiedName())) {
                return nc;
            }

            // Check if constructor has real arguments (not just J.Empty)
            List<Expression> arguments = nc.getArguments();
            if (
                arguments != null &&
                !arguments.isEmpty() &&
                (arguments.size() != 1 || !(arguments.get(0) instanceof J.Empty))
            ) {
                // Has real arguments, skip
                return nc;
            }
            // This is an empty constructor, we can transform it

            this.maybeAddImport("org.eclipse.collections.api.factory.Lists");
            this.maybeRemoveImport("java.util.ArrayList");

            JavaParser.Builder<? extends JavaParser, ?> javaParser = JavaParser.fromJavaVersion()
                .classpath("eclipse-collections-api")
                .dependsOn(
                    """
                    package org.eclipse.collections.api.factory;
                    public enum Lists {
                        ;
                        public static final MutableListFactory mutable = null;
                        public static final class MutableListFactory {
                            public <T> T empty() { return null; }
                        }
                    }"""
                );

            JavaTemplate nonGenericTemplate = JavaTemplate.builder("Lists.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.Lists")
                .contextSensitive()
                .javaParser(javaParser)
                .build();

            if (
                !(nc.getClazz() instanceof J.ParameterizedType paramType) ||
                paramType.getTypeParameters() == null ||
                paramType.getTypeParameters().isEmpty()
            ) {
                // Non-parameterized ArrayList: new ArrayList()
                return nonGenericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            // Ensure imports are properly ordered
            this.doAfterVisit(new OrderImports(false).getVisitor());

            // Check if this is a diamond operator by looking at the type parameters
            boolean isDiamondOperator =
                paramType.getTypeParameters().size() == 1 && paramType.getTypeParameters().get(0) instanceof J.Empty;

            if (isDiamondOperator) {
                // Diamond operator: new ArrayList<>()
                return nonGenericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            // Explicit type parameters: new ArrayList<String>()
            // Extract the explicit type parameters from the source
            String typeParams = paramType
                .getTypeParameters()
                .stream()
                .map(tp ->
                    // Get the source representation of the type parameter
                    tp.print(this.getCursor()))
                .collect(Collectors.joining(", "));

            JavaTemplate genericTemplate = JavaTemplate.builder("Lists.mutable.<" + typeParams + ">empty()")
                .imports("org.eclipse.collections.api.factory.Lists")
                .contextSensitive()
                .javaParser(javaParser)
                .build();

            return genericTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
        }
    }
}
