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
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class ArrayIterateToNotEmpty extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace array null/length checks with ArrayIterate.notEmpty()";
    }

    @Override
    public String getDescription() {
        return "Replaces `array != null && array.length > 0` with `ArrayIterate.notEmpty(array)`.";
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

                if (!(e instanceof J.Binary)) {
                    return e;
                }

                J.Binary b = (J.Binary) e;

                // Look for pattern: array != null && array.length > 0
                if (b.getOperator() == J.Binary.Type.And) {
                    J.Binary leftBinary = null;
                    J.Binary rightBinary = null;

                    if (b.getLeft() instanceof J.Binary) {
                        leftBinary = (J.Binary) b.getLeft();
                    }
                    if (b.getRight() instanceof J.Binary) {
                        rightBinary = (J.Binary) b.getRight();
                    }

                    // Check if left side is array != null and right side is array.length > 0
                    if (leftBinary != null && rightBinary != null) {
                        if (
                            leftBinary.getOperator() == J.Binary.Type.NotEqual &&
                            rightBinary.getOperator() == J.Binary.Type.GreaterThan
                        ) {
                            // Check if left side is array != null
                            if (isNullLiteral(leftBinary.getRight()) && isArrayIdentifier(leftBinary.getLeft())) {
                                J.Identifier arrayId = (J.Identifier) leftBinary.getLeft();

                                // Check if right side is array.length > 0
                                if (
                                    isArrayLengthAccess(rightBinary.getLeft(), arrayId.getSimpleName()) &&
                                    isZeroLiteral(rightBinary.getRight())
                                ) {
                                    maybeAddImport("org.eclipse.collections.impl.utility.ArrayIterate");

                                    JavaTemplate template = JavaTemplate.builder("ArrayIterate.notEmpty(#{any()})")
                                        .imports("org.eclipse.collections.impl.utility.ArrayIterate")
                                        .contextSensitive()
                                        .javaParser(
                                            JavaParser.fromJavaVersion()
                                                .classpath("eclipse-collections")
                                                .dependsOn(
                                                    """
                                                    package org.eclipse.collections.impl.utility;
                                                    public final class ArrayIterate {
                                                        public static boolean notEmpty(Object[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(byte[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(char[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(int[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(long[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(float[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(double[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(short[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                        public static boolean notEmpty(boolean[] array) {
                                                            return array != null && array.length > 0;
                                                        }
                                                    }"""
                                                )
                                        )
                                        .build();

                                    return template.apply(getCursor(), b.getCoordinates().replace(), arrayId);
                                }
                            }
                        }
                    }
                }

                return e;
            }

            private boolean isNullLiteral(Expression expr) {
                return expr instanceof J.Literal && ((J.Literal) expr).getValue() == null;
            }

            private boolean isZeroLiteral(Expression expr) {
                return (
                    expr instanceof J.Literal &&
                    ((J.Literal) expr).getValue() instanceof Integer &&
                    ((J.Literal) expr).getValue().equals(0)
                );
            }

            private boolean isArrayIdentifier(Expression expr) {
                return expr instanceof J.Identifier;
            }

            private boolean isArrayLengthAccess(Expression expr, String arrayName) {
                if (!(expr instanceof J.FieldAccess)) {
                    return false;
                }
                J.FieldAccess fieldAccess = (J.FieldAccess) expr;
                return (
                    fieldAccess.getName().getSimpleName().equals("length") &&
                    fieldAccess.getTarget() instanceof J.Identifier &&
                    ((J.Identifier) fieldAccess.getTarget()).getSimpleName().equals(arrayName)
                );
            }
        };
    }
}
