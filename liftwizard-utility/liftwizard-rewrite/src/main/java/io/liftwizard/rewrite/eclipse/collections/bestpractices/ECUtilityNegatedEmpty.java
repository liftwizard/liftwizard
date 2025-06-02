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

public class ECUtilityNegatedEmpty extends Recipe {

    private static final MethodMatcher ITERATE_IS_EMPTY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.Iterate isEmpty(..)",
        true
    );

    private static final MethodMatcher ITERATE_NOT_EMPTY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.Iterate notEmpty(..)",
        true
    );

    private static final MethodMatcher MAP_ITERATE_IS_EMPTY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.MapIterate isEmpty(..)",
        true
    );

    private static final MethodMatcher MAP_ITERATE_NOT_EMPTY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.MapIterate notEmpty(..)",
        true
    );

    private static final MethodMatcher ARRAY_ITERATE_IS_EMPTY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.ArrayIterate isEmpty(..)",
        true
    );

    private static final MethodMatcher ARRAY_ITERATE_NOT_EMPTY = new MethodMatcher(
        "org.eclipse.collections.impl.utility.ArrayIterate notEmpty(..)",
        true
    );

    @Override
    public String getDisplayName() {
        return "`!Iterate.isEmpty()` → `Iterate.notEmpty()`";
    }

    @Override
    public String getDescription() {
        return (
            "Simplifies negated empty/notEmpty method calls on Eclipse Collections utility classes. " +
            "Converts `!Iterate.isEmpty(collection)` to `Iterate.notEmpty(collection)`, " +
            "`!MapIterate.isEmpty(map)` to `MapIterate.notEmpty(map)`, " +
            "`!ArrayIterate.isEmpty(array)` to `ArrayIterate.notEmpty(array)`, " +
            "and vice versa for notEmpty calls."
        );
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
            Preconditions.or(
                new UsesMethod<>(ITERATE_IS_EMPTY),
                new UsesMethod<>(ITERATE_NOT_EMPTY),
                new UsesMethod<>(MAP_ITERATE_IS_EMPTY),
                new UsesMethod<>(MAP_ITERATE_NOT_EMPTY),
                new UsesMethod<>(ARRAY_ITERATE_IS_EMPTY),
                new UsesMethod<>(ARRAY_ITERATE_NOT_EMPTY)
            ),
            new JavaVisitor<>() {
                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (!(e instanceof J.Unary)) {
                        return e;
                    }

                    J.Unary unary = (J.Unary) e;
                    if (unary.getOperator() != J.Unary.Type.Not) {
                        return e;
                    }

                    Expression operand = unary.getExpression();

                    // Handle parenthesized expressions
                    if (operand instanceof J.Parentheses) {
                        J.Parentheses parentheses = (J.Parentheses) operand;
                        operand = (Expression) parentheses.getTree();
                    }

                    if (!(operand instanceof J.MethodInvocation)) {
                        return e;
                    }

                    J.MethodInvocation methodCall = (J.MethodInvocation) operand;

                    // Handle Iterate methods
                    if (ITERATE_IS_EMPTY.matches(methodCall)) {
                        return methodCall
                            .withName(methodCall.getName().withSimpleName("notEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    if (ITERATE_NOT_EMPTY.matches(methodCall)) {
                        return methodCall
                            .withName(methodCall.getName().withSimpleName("isEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    // Handle MapIterate methods
                    if (MAP_ITERATE_IS_EMPTY.matches(methodCall)) {
                        return methodCall
                            .withName(methodCall.getName().withSimpleName("notEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    if (MAP_ITERATE_NOT_EMPTY.matches(methodCall)) {
                        return methodCall
                            .withName(methodCall.getName().withSimpleName("isEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    // Handle ArrayIterate methods
                    if (ARRAY_ITERATE_IS_EMPTY.matches(methodCall)) {
                        return methodCall
                            .withName(methodCall.getName().withSimpleName("notEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    if (ARRAY_ITERATE_NOT_EMPTY.matches(methodCall)) {
                        return methodCall
                            .withName(methodCall.getName().withSimpleName("isEmpty"))
                            .withPrefix(unary.getPrefix());
                    }

                    return e;
                }
            }
        );
    }
}
