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
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;

public class AssertionsStaticImport extends Recipe {

	private static final MethodMatcher ASSERTIONS_STATIC_METHOD_MATCHER = new MethodMatcher(
		"org.assertj.core.api.Assertions *(..)"
	);

	@Override
	public String getDisplayName() {
		return "Convert `Assertions.*()` to static import";
	}

	@Override
	public String getDescription() {
		return "Convert `org.assertj.core.api.Assertions.*()` calls to use static import.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			new UsesType<>("org.assertj.core.api.Assertions", false),
			new AssertionsStaticImportVisitor()
		);
	}

	private static final class AssertionsStaticImportVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
			J.MethodInvocation methodInvocation = super.visitMethodInvocation(method, ctx);

			if (!ASSERTIONS_STATIC_METHOD_MATCHER.matches(methodInvocation)) {
				return methodInvocation;
			}

			if (!(methodInvocation.getSelect() instanceof J.Identifier identifier)) {
				return methodInvocation;
			}

			if (!"Assertions".equals(identifier.getSimpleName())) {
				return methodInvocation;
			}

			String methodName = methodInvocation.getSimpleName();
			int argumentCount = methodInvocation.getArguments().size();

			StringBuilder templatePattern = new StringBuilder(methodName).append('(');
			for (int i = 0; i < argumentCount; i++) {
				if (i > 0) {
					templatePattern.append(", ");
				}
				templatePattern.append("#{any()}");
			}
			templatePattern.append(')');

			JavaTemplate template = JavaTemplate.builder(templatePattern.toString())
				.staticImports("org.assertj.core.api.Assertions." + methodName)
				.javaParser(JavaParser.fromJavaVersion().classpath("assertj-core"))
				.build();

			this.maybeRemoveImport("org.assertj.core.api.Assertions");
			this.maybeAddImport("org.assertj.core.api.Assertions", methodName, false);

			return template.apply(
				this.getCursor(),
				methodInvocation.getCoordinates().replace(),
				methodInvocation.getArguments().toArray()
			);
		}
	}
}
