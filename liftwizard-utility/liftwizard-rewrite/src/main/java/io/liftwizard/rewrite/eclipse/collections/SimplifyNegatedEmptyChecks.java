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
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class SimplifyNegatedEmptyChecks extends Recipe {

    private static final MethodMatcher IS_EMPTY_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* isEmpty()",
        true
    );
    private static final MethodMatcher NOT_EMPTY_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* notEmpty()",
        true
    );

    @Override
    public String getDisplayName() {
        return "Simplify negated empty checks";
    }

    @Override
    public String getDescription() {
        return "Simplifies negated empty checks: `!iterable.isEmpty()` to `iterable.notEmpty()` and `!iterable.notEmpty()` to `iterable.isEmpty()` for Eclipse Collections types.";
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
        TreeVisitor<?, ExecutionContext> check = Preconditions.or(
            new UsesMethod<>(IS_EMPTY_MATCHER),
            new UsesMethod<>(NOT_EMPTY_MATCHER)
        );

        return Preconditions.check(
            check,
            new JavaVisitor<ExecutionContext>() {
                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (!(e instanceof J.Unary)) {
                        return e;
                    }

                    J.Unary unary = (J.Unary) e;
                    if (
                        unary.getOperator() != J.Unary.Type.Not ||
                        !(unary.getExpression() instanceof J.MethodInvocation)
                    ) {
                        return e;
                    }

                    J.MethodInvocation methodInvocation = (J.MethodInvocation) unary.getExpression();

                    if (IS_EMPTY_MATCHER.matches(methodInvocation)) {
                        // Transform !isEmpty() to notEmpty()
                        return methodInvocation
                            .withName(methodInvocation.getName().withSimpleName("notEmpty"))
                            .withPrefix(unary.getPrefix());
                    } else if (NOT_EMPTY_MATCHER.matches(methodInvocation)) {
                        // Transform !notEmpty() to isEmpty()
                        return methodInvocation
                            .withName(methodInvocation.getName().withSimpleName("isEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    return e;
                }
            }
        );
    }
}
