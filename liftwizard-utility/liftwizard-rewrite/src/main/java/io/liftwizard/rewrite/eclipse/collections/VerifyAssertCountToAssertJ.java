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
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class VerifyAssertCountToAssertJ extends Recipe {

    private static final MethodMatcher ASSERT_COUNT_MATCHER = new MethodMatcher(
        "org.eclipse.collections.impl.test.Verify assertCount(int, java.lang.Iterable, org.eclipse.collections.api.block.predicate.Predicate)",
        true
    );

    @Override
    public String getDisplayName() {
        return "Migrate Eclipse Collections Verify.assertCount to AssertJ";
    }

    @Override
    public String getDescription() {
        return "Migrates Eclipse Collections `Verify.assertCount(expectedCount, iterable, discriminator)` to AssertJ `assertThat(Iterate.count(iterable, discriminator)).isEqualTo(expectedCount)`.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("eclipse-collections");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofSeconds(30);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesMethod<>(ASSERT_COUNT_MATCHER),
            new JavaVisitor<ExecutionContext>() {
                @Override
                public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                    J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

                    if (!ASSERT_COUNT_MATCHER.matches(m)) {
                        return m;
                    }

                    if (m.getArguments().size() != 3) {
                        return m;
                    }

                    final Expression expectedCount = m.getArguments().get(0);
                    final Expression iterable = m.getArguments().get(1);
                    final Expression discriminator = m.getArguments().get(2);

                    final JavaTemplate template = JavaTemplate.builder(
                        "Assertions.assertThat(Iterate.count(#{any(java.lang.Iterable)}, #{any(org.eclipse.collections.api.block.predicate.Predicate)})).isEqualTo(#{any(int)})"
                    )
                        .imports("org.assertj.core.api.Assertions", "org.eclipse.collections.impl.utility.Iterate")
                        .contextSensitive()
                        .javaParser(
                            JavaParser.fromJavaVersion().classpath(
                                "assertj-core",
                                "eclipse-collections",
                                "eclipse-collections-api"
                            )
                        )
                        .build();

                    maybeAddImport("org.assertj.core.api.Assertions");
                    maybeAddImport("org.eclipse.collections.impl.utility.Iterate");
                    maybeRemoveImport("org.eclipse.collections.impl.test.Verify");

                    return template.apply(
                        getCursor(),
                        m.getCoordinates().replace(),
                        iterable,
                        discriminator,
                        expectedCount
                    );
                }
            }
        );
    }
}
