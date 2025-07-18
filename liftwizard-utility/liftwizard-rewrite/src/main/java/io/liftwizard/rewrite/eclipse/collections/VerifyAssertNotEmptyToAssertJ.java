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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.OrderImports;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class VerifyAssertNotEmptyToAssertJ extends Recipe {

    private static final MethodMatcher ASSERT_NOT_EMPTY_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertNotEmpty(java.lang.String, java.lang.Object)"
    );

    @Override
    public String getDisplayName() {
        return "Replace Verify.assertNotEmpty with AssertJ";
    }

    @Override
    public String getDescription() {
        return "Replaces `Verify.assertNotEmpty(message, collection)` with `assertThat(collection).as(message).isNotEmpty()`.";
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
            new UsesMethod<>(ASSERT_NOT_EMPTY_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

                    if (ASSERT_NOT_EMPTY_MATCHER.matches(m)) {
                        final JavaTemplate template = JavaTemplate.builder(
                            "Assertions.assertThat(#{any(java.lang.Iterable)}).as(#{any(java.lang.String)}).isNotEmpty()"
                        )
                            .imports("org.assertj.core.api.Assertions")
                            .contextSensitive()
                            .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                            .build();

                        final Expression stringArg = m.getArguments().get(0);
                        final Expression collectionArg = m.getArguments().get(1);

                        maybeRemoveImport("org.eclipse.collections.impl.test.Verify");
                        maybeAddImport("org.assertj.core.api.Assertions");

                        doAfterVisit(new OrderImports(false).getVisitor());

                        return template.apply(getCursor(), m.getCoordinates().replace(), collectionArg, stringArg);
                    }

                    return m;
                }
            }
        );
    }
}
