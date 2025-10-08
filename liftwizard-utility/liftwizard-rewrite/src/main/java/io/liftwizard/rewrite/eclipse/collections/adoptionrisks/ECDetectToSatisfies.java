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

package io.liftwizard.rewrite.eclipse.collections.adoptionrisks;

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

public class ECDetectToSatisfies extends Recipe {

    private static final MethodMatcher DETECT_MATCHER = new MethodMatcher(
        "org.eclipse.collections.api..* detect(..)",
        true
    );

    @Override
    public String getDisplayName() {
        return "`detect() != null` → `anySatisfy()`";
    }

    @Override
    public String getDescription() {
        return (
            "Converts `iterable.detect(predicate) != null` to `iterable.anySatisfy(predicate)` and `iterable.detect(predicate) == null` to `iterable.noneSatisfy(predicate)` for Eclipse Collections types. " +
            "Warning: This transformation can change semantics if the collection contains null values."
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
            new UsesMethod<>(DETECT_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public J visitBinary(J.Binary binary, ExecutionContext ctx) {
                    J.Binary b = (J.Binary) super.visitBinary(binary, ctx);

                    if (b.getOperator() != J.Binary.Type.Equal && b.getOperator() != J.Binary.Type.NotEqual) {
                        return b;
                    }

                    J.MethodInvocation detectCall = this.getDetectCall(b.getLeft());
                    boolean isNullOnRight = this.isNullLiteral(b.getRight());

                    if (detectCall == null && !isNullOnRight) {
                        detectCall = this.getDetectCall(b.getRight());
                        isNullOnRight = this.isNullLiteral(b.getLeft());
                    }

                    if (detectCall == null || !isNullOnRight) {
                        return b;
                    }

                    String newMethodName = b.getOperator() == J.Binary.Type.Equal ? "noneSatisfy" : "anySatisfy";

                    return detectCall
                        .withName(detectCall.getName().withSimpleName(newMethodName))
                        .withPrefix(b.getPrefix());
                }

                private J.MethodInvocation getDetectCall(Expression expression) {
                    if (!(expression instanceof J.MethodInvocation)) {
                        return null;
                    }

                    J.MethodInvocation methodInvocation = (J.MethodInvocation) expression;

                    if (!DETECT_MATCHER.matches(methodInvocation)) {
                        return null;
                    }

                    return methodInvocation;
                }

                private boolean isNullLiteral(Expression expression) {
                    return expression instanceof J.Literal && ((J.Literal) expression).getValue() == null;
                }
            }
        );
    }
}
