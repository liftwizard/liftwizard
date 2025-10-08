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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class ECSetConstructorToFactory extends Recipe {

    @Override
    public String getDisplayName() {
        return "`new UnifiedSet<>()` → `Sets.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new UnifiedSet<>()` with `Sets.mutable.<T>empty()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesType<>("org.eclipse.collections.impl.set.mutable.UnifiedSet", false),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                    J.NewClass n = (J.NewClass) super.visitNewClass(newClass, ctx);

                    if (n.getClazz() == null || n.getArguments().size() != 1) {
                        return n;
                    }

                    Expression argument = n.getArguments().get(0);
                    if (!(argument instanceof J.Empty)) {
                        return n;
                    }

                    JavaType.FullyQualified type = TypeUtils.asFullyQualified(n.getClazz().getType());
                    if (type == null) {
                        return n;
                    }

                    String fullyQualifiedName = type.getFullyQualifiedName();
                    if (!"org.eclipse.collections.impl.set.mutable.UnifiedSet".equals(fullyQualifiedName)) {
                        return n;
                    }

                    // Handle diamond operator
                    if (n.getClazz() instanceof J.ParameterizedType) {
                        J.ParameterizedType parameterizedType = (J.ParameterizedType) n.getClazz();
                        if (parameterizedType.getTypeParameters() != null) {
                            if (
                                parameterizedType.getTypeParameters().isEmpty() ||
                                (parameterizedType.getTypeParameters().size() == 1 &&
                                    parameterizedType.getTypeParameters().get(0) instanceof J.Empty)
                            ) {
                                // Diamond operator case
                                JavaTemplate template = JavaTemplate.builder("Sets.mutable.empty()")
                                    .contextSensitive()
                                    .imports("org.eclipse.collections.api.factory.set.Sets")
                                    .javaParser(
                                        JavaParser.fromJavaVersion()
                                            .classpath("eclipse-collections-api")
                                            .dependsOn(
                                                "package org.eclipse.collections.api.factory.set;\n" +
                                                "public class Sets {\n" +
                                                "    public static org.eclipse.collections.api.factory.set.MutableSetFactory mutable = null;\n" +
                                                "}\n" +
                                                "interface MutableSetFactory {\n" +
                                                "    <T> org.eclipse.collections.api.set.MutableSet<T> empty();\n" +
                                                "}"
                                            )
                                    )
                                    .build();

                                this.maybeAddImport("org.eclipse.collections.api.factory.set.Sets");
                                this.maybeRemoveImport("org.eclipse.collections.impl.set.mutable.UnifiedSet");

                                return template.apply(this.getCursor(), n.getCoordinates().replace());
                            }
                        }
                    }

                    // Non-parameterized case
                    JavaTemplate template = JavaTemplate.builder("Sets.mutable.empty()")
                        .contextSensitive()
                        .imports("org.eclipse.collections.api.factory.set.Sets")
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    "package org.eclipse.collections.api.factory.set;\n" +
                                    "public class Sets {\n" +
                                    "    public static org.eclipse.collections.api.factory.set.MutableSetFactory mutable = null;\n" +
                                    "}\n" +
                                    "interface MutableSetFactory {\n" +
                                    "    org.eclipse.collections.api.set.MutableSet empty();\n" +
                                    "}"
                                )
                        )
                        .build();

                    this.maybeAddImport("org.eclipse.collections.api.factory.set.Sets");
                    this.maybeRemoveImport("org.eclipse.collections.impl.set.mutable.UnifiedSet");

                    return template.apply(this.getCursor(), n.getCoordinates().replace());
                }
            }
        );
    }
}
