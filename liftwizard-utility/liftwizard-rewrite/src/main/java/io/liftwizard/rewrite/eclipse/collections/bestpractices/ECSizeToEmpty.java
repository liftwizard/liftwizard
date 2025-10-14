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

public class ECSizeToEmpty extends Recipe {

    private static final MethodMatcher SIZE_MATCHER = new MethodMatcher("org.eclipse.collections.api..* size()", true);

    @Override
    public String getDisplayName() {
        return "`size() == 0` → `isEmpty()`";
    }

    @Override
    public String getDescription() {
        return "Converts size() comparisons to more idiomatic isEmpty() and notEmpty() method calls for Eclipse Collections types. Handles patterns like `size() == 0` -> `isEmpty()`, `size() > 0` -> `notEmpty()`, `size() >= 1` -> `notEmpty()`, etc.";
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
            new UsesMethod<>(SIZE_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (!(e instanceof J.Binary binary)) {
                        return e;
                    }

                    // Handle isEmpty() patterns: size() == 0, size() < 1, size() <= 0
                    J.MethodInvocation sizeCallForEmpty = this.getSizeCallForIsEmpty(binary);
                    if (sizeCallForEmpty != null) {
                        // Check if we're inside the isEmpty() method to avoid infinite recursion
                        J.MethodDeclaration enclosingMethod = this.getCursor().firstEnclosing(
                            J.MethodDeclaration.class
                        );
                        if (
                            enclosingMethod != null &&
                            "isEmpty".equals(enclosingMethod.getSimpleName()) &&
                            sizeCallForEmpty.getSelect() instanceof J.Identifier &&
                            "this".equals(((J.Identifier) sizeCallForEmpty.getSelect()).getSimpleName())
                        ) {
                            // Don't transform this.size() == 0 inside isEmpty() method
                            return e;
                        }

                        return sizeCallForEmpty
                            .withName(sizeCallForEmpty.getName().withSimpleName("isEmpty"))
                            .withPrefix(binary.getPrefix());
                    }

                    // Handle notEmpty() patterns: size() > 0, size() != 0, size() >= 1
                    J.MethodInvocation sizeCallForNotEmpty = this.getSizeCallForNotEmpty(binary);
                    if (sizeCallForNotEmpty != null) {
                        // Check if we're inside the notEmpty() method to avoid infinite recursion
                        J.MethodDeclaration enclosingMethod = this.getCursor().firstEnclosing(
                            J.MethodDeclaration.class
                        );
                        if (
                            enclosingMethod != null &&
                            "notEmpty".equals(enclosingMethod.getSimpleName()) &&
                            sizeCallForNotEmpty.getSelect() instanceof J.Identifier &&
                            "this".equals(((J.Identifier) sizeCallForNotEmpty.getSelect()).getSimpleName())
                        ) {
                            // Don't transform this.size() > 0 inside notEmpty() method
                            return e;
                        }

                        return sizeCallForNotEmpty
                            .withName(sizeCallForNotEmpty.getName().withSimpleName("notEmpty"))
                            .withPrefix(binary.getPrefix());
                    }

                    return e;
                }

                private J.MethodInvocation getSizeCallForIsEmpty(J.Binary binary) {
                    Expression left = binary.getLeft();
                    Expression right = binary.getRight();

                    if (binary.getOperator() == J.Binary.Type.Equal) {
                        // Pattern: size() == 0 or 0 == size()
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThan) {
                        // Pattern: size() < 1
                        if (left instanceof J.MethodInvocation methodCall && this.isOne(right)) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 1 > size()
                        if (this.isOne(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThanOrEqual) {
                        // Pattern: size() <= 0
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 0 >= size()
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.GreaterThanOrEqual) {
                        // Pattern: 0 >= size() - this is handled in LessThanOrEqual case as the reverse
                        // But we need to handle when it's written as 0 >= size()
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.GreaterThan) {
                        // Pattern: 1 > size()
                        if (this.isOne(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    }

                    return null;
                }

                private J.MethodInvocation getSizeCallForNotEmpty(J.Binary binary) {
                    Expression left = binary.getLeft();
                    Expression right = binary.getRight();

                    if (binary.getOperator() == J.Binary.Type.GreaterThan) {
                        // Pattern: size() > 0
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 0 < size()
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.NotEqual) {
                        // Pattern: size() != 0 or 0 != size()
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.GreaterThanOrEqual) {
                        // Pattern: size() >= 1
                        if (left instanceof J.MethodInvocation methodCall && this.isOne(right)) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 1 <= size()
                        if (this.isOne(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThanOrEqual) {
                        // Pattern: 1 <= size()
                        if (this.isOne(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThan) {
                        // Pattern: 0 < size()
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (SIZE_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    }

                    return null;
                }

                private boolean isZero(Expression expression) {
                    if (expression instanceof J.Literal literal) {
                        Object value = literal.getValue();
                        return value instanceof Integer && (Integer) value == 0;
                    }
                    return false;
                }

                private boolean isOne(Expression expression) {
                    if (expression instanceof J.Literal literal) {
                        Object value = literal.getValue();
                        return value instanceof Integer && (Integer) value == 1;
                    }
                    return false;
                }
            }
        );
    }
}
