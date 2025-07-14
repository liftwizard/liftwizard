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
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class VerifyAssertContainsAllToAssertJ extends Recipe {

    private static final MethodMatcher VERIFY_ASSERT_CONTAINS_ALL_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertContainsAll(java.lang.String, java.lang.Iterable, java.lang.Object...)"
    );

    @Override
    public String getDisplayName() {
        return "Verify.assertContainsAll() → AssertJ assertThat().containsAll()";
    }

    @Override
    public String getDescription() {
        return "Transforms Eclipse Collections Verify.assertContainsAll(message, collection, objects...) calls to AssertJ assertThat(collection).as(message).containsAll(Lists.mutable.with(objects...)) for better test readability and modern assertion style.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(5);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesMethod<>(VERIFY_ASSERT_CONTAINS_ALL_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

                    if (VERIFY_ASSERT_CONTAINS_ALL_MATCHER.matches(m)) {
                        List<Expression> args = m.getArguments();
                        final Expression messageArg = args.get(0);
                        final Expression collectionArg = args.get(1);

                        // Get the varargs as a sublist
                        List<Expression> varArgs = args.subList(2, args.size());

                        // Build the template with proper placeholders for varargs
                        StringBuilder templateStr = new StringBuilder(
                            "Assertions.assertThat(#{any(java.lang.Iterable)}).as(#{any(java.lang.String)}).containsAll(Lists.mutable.with("
                        );

                        for (int i = 0; i < varArgs.size(); i++) {
                            if (i > 0) {
                                templateStr.append(", ");
                            }
                            templateStr.append("#{any(java.lang.Object)}");
                        }
                        templateStr.append("))");

                        final JavaTemplate template = JavaTemplate.builder(templateStr.toString())
                            .imports("org.assertj.core.api.Assertions", "org.eclipse.collections.api.factory.Lists")
                            .contextSensitive()
                            .javaParser(
                                JavaParser.fromJavaVersion().classpath("assertj-core", "eclipse-collections-api")
                            )
                            .build();

                        maybeAddImport("org.eclipse.collections.api.factory.Lists");
                        maybeAddImport("org.assertj.core.api.Assertions");
                        maybeRemoveImport("org.eclipse.collections.impl.test.Verify");

                        // Build the argument list: collection, message, then all varargs
                        Object[] templateArgs = new Object[2 + varArgs.size()];
                        templateArgs[0] = collectionArg;
                        templateArgs[1] = messageArg;
                        for (int i = 0; i < varArgs.size(); i++) {
                            templateArgs[2 + i] = varArgs.get(i);
                        }

                        return template.apply(getCursor(), m.getCoordinates().replace(), templateArgs);
                    }

                    return m;
                }
            }
        );
    }
}
