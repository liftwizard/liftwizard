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
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ECNullSafeEquals extends Recipe {

    private static final String COMPARATORS = "org.eclipse.collections.impl.block.factory.Comparators";

    @Override
    public String getDisplayName() {
        return "Null-safe equality checks → `Comparators.nullSafeEquals()`";
    }

    @Override
    public String getDescription() {
        return "Replace complex null-safe equality checks with `Comparators.nullSafeEquals()`.";
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
            @Override
            public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                Expression e = (Expression) super.visitExpression(expression, ctx);

                JavaTemplate nullSafeEqualsTemplate = JavaTemplate.builder(
                    "Comparators.nullSafeEquals(#{any()}, #{any()})"
                )
                    .imports(COMPARATORS)
                    .contextSensitive()
                    .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections"))
                    .build();

                JavaTemplate notNullSafeEqualsTemplate = JavaTemplate.builder(
                    "!Comparators.nullSafeEquals(#{any()}, #{any()})"
                )
                    .imports(COMPARATORS)
                    .contextSensitive()
                    .javaParser(JavaParser.fromJavaVersion().classpath("eclipse-collections"))
                    .build();

                // Pattern 1: left == null ? right != null : !left.equals(right)
                // Result: !Comparators.nullSafeEquals(left, right)
                if (e instanceof J.Ternary ternary) {
                    if (this.isNullCheck(ternary.getCondition(), true)) {
                        J.Binary nullCheck = (J.Binary) ternary.getCondition();
                        Expression left = nullCheck.getLeft();

                        if (
                            ternary.getTruePart() instanceof J.Binary truePart &&
                            truePart.getOperator() == J.Binary.Type.NotEqual &&
                            this.isNullLiteral(truePart.getRight())
                        ) {
                            Expression right = truePart.getLeft();

                            if (
                                ternary.getFalsePart() instanceof J.Unary unary &&
                                unary.getOperator() == J.Unary.Type.Not &&
                                this.isEqualsCall(unary.getExpression(), left, right)
                            ) {
                                return this.replaceWithNullSafeEquals(
                                    ternary,
                                    left,
                                    right,
                                    true,
                                    nullSafeEqualsTemplate,
                                    notNullSafeEqualsTemplate
                                );
                            }
                        }
                    }
                }

                // Pattern 2: left == null ? right == null : left.equals(right)
                // Result: Comparators.nullSafeEquals(left, right)
                if (e instanceof J.Ternary ternary) {
                    if (this.isNullCheck(ternary.getCondition(), true)) {
                        J.Binary nullCheck = (J.Binary) ternary.getCondition();
                        Expression left = nullCheck.getLeft();

                        if (
                            ternary.getTruePart() instanceof J.Binary truePart &&
                            truePart.getOperator() == J.Binary.Type.Equal &&
                            this.isNullLiteral(truePart.getRight())
                        ) {
                            Expression right = truePart.getLeft();

                            if (this.isEqualsCall(ternary.getFalsePart(), left, right)) {
                                return this.replaceWithNullSafeEquals(
                                    ternary,
                                    left,
                                    right,
                                    false,
                                    nullSafeEqualsTemplate,
                                    notNullSafeEqualsTemplate
                                );
                            }
                        }
                    }
                }

                // Pattern 3: left == null ? right == null : left == right || left.equals(right)
                // Result: Comparators.nullSafeEquals(left, right)
                if (e instanceof J.Ternary ternary) {
                    if (this.isNullCheck(ternary.getCondition(), true)) {
                        J.Binary nullCheck = (J.Binary) ternary.getCondition();
                        Expression left = nullCheck.getLeft();

                        if (
                            ternary.getTruePart() instanceof J.Binary truePart &&
                            truePart.getOperator() == J.Binary.Type.Equal &&
                            this.isNullLiteral(truePart.getRight())
                        ) {
                            Expression right = truePart.getLeft();

                            if (
                                ternary.getFalsePart() instanceof J.Binary falsePart &&
                                falsePart.getOperator() == J.Binary.Type.Or
                            ) {
                                if (
                                    falsePart.getLeft() instanceof J.Binary referenceCheck &&
                                    referenceCheck.getOperator() == J.Binary.Type.Equal &&
                                    this.isSameVariable(referenceCheck.getLeft(), left) &&
                                    this.isSameVariable(referenceCheck.getRight(), right) &&
                                    this.isEqualsCall(falsePart.getRight(), left, right)
                                ) {
                                    return this.replaceWithNullSafeEquals(
                                        ternary,
                                        left,
                                        right,
                                        false,
                                        nullSafeEqualsTemplate,
                                        notNullSafeEqualsTemplate
                                    );
                                }
                            }
                        }
                    }
                }

                // Pattern 4: left == right || left != null && left.equals(right)
                // Result: Comparators.nullSafeEquals(left, right)
                if (e instanceof J.Binary binary && binary.getOperator() == J.Binary.Type.Or) {
                    if (
                        binary.getLeft() instanceof J.Binary referenceCheck &&
                        referenceCheck.getOperator() == J.Binary.Type.Equal
                    ) {
                        Expression left = referenceCheck.getLeft();
                        Expression right = referenceCheck.getRight();

                        if (
                            binary.getRight() instanceof J.Binary andExpr && andExpr.getOperator() == J.Binary.Type.And
                        ) {
                            if (
                                andExpr.getLeft() instanceof J.Binary notNullCheck &&
                                notNullCheck.getOperator() == J.Binary.Type.NotEqual &&
                                this.isSameVariable(notNullCheck.getLeft(), left) &&
                                this.isNullLiteral(notNullCheck.getRight()) &&
                                this.isEqualsCall(andExpr.getRight(), left, right)
                            ) {
                                return this.replaceWithNullSafeEquals(
                                    binary,
                                    left,
                                    right,
                                    false,
                                    nullSafeEqualsTemplate,
                                    notNullSafeEqualsTemplate
                                );
                            }
                        }
                    }
                }

                // Pattern 5: right == left || left != null && left.equals(right)
                // Result: Comparators.nullSafeEquals(left, right)
                if (e instanceof J.Binary binary && binary.getOperator() == J.Binary.Type.Or) {
                    if (
                        binary.getLeft() instanceof J.Binary referenceCheck &&
                        referenceCheck.getOperator() == J.Binary.Type.Equal
                    ) {
                        Expression right = referenceCheck.getLeft();
                        Expression left = referenceCheck.getRight();

                        if (
                            binary.getRight() instanceof J.Binary andExpr && andExpr.getOperator() == J.Binary.Type.And
                        ) {
                            if (
                                andExpr.getLeft() instanceof J.Binary notNullCheck &&
                                notNullCheck.getOperator() == J.Binary.Type.NotEqual &&
                                this.isSameVariable(notNullCheck.getLeft(), left) &&
                                this.isNullLiteral(notNullCheck.getRight()) &&
                                this.isEqualsCall(andExpr.getRight(), left, right)
                            ) {
                                return this.replaceWithNullSafeEquals(
                                    binary,
                                    left,
                                    right,
                                    false,
                                    nullSafeEqualsTemplate,
                                    notNullSafeEqualsTemplate
                                );
                            }
                        }
                    }
                }

                // Pattern 6: left == null || right == null ? left == right : left.equals(right)
                // Result: Comparators.nullSafeEquals(left, right)
                if (e instanceof J.Ternary ternary) {
                    if (
                        ternary.getCondition() instanceof J.Binary orCondition &&
                        orCondition.getOperator() == J.Binary.Type.Or
                    ) {
                        Expression left = null;
                        Expression right = null;

                        if (
                            orCondition.getLeft() instanceof J.Binary leftNullCheck &&
                            leftNullCheck.getOperator() == J.Binary.Type.Equal &&
                            this.isNullLiteral(leftNullCheck.getRight())
                        ) {
                            left = leftNullCheck.getLeft();
                        }

                        if (
                            orCondition.getRight() instanceof J.Binary rightNullCheck &&
                            rightNullCheck.getOperator() == J.Binary.Type.Equal &&
                            this.isNullLiteral(rightNullCheck.getRight())
                        ) {
                            right = rightNullCheck.getLeft();
                        }

                        if (
                            left != null &&
                            right != null &&
                            ternary.getTruePart() instanceof J.Binary truePart &&
                            truePart.getOperator() == J.Binary.Type.Equal &&
                            this.isSameVariable(truePart.getLeft(), left) &&
                            this.isSameVariable(truePart.getRight(), right) &&
                            this.isEqualsCall(ternary.getFalsePart(), left, right)
                        ) {
                            return this.replaceWithNullSafeEquals(
                                ternary,
                                left,
                                right,
                                false,
                                nullSafeEqualsTemplate,
                                notNullSafeEqualsTemplate
                            );
                        }
                    }
                }

                return e;
            }

            private boolean isNullCheck(Expression expr, boolean checkEqual) {
                if (expr instanceof J.Binary binary) {
                    J.Binary.Type expectedOp = checkEqual ? J.Binary.Type.Equal : J.Binary.Type.NotEqual;
                    return binary.getOperator() == expectedOp && this.isNullLiteral(binary.getRight());
                }
                return false;
            }

            private boolean isNullLiteral(Expression expr) {
                return expr instanceof J.Literal literal && literal.getValue() == null;
            }

            private boolean isEqualsCall(Expression expr, Expression expectedReceiver, Expression expectedArg) {
                if (expr instanceof J.MethodInvocation method) {
                    if (
                        "equals".equals(method.getSimpleName()) &&
                        method.getArguments().size() == 1 &&
                        this.isSameVariable(method.getSelect(), expectedReceiver) &&
                        this.isSameVariable(method.getArguments().get(0), expectedArg)
                    ) {
                        return true;
                    }
                }
                return false;
            }

            private boolean isSameVariable(Expression e1, Expression e2) {
                if (e1 instanceof J.Identifier id1 && e2 instanceof J.Identifier id2) {
                    return id1.getSimpleName().equals(id2.getSimpleName());
                }
                return false;
            }

            private Expression replaceWithNullSafeEquals(
                Expression original,
                Expression left,
                Expression right,
                boolean negate,
                JavaTemplate nullSafeEqualsTemplate,
                JavaTemplate notNullSafeEqualsTemplate
            ) {
                this.maybeAddImport(COMPARATORS);
                this.doAfterVisit(new OrderImports(false).getVisitor());

                JavaTemplate template = negate ? notNullSafeEqualsTemplate : nullSafeEqualsTemplate;
                return template.apply(this.getCursor(), original.getCoordinates().replace(), left, right);
            }
        };
    }
}
