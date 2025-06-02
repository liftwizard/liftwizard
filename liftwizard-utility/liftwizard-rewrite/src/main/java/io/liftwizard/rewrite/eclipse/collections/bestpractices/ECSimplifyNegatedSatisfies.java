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
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ECSimplifyNegatedSatisfies extends Recipe {

    private static final MethodMatcher NONE_SATISFY_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* noneSatisfy(..)",
        true
    );
    private static final MethodMatcher ANY_SATISFY_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* anySatisfy(..)",
        true
    );

    @Override
    public String getDisplayName() {
        return "`!noneSatisfy()` → `anySatisfy()`";
    }

    @Override
    public String getDescription() {
        return "Simplifies negated satisfies checks: `!iterable.noneSatisfy(predicate)` to `iterable.anySatisfy(predicate)` and `!iterable.anySatisfy(predicate)` to `iterable.noneSatisfy(predicate)` for Eclipse Collections types.";
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
            new UsesMethod<>(NONE_SATISFY_MATCHER),
            new UsesMethod<>(ANY_SATISFY_MATCHER)
        );

        return Preconditions.check(
            check,
            new JavaVisitor<ExecutionContext>() {
                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (!(e instanceof J.Unary unary)) {
                        return e;
                    }

                    if (
                        unary.getOperator() != J.Unary.Type.Not ||
                        !(unary.getExpression() instanceof J.MethodInvocation methodInvocation)
                    ) {
                        return e;
                    }

                    // Skip transformation if we're inside anySatisfy or noneSatisfy method AND the call is on 'this'
                    J.MethodDeclaration enclosingMethod = getCursor().firstEnclosing(J.MethodDeclaration.class);
                    if (enclosingMethod != null) {
                        String methodName = enclosingMethod.getSimpleName();
                        if ("anySatisfy".equals(methodName) || "noneSatisfy".equals(methodName)) {
                            // Check if the method is being called on 'this' (either explicitly or implicitly)
                            Expression select = methodInvocation.getSelect();
                            if (
                                select == null ||
                                (select instanceof J.Identifier &&
                                    "this".equals(((J.Identifier) select).getSimpleName()))
                            ) {
                                return e;
                            }
                        }
                    }

                    if (NONE_SATISFY_MATCHER.matches(methodInvocation)) {
                        // Check if we're inside the anySatisfy() method to avoid issues

                        if (
                            enclosingMethod != null &&
                            "anySatisfy".equals(enclosingMethod.getSimpleName()) &&
                            methodInvocation.getSelect() instanceof J.Identifier &&
                            "this".equals(((J.Identifier) methodInvocation.getSelect()).getSimpleName())
                        ) {
                            // Don't transform this.noneSatisfy() inside anySatisfy() method
                            return e;
                        }

                        // Transform !noneSatisfy(predicate) to anySatisfy(predicate)
                        return methodInvocation
                            .withName(methodInvocation.getName().withSimpleName("anySatisfy"))
                            .withPrefix(unary.getPrefix());
                    }

                    if (ANY_SATISFY_MATCHER.matches(methodInvocation)) {
                        // Check if we're inside the noneSatisfy() method to avoid issues

                        if (
                            enclosingMethod != null &&
                            "noneSatisfy".equals(enclosingMethod.getSimpleName()) &&
                            methodInvocation.getSelect() instanceof J.Identifier &&
                            "this".equals(((J.Identifier) methodInvocation.getSelect()).getSimpleName())
                        ) {
                            // Don't transform this.anySatisfy() inside noneSatisfy() method
                            return e;
                        }

                        // Transform !anySatisfy(predicate) to noneSatisfy(predicate)
                        return methodInvocation
                            .withName(methodInvocation.getName().withSimpleName("noneSatisfy"))
                            .withPrefix(unary.getPrefix());
                    }

                    return e;
                }
            }
        );
    }
}
