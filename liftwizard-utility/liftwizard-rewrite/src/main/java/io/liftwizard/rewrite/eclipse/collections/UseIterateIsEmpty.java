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
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class UseIterateIsEmpty extends Recipe {

    private static final MethodMatcher IS_EMPTY_MATCHER = new MethodMatcher("java.util.Collection isEmpty()", true);

    @Override
    public String getDisplayName() {
        return "Use Iterate.isEmpty() for null-safe empty checks";
    }

    @Override
    public String getDescription() {
        return "Transforms null-safe empty checks like `collection == null || collection.isEmpty()` to use `Iterate.isEmpty(collection)` for better readability and null safety.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(15);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesMethod<>(IS_EMPTY_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                private final JavaTemplate iterateIsEmptyTemplate = JavaTemplate.builder(
                    "Iterate.isEmpty(#{any(java.lang.Iterable)})"
                )
                    .imports("org.eclipse.collections.impl.utility.Iterate")
                    .contextSensitive()
                    .javaParser(
                        JavaParser.fromJavaVersion()
                            .classpath("eclipse-collections")
                            .dependsOn(
                                """
                                package org.eclipse.collections.impl.utility;
                                public final class Iterate {
                                    public static boolean isEmpty(Iterable<?> iterable) {
                                        return iterable == null || !iterable.iterator().hasNext();
                                    }
                                }"""
                            )
                    )
                    .build();

                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (!(e instanceof J.Binary)) {
                        return e;
                    }

                    J.Binary binary = (J.Binary) e;
                    if (binary.getOperator() != J.Binary.Type.Or) {
                        return e;
                    }

                    Expression left = binary.getLeft();
                    Expression right = binary.getRight();

                    if (isNullCheck(left) && isEmptyCheck(right)) {
                        Expression collection = extractCollectionFromNullCheck(left);
                        Expression collectionFromEmpty = extractCollectionFromEmptyCheck(right);

                        if (
                            collection != null &&
                            collectionFromEmpty != null &&
                            areEquivalentExpressions(collection, collectionFromEmpty)
                        ) {
                            doAfterVisit(new AddImport<>("org.eclipse.collections.impl.utility.Iterate", null, false));

                            return iterateIsEmptyTemplate.apply(getCursor(), e.getCoordinates().replace(), collection);
                        }
                    }

                    return e;
                }

                private boolean isNullCheck(Expression expression) {
                    if (!(expression instanceof J.Binary)) {
                        return false;
                    }
                    J.Binary binary = (J.Binary) expression;
                    return (
                        binary.getOperator() == J.Binary.Type.Equal &&
                        (isNullLiteral(binary.getLeft()) || isNullLiteral(binary.getRight()))
                    );
                }

                private boolean isEmptyCheck(Expression expression) {
                    return (
                        expression instanceof J.MethodInvocation &&
                        IS_EMPTY_MATCHER.matches((J.MethodInvocation) expression)
                    );
                }

                private Expression extractCollectionFromNullCheck(Expression nullCheck) {
                    if (!(nullCheck instanceof J.Binary)) {
                        return null;
                    }
                    J.Binary binary = (J.Binary) nullCheck;
                    if (isNullLiteral(binary.getLeft())) {
                        return binary.getRight();
                    }
                    if (isNullLiteral(binary.getRight())) {
                        return binary.getLeft();
                    }
                    return null;
                }

                private Expression extractCollectionFromEmptyCheck(Expression emptyCheck) {
                    if (!(emptyCheck instanceof J.MethodInvocation)) {
                        return null;
                    }
                    J.MethodInvocation methodInvocation = (J.MethodInvocation) emptyCheck;
                    return methodInvocation.getSelect();
                }

                private boolean isNullLiteral(Expression expression) {
                    return expression instanceof J.Literal && ((J.Literal) expression).getValue() == null;
                }

                private boolean areEquivalentExpressions(Expression expr1, Expression expr2) {
                    return expr1.printTrimmed(getCursor()).equals(expr2.printTrimmed(getCursor()));
                }
            }
        );
    }
}
