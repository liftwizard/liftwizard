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
import java.util.List;
import java.util.Set;

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
import org.openrewrite.java.tree.Statement;

public class VerifyAssertThrowsToAssertJ extends Recipe {

    private static final MethodMatcher VERIFY_ASSERT_THROWS_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertThrows(java.lang.Class, java.lang.Runnable)",
        true
    );

    @Override
    public String getDisplayName() {
        return "Verify.assertThrows to AssertJ";
    }

    @Override
    public String getDescription() {
        return "Migrates Eclipse Collections Verify.assertThrows to AssertJ assertThatThrownBy pattern.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(2);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesMethod<>(VERIFY_ASSERT_THROWS_MATCHER),
            new JavaIsoVisitor<ExecutionContext>() {
                private final JavaTemplate template = JavaTemplate.builder(
                    "assertThatThrownBy(() -> #{any(java.lang.Object)}).isInstanceOf(#{any(java.lang.Class)}.class)"
                )
                    .staticImports("org.assertj.core.api.Assertions.assertThatThrownBy")
                    .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                    .build();

                @Override
                public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = super.visitMethodInvocation(method, ctx);
                    if (!VERIFY_ASSERT_THROWS_MATCHER.matches(m)) {
                        return m;
                    }

                    if (m.getArguments().size() != 2) {
                        return m;
                    }

                    Expression exceptionClassArg = m.getArguments().get(0);
                    Expression runnableArg = m.getArguments().get(1);

                    maybeAddImport("org.assertj.core.api.Assertions", "assertThatThrownBy");
                    maybeRemoveImport("org.eclipse.collections.impl.test.Verify");

                    if (runnableArg instanceof J.NewClass) {
                        J.NewClass newClass = (J.NewClass) runnableArg;
                        if (newClass.getBody() != null) {
                            List<Statement> statements = newClass.getBody().getStatements();
                            if (statements.size() == 1 && statements.get(0) instanceof J.MethodDeclaration) {
                                J.MethodDeclaration runMethod = (J.MethodDeclaration) statements.get(0);
                                if (runMethod.getBody() != null) {
                                    J.Block lambdaBody = runMethod
                                        .getBody()
                                        .withStatements(runMethod.getBody().getStatements());

                                    return template.apply(
                                        getCursor(),
                                        m.getCoordinates().replace(),
                                        lambdaBody,
                                        ((J.FieldAccess) exceptionClassArg).getTarget()
                                    );
                                }
                            }
                        }
                    }

                    return m;
                }
            }
        );
    }
}
