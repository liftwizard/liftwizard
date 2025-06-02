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

public class ECCountToSatisfies extends Recipe {

    private static final MethodMatcher COUNT_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* count(..)",
        true
    );

    @Override
    public String getDisplayName() {
        return "`count() == 0` → `noneSatisfy()`";
    }

    @Override
    public String getDescription() {
        return (
            "Converts count() comparisons to more efficient satisfies methods for Eclipse Collections types. " +
            "Handles patterns like `count(predicate) == 0` -> `noneSatisfy(predicate)`, `count(predicate) > 0` -> `anySatisfy(predicate)`, " +
            "`count(predicate) != 0` -> `anySatisfy(predicate)`, `count(predicate) <= 0` -> `noneSatisfy(predicate)`, " +
            "and `count(predicate) >= 1` -> `anySatisfy(predicate)`."
        );
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
            new UsesMethod<>(COUNT_MATCHER),
            new JavaVisitor<>() {
                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (!(e instanceof J.Binary binary)) {
                        return e;
                    }

                    // Handle patterns that should become noneSatisfy()
                    J.MethodInvocation countCallForNone = this.getCountCallForNoneSatisfy(binary);
                    if (countCallForNone != null) {
                        return countCallForNone
                            .withName(countCallForNone.getName().withSimpleName("noneSatisfy"))
                            .withPrefix(binary.getPrefix());
                    }

                    // Handle patterns that should become anySatisfy()
                    J.MethodInvocation countCallForAny = this.getCountCallForAnySatisfy(binary);
                    if (countCallForAny != null) {
                        return countCallForAny
                            .withName(countCallForAny.getName().withSimpleName("anySatisfy"))
                            .withPrefix(binary.getPrefix());
                    }

                    return e;
                }

                private J.MethodInvocation getCountCallForNoneSatisfy(J.Binary binary) {
                    Expression left = binary.getLeft();
                    Expression right = binary.getRight();

                    if (binary.getOperator() == J.Binary.Type.Equal) {
                        // Pattern: count(pred) == 0
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 0 == count(pred)
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThanOrEqual) {
                        // Pattern: count(pred) <= 0
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.GreaterThanOrEqual) {
                        // Pattern: 0 >= count(pred)
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    }

                    return null;
                }

                private J.MethodInvocation getCountCallForAnySatisfy(J.Binary binary) {
                    Expression left = binary.getLeft();
                    Expression right = binary.getRight();

                    if (binary.getOperator() == J.Binary.Type.GreaterThan) {
                        // Pattern: count(pred) > 0
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 0 < count(pred)
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.NotEqual) {
                        // Pattern: count(pred) != 0 or 0 != count(pred)
                        if (left instanceof J.MethodInvocation methodCall && this.isZero(right)) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.GreaterThanOrEqual) {
                        // Pattern: count(pred) >= 1
                        if (left instanceof J.MethodInvocation methodCall && this.isOne(right)) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                        // Pattern: 1 <= count(pred)
                        if (this.isOne(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThan) {
                        // Pattern: 0 < count(pred)
                        if (this.isZero(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
                                return methodCall;
                            }
                        }
                    } else if (binary.getOperator() == J.Binary.Type.LessThanOrEqual) {
                        // Pattern: 1 <= count(pred)
                        if (this.isOne(left) && right instanceof J.MethodInvocation methodCall) {
                            if (COUNT_MATCHER.matches(methodCall)) {
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
