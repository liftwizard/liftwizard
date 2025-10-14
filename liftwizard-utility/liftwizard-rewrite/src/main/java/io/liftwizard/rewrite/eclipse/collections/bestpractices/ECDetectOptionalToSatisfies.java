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

public class ECDetectOptionalToSatisfies extends Recipe {

    private static final MethodMatcher DETECT_OPTIONAL_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* detectOptional(..)",
        true
    );

    private static final MethodMatcher IS_PRESENT_MATCHER = new MethodMatcher("java.util.Optional isPresent()", true);

    private static final MethodMatcher IS_EMPTY_MATCHER = new MethodMatcher("java.util.Optional isEmpty()", true);

    private static final MethodMatcher IS_PRESENT_INT_MATCHER = new MethodMatcher(
        "java.util.OptionalInt isPresent()",
        true
    );

    private static final MethodMatcher IS_EMPTY_INT_MATCHER = new MethodMatcher(
        "java.util.OptionalInt isEmpty()",
        true
    );

    private static final MethodMatcher IS_PRESENT_LONG_MATCHER = new MethodMatcher(
        "java.util.OptionalLong isPresent()",
        true
    );

    private static final MethodMatcher IS_EMPTY_LONG_MATCHER = new MethodMatcher(
        "java.util.OptionalLong isEmpty()",
        true
    );

    private static final MethodMatcher IS_PRESENT_DOUBLE_MATCHER = new MethodMatcher(
        "java.util.OptionalDouble isPresent()",
        true
    );

    private static final MethodMatcher IS_EMPTY_DOUBLE_MATCHER = new MethodMatcher(
        "java.util.OptionalDouble isEmpty()",
        true
    );

    @Override
    public String getDisplayName() {
        return "`detectOptional().isPresent()` → `anySatisfy()`";
    }

    @Override
    public String getDescription() {
        return "Converts `iterable.detectOptional(predicate).isPresent()` to `iterable.anySatisfy(predicate)`, `!iterable.detectOptional(predicate).isPresent()` to `iterable.noneSatisfy(predicate)`, `iterable.detectOptional(predicate).isEmpty()` to `iterable.noneSatisfy(predicate)`, and `!iterable.detectOptional(predicate).isEmpty()` to `iterable.anySatisfy(predicate)` for Eclipse Collections types.";
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
            new UsesMethod<>(DETECT_OPTIONAL_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                    Expression e = (Expression) super.visitExpression(expression, ctx);

                    if (e instanceof J.Unary unary) {
                        if (unary.getOperator() == J.Unary.Type.Not) {
                            DetectOptionalCallResult result = this.getDetectOptionalCall(unary.getExpression());
                            if (result != null) {
                                String methodName = result.isEmptyCheck() ? "anySatisfy" : "noneSatisfy";
                                return result
                                    .detectOptionalCall()
                                    .withName(result.detectOptionalCall().getName().withSimpleName(methodName))
                                    .withPrefix(unary.getPrefix());
                            }
                        }
                    }

                    DetectOptionalCallResult result = this.getDetectOptionalCall(e);
                    if (result != null) {
                        String methodName = result.isEmptyCheck() ? "noneSatisfy" : "anySatisfy";
                        return result
                            .detectOptionalCall()
                            .withName(result.detectOptionalCall().getName().withSimpleName(methodName))
                            .withPrefix(e.getPrefix());
                    }

                    return e;
                }

                private DetectOptionalCallResult getDetectOptionalCall(Expression expression) {
                    if (!(expression instanceof J.MethodInvocation methodInvocation)) {
                        return null;
                    }

                    boolean isEmptyCheck =
                        IS_EMPTY_MATCHER.matches(methodInvocation) ||
                        IS_EMPTY_INT_MATCHER.matches(methodInvocation) ||
                        IS_EMPTY_LONG_MATCHER.matches(methodInvocation) ||
                        IS_EMPTY_DOUBLE_MATCHER.matches(methodInvocation);

                    boolean isPresentCheck =
                        IS_PRESENT_MATCHER.matches(methodInvocation) ||
                        IS_PRESENT_INT_MATCHER.matches(methodInvocation) ||
                        IS_PRESENT_LONG_MATCHER.matches(methodInvocation) ||
                        IS_PRESENT_DOUBLE_MATCHER.matches(methodInvocation);

                    if (!isEmptyCheck && !isPresentCheck) {
                        return null;
                    }

                    Expression select = methodInvocation.getSelect();
                    if (!(select instanceof J.MethodInvocation detectOptionalCall)) {
                        return null;
                    }

                    if (!DETECT_OPTIONAL_MATCHER.matches(detectOptionalCall)) {
                        return null;
                    }

                    return new DetectOptionalCallResult(detectOptionalCall, isEmptyCheck);
                }

                record DetectOptionalCallResult(J.MethodInvocation detectOptionalCall, boolean isEmptyCheck) {}
            }
        );
    }
}
