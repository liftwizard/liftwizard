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

package io.liftwizard.rewrite.assertj;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class VerifyAssertThrowsToAssertJ extends Recipe {

    private static final MethodMatcher VERIFY_ASSERT_THROWS_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertThrows(java.lang.Class, *)"
    );

    @Override
    public String getDisplayName() {
        return "Replace `Verify.assertThrows()` with AssertJ";
    }

    @Override
    public String getDescription() {
        return "Replace Eclipse Collections `Verify.assertThrows()` with AssertJ `assertThatThrownBy().isInstanceOf()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(VERIFY_ASSERT_THROWS_MATCHER), new VerifyAssertThrowsVisitor());
    }

    private static final class VerifyAssertThrowsVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

            if (!VERIFY_ASSERT_THROWS_MATCHER.matches(methodInvocation)) {
                return methodInvocation;
            }

            Expression callable = methodInvocation.getArguments().get(1);

            String templateSourceCode = this.isLambdaOrMethodReference(callable)
                ? "assertThatThrownBy(#{any()}).isInstanceOf(#{any(java.lang.Class)})"
                : "assertThatThrownBy(#{any(java.util.concurrent.Callable)}::call).isInstanceOf(#{any(java.lang.Class)})";
            JavaTemplate template = JavaTemplate.builder(templateSourceCode)
                .staticImports("org.assertj.core.api.Assertions.assertThatThrownBy")
                .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                .build();

            this.maybeAddImport("org.assertj.core.api.Assertions", "assertThatThrownBy", false);
            this.maybeRemoveImport("org.eclipse.collections.impl.test.Verify");

            Expression exceptionClass = methodInvocation.getArguments().get(0);
            return template.apply(
                this.getCursor(),
                methodInvocation.getCoordinates().replace(),
                callable,
                exceptionClass
            );
        }

        private boolean isLambdaOrMethodReference(Expression expression) {
            return expression instanceof J.Lambda || expression instanceof J.MemberReference;
        }
    }
}
