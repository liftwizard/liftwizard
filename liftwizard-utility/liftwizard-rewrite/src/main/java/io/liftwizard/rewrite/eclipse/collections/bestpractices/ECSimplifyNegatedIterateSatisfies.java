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
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ECSimplifyNegatedIterateSatisfies extends Recipe {

    private static final MethodMatcher ITERATE_NONE_SATISFY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.Iterate noneSatisfy(..)"
    );
    private static final MethodMatcher ITERATE_ANY_SATISFY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.Iterate anySatisfy(..)"
    );

    @Override
    public String getDisplayName() {
        return "`!Iterate.noneSatisfy()` → `Iterate.anySatisfy()`";
    }

    @Override
    public String getDescription() {
        return "Replace `!Iterate.noneSatisfy()` with `Iterate.anySatisfy()` and `!Iterate.anySatisfy()` with `Iterate.noneSatisfy()`.";
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
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                Expression e = (Expression) super.visitExpression(expression, ctx);

                if (!(e instanceof J.Unary unary)) {
                    return e;
                }

                if (unary.getOperator() != J.Unary.Type.Not) {
                    return e;
                }

                // Skip double negation: !!expr
                Object parent = this.getCursor().getParentTreeCursor().getValue();
                if (parent instanceof J.Unary && ((J.Unary) parent).getOperator() == J.Unary.Type.Not) {
                    return e;
                }

                Expression innerExpression = unary.getExpression();

                // Handle parenthesized expressions
                while (innerExpression instanceof J.Parentheses) {
                    J tree = ((J.Parentheses<?>) innerExpression).getTree();
                    if (tree instanceof Expression) {
                        innerExpression = (Expression) tree;
                    } else {
                        break;
                    }
                }

                if (!(innerExpression instanceof J.MethodInvocation methodInvocation)) {
                    return e;
                }

                if (ITERATE_NONE_SATISFY.matches(methodInvocation)) {
                    // Transform !Iterate.noneSatisfy(iterable, predicate) to Iterate.anySatisfy(iterable, predicate)
                    return methodInvocation
                        .withName(methodInvocation.getName().withSimpleName("anySatisfy"))
                        .withPrefix(unary.getPrefix());
                }

                if (ITERATE_ANY_SATISFY.matches(methodInvocation)) {
                    // Transform !Iterate.anySatisfy(iterable, predicate) to Iterate.noneSatisfy(iterable, predicate)
                    return methodInvocation
                        .withName(methodInvocation.getName().withSimpleName("noneSatisfy"))
                        .withPrefix(unary.getPrefix());
                }

                return e;
            }
        };
    }
}
