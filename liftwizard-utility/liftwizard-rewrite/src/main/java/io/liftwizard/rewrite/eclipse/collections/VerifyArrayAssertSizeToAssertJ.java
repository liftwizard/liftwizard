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
import org.openrewrite.java.tree.J;

public class VerifyArrayAssertSizeToAssertJ extends Recipe {

    private static final MethodMatcher VERIFY_ASSERT_SIZE_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertSize(String, int, ..)",
        true
    );

    @Override
    public String getDisplayName() {
        return "Transform Verify.assertSize to AssertJ for arrays";
    }

    @Override
    public String getDescription() {
        return "Transforms `Verify.assertSize(message, size, array)` to `assertThat(array).as(message).hasSize(size)` using AssertJ.";
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
            new UsesMethod<>(VERIFY_ASSERT_SIZE_MATCHER),
            new JavaIsoVisitor<ExecutionContext>() {
                private final JavaTemplate assertJTemplate = JavaTemplate.builder(
                    "assertThat(#{any()}).as(#{any(String)}).hasSize(#{any(int)})"
                )
                    .javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
                    .staticImports("org.assertj.core.api.Assertions.assertThat")
                    .build();

                @Override
                public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

                    if (!VERIFY_ASSERT_SIZE_MATCHER.matches(m)) {
                        return m;
                    }

                    List<org.openrewrite.java.tree.Expression> args = m.getArguments();
                    if (args.size() != 3) {
                        return m;
                    }

                    // Check that the third argument is an array type
                    if (
                        args.get(2).getType() != null &&
                        !(args.get(2).getType() instanceof org.openrewrite.java.tree.JavaType.Array)
                    ) {
                        return m;
                    }

                    maybeAddImport("org.assertj.core.api.Assertions", "assertThat");
                    maybeRemoveImport("org.eclipse.collections.impl.test.Verify");

                    return assertJTemplate.apply(
                        getCursor(),
                        m.getCoordinates().replace(),
                        args.get(2), // array
                        args.get(0), // message
                        args.get(1) // size
                    );
                }
            }
        );
    }
}
