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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Simplifies negated empty checks.
 *
 * <p>Converts {@code !iterable.isEmpty()} to {@code iterable.notEmpty()} and vice versa
 * for Eclipse Collections {@code RichIterable} types. Skips transformations inside
 * {@code isEmpty()} or {@code notEmpty()} method implementations to prevent infinite recursion.
 */
public class ECSimplifyNegatedEmptyChecks extends Recipe {

	private static final String RICH_ITERABLE = "org.eclipse.collections.api.RichIterable";

	@Override
	public String getDisplayName() {
		return "`!isEmpty()` → `notEmpty()` and `!notEmpty()` → `isEmpty()`";
	}

	@Override
	public String getDescription() {
		return (
			"Simplifies negated empty checks: `!iterable.isEmpty()` to `iterable.notEmpty()` "
			+ "and `!iterable.notEmpty()` to `iterable.isEmpty()` for Eclipse Collections types. "
			+ "Prevents transformations inside isEmpty() or notEmpty() method implementations "
			+ "to avoid infinite recursion."
		);
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new SimplifyNegatedEmptyChecksVisitor();
	}

	private static final class SimplifyNegatedEmptyChecksVisitor extends JavaVisitor<ExecutionContext> {

		private static final String ISEMPTY_METHOD = "isEmpty";
		private static final String NOTEMPTY_METHOD = "notEmpty";

		@Override
		public J visitUnary(J.Unary unary, ExecutionContext ctx) {
			J result = super.visitUnary(unary, ctx);

			if (!(result instanceof J.Unary visitedUnary)) {
				return result;
			}

			if (visitedUnary.getOperator() != J.Unary.Type.Not) {
				return result;
			}

			if (!(visitedUnary.getExpression() instanceof J.MethodInvocation methodInv)) {
				return result;
			}

			String simpleName = methodInv.getSimpleName();
			String replacement;
			if (ISEMPTY_METHOD.equals(simpleName)) {
				replacement = NOTEMPTY_METHOD;
			} else if (NOTEMPTY_METHOD.equals(simpleName)) {
				replacement = ISEMPTY_METHOD;
			} else {
				return result;
			}

			if (
				methodInv.getSelect() == null
				|| !TypeUtils.isAssignableTo(RICH_ITERABLE, methodInv.getSelect().getType())
			) {
				return result;
			}

			// Prevent infinite recursion when isEmpty()/notEmpty() is implemented
			// in terms of the other, e.g. `boolean isEmpty() { return !this.notEmpty(); }`
			if (this.isInsideEmptyCheckMethod()) {
				return result;
			}

			return methodInv
				.withName(methodInv.getName().withSimpleName(replacement))
				.withPrefix(visitedUnary.getPrefix());
		}

		private boolean isInsideEmptyCheckMethod() {
			J.MethodDeclaration enclosingMethod = this.getCursor().firstEnclosing(J.MethodDeclaration.class);

			if (enclosingMethod == null) {
				return false;
			}

			String methodName = enclosingMethod.getSimpleName();
			return ISEMPTY_METHOD.equals(methodName) || NOTEMPTY_METHOD.equals(methodName);
		}
	}
}
