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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.OrderImports;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class ECListConstructorToFactory extends Recipe {

    private static final String FAST_LIST = "org.eclipse.collections.impl.list.mutable.FastList";
    private static final String LISTS_FACTORY = "org.eclipse.collections.api.factory.Lists";

    @Override
    public String getDisplayName() {
        return "`new FastList<>()` → `Lists.mutable.empty()`";
    }

    @Override
    public String getDescription() {
        return "Replace `new FastList()` constructor calls with `org.eclipse.collections.api.factory.Lists.mutable.empty()`.";
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
        return new JavaVisitor<ExecutionContext>() {
            private final JavaTemplate listsEmptyTemplate = JavaTemplate.builder("Lists.mutable.empty()")
                .imports("org.eclipse.collections.api.factory.Lists")
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections-api"))
                .build();

            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                // Check if this is a FastList constructor
                JavaType.FullyQualified type = TypeUtils.asFullyQualified(nc.getType());
                if (type == null || !FAST_LIST.equals(type.getFullyQualifiedName())) {
                    return nc;
                }

                // Check if constructor has real arguments (not just J.Empty)
                if (nc.getArguments() != null && !nc.getArguments().isEmpty()) {
                    // Check if the only argument is an Empty (which means no real arguments)
                    if (nc.getArguments().size() == 1 && nc.getArguments().get(0) instanceof J.Empty) {
                        // This is an empty constructor, we can transform it
                    } else {
                        // Has real arguments, skip
                        return nc;
                    }
                }

                // Add import for Lists factory and remove the impl import
                this.maybeAddImport(LISTS_FACTORY);
                this.maybeRemoveImport(FAST_LIST);

                // Ensure imports are properly ordered
                this.doAfterVisit(new OrderImports(false).getVisitor());

                // Check if it's a parameterized type
                if (nc.getClazz() instanceof J.ParameterizedType paramType) {
                    if (paramType.getTypeParameters() != null && !paramType.getTypeParameters().isEmpty()) {
                        // Check if this is a diamond operator by looking at the type parameters
                        boolean isDiamondOperator =
                            paramType.getTypeParameters().size() == 1 &&
                            paramType.getTypeParameters().get(0) instanceof J.Empty;

                        if (isDiamondOperator) {
                            // Diamond operator: new FastList<>()
                            // Use Lists.mutable.empty() without explicit type parameters
                            return this.listsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
                        } else {
                            // Explicit type parameters: new FastList<String>()
                            // Do not transform
                            return nc;
                        }
                    }
                }

                // Non-parameterized FastList: new FastList()
                return this.listsEmptyTemplate.apply(this.getCursor(), nc.getCoordinates().replace());
            }
        };
    }
}
