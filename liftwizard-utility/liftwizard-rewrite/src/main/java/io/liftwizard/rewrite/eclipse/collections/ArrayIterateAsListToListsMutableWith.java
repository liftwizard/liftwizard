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

package io.liftwizard.rewrite.eclipse.collections;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ArrayIterateAsListToListsMutableWith extends Recipe {

    private static final MethodMatcher ARRAY_ITERATE_AS_LIST_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.utility.ArrayIterate asList(..)",
        true
    );

    @Override
    public String getDisplayName() {
        return "Replace ArrayIterate.asList() with Lists.mutable.with()";
    }

    @Override
    public String getDescription() {
        return "Replaces `ArrayIterate.asList(new Object[]{expr})` with `Lists.mutable.with(expr)` for Eclipse Collections.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(10);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesMethod<>(ARRAY_ITERATE_AS_LIST_MATCHER),
            new JavaIsoVisitor<ExecutionContext>() {
                @Override
                public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

                    if (!ARRAY_ITERATE_AS_LIST_MATCHER.matches(m)) {
                        return m;
                    }

                    if (m.getArguments().size() != 1) {
                        return m;
                    }

                    Expression argument = m.getArguments().get(0);
                    if (!(argument instanceof J.NewArray)) {
                        return m;
                    }

                    J.NewArray newArray = (J.NewArray) argument;
                    if (newArray.getInitializer() == null || newArray.getInitializer().isEmpty()) {
                        return m;
                    }

                    StringBuilder templateBuilder = new StringBuilder("Lists.mutable.with(");
                    for (int i = 0; i < newArray.getInitializer().size(); i++) {
                        if (i > 0) {
                            templateBuilder.append(", ");
                        }
                        templateBuilder.append("#{any()}");
                    }
                    templateBuilder.append(")");

                    this.maybeAddImport("org.eclipse.collections.api.factory.Lists");
                    this.maybeRemoveImport("org.eclipse.collections.impl.utility.ArrayIterate");

                    JavaTemplate template = JavaTemplate.builder(templateBuilder.toString())
                        .imports("org.eclipse.collections.api.factory.Lists")
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion()
                                .classpath("eclipse-collections-api")
                                .dependsOn(
                                    """
                                    package org.eclipse.collections.api.list;
                                    public interface MutableList<T> extends java.util.List<T> {
                                    }""",
                                    """
                                    package org.eclipse.collections.api.factory;
                                    import org.eclipse.collections.api.list.MutableList;
                                    public final class Lists {
                                        public static final MutableListFactory mutable = null;
                                        public static final class MutableListFactory {
                                            public <T> MutableList<T> with(T... elements) { return null; }
                                        }
                                    }"""
                                )
                        )
                        .build();

                    return template.apply(
                        this.getCursor(),
                        m.getCoordinates().replace(),
                        newArray.getInitializer().toArray()
                    );
                }
            }
        );
    }
}
