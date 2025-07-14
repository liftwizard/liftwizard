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

public class VerifyAssertNotEqualsToAssertJ extends Recipe {

    private static final MethodMatcher VERIFY_ASSERT_NOT_EQUALS_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertNotEquals(java.lang.String, java.lang.Object, java.lang.Object)"
    );

    @Override
    public String getDisplayName() {
        return "Verify.assertNotEquals() → AssertJ assertThat().isNotEqualTo()";
    }

    @Override
    public String getDescription() {
        return "Transforms Eclipse Collections Verify.assertNotEquals(message, left, right) calls to AssertJ assertThat(left).as(message).isNotEqualTo(right) for better test readability and modern assertion style.";
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
            new UsesMethod<>(VERIFY_ASSERT_NOT_EQUALS_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

                    if (VERIFY_ASSERT_NOT_EQUALS_MATCHER.matches(m)) {
                        JavaTemplate template = JavaTemplate.builder(
                            "Assertions.assertThat(#{any(java.lang.Object)}).as(#{any(java.lang.String)}).isNotEqualTo(#{any(java.lang.Object)})"
                        )
                            .imports("org.assertj.core.api.Assertions")
                            .contextSensitive()
                            .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                            .build();

                        Expression messageArg = m.getArguments().get(0);
                        Expression leftArg = m.getArguments().get(1);
                        Expression rightArg = m.getArguments().get(2);

                        maybeAddImport("org.assertj.core.api.Assertions");
                        maybeRemoveImport("org.eclipse.collections.impl.test.Verify");

                        return template.apply(getCursor(), m.getCoordinates().replace(), leftArg, messageArg, rightArg);
                    }

                    return m;
                }
            }
        );
    }
}
