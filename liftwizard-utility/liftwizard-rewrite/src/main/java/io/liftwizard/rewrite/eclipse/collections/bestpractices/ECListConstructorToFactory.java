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

public class ECListConstructorToFactory extends Recipe {

    @Override
    public String getDisplayName() {
        return "`new FastList<>()` → `Lists.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new FastList()` constructor calls with `Lists.mutable.empty()`.";
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
        return new ECListConstructorToFactoryVisitor();
    }

    private static final class ECListConstructorToFactoryVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            final JavaTemplate listsEmptyTemplate = JavaTemplate.builder("Lists.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.Lists")
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
                .build();

            JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
            if (
                type == null ||
                !"org.eclipse.collections.impl.list.mutable.FastList".equals(type.getFullyQualifiedName())
            ) {
                return nc;
            }

            List<Expression> arguments = nc.getArguments();
            if (!arguments.isEmpty() && (arguments.size() != 1 || !(arguments.get(0) instanceof J.Empty))) {
                return nc;
            }

            this.maybeAddImport("org.eclipse.collections.api.factory.Lists");
            this.maybeRemoveImport("org.eclipse.collections.impl.list.mutable.FastList");

            if (
                !(nc.getClazz() instanceof J.ParameterizedType paramType) ||
                paramType.getTypeParameters() == null ||
                paramType.getTypeParameters().isEmpty()
            ) {
                return listsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            this.doAfterVisit(new OrderImports(false).getVisitor());

            boolean isDiamondOperator =
                paramType.getTypeParameters().size() == 1 && paramType.getTypeParameters().get(0) instanceof J.Empty;

            if (isDiamondOperator) {
                return listsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }

            String typeParametersString = paramType
                .getTypeParameters()
                .stream()
                .map(J.class::cast)
                .map(J::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

            JavaTemplate listsEmptyWithTypeParamsTemplate = JavaTemplate.builder(
                "Lists.mutable.<" + typeParametersString + ">empty()"
            )
                .imports("org.eclipse.collections.api.factory.Lists")
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
                .build();

            return listsEmptyWithTypeParamsTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
        }
    }
}
